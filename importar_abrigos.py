#!/usr/bin/env python3
"""Importa abrigos do arquivo de texto para instituicao/localizacao do AcolheSP.

Uso (na raiz do projeto):
  python3 importar_abrigos.py --dry-run   # só valida o TXT; não grava no banco
  python3 importar_abrigos.py             # importa em uma transação

A conexão usa DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD. Como alternativa,
lê POSTGRES_DB, POSTGRES_USER e POSTGRES_PASSWORD do .env do Docker Compose.
"""

import argparse
import os
import re
import sys
from decimal import Decimal, InvalidOperation
from pathlib import Path


BASE_DIR = Path(__file__).resolve().parent
ARQUIVO_ENTRADA = BASE_DIR / "centro_acolhida_geocodificado.txt"

# Os grupos entre aspas podem ter qualquer quantidade de caracteres, inclusive vazio.
PADRAO_LINHA = re.compile(r'^\s*(\d+)\s+"([^"]*)"\s+"([^"]*)"\s*$')

# Reconhece onde começa o endereço dentro do campo combinado do TXT.
PADRAO_SEPARADOR_ENDERECO = re.compile(
    r'\s+-\s+(?=(?:RUA\b|R\.\s|AV\.?\s|AVENIDA\b|ALAMEDA\b|'
    r'PRAÇA\b|PRACA\b|LARGO\b|ESTRADA\b|TRAVESSA\b|RODOVIA\b|'
    r'VIADUTO\b|VIA\b|BECO\b|PASSAGEM\b))',
    re.IGNORECASE,
)


def ler_env_compose(caminho: Path) -> dict[str, str]:
    """Lê as variáveis simples do .env sem exigir python-dotenv."""
    dados: dict[str, str] = {}
    if not caminho.exists():
        return dados

    for linha in caminho.read_text(encoding="utf-8").splitlines():
        linha = linha.strip()
        if not linha or linha.startswith("#") or "=" not in linha:
            continue
        chave, valor = linha.split("=", 1)
        chave, valor = chave.strip(), valor.strip()
        if len(valor) >= 2 and valor[0] == valor[-1] and valor[0] in ("'", '"'):
            valor = valor[1:-1]
        dados[chave] = valor
    return dados


def configuracao_banco() -> dict[str, object]:
    compose_env = ler_env_compose(BASE_DIR / ".env")
    return {
        "host": os.getenv("DB_HOST", "localhost"),
        "port": int(os.getenv("DB_PORT", "5432")),
        "dbname": os.getenv("DB_NAME") or os.getenv("POSTGRES_DB") or compose_env.get("POSTGRES_DB", "acolhesp"),
        "user": os.getenv("DB_USER") or os.getenv("POSTGRES_USER") or compose_env.get("POSTGRES_USER", "app"),
        "password": os.getenv("DB_PASSWORD") or os.getenv("POSTGRES_PASSWORD") or compose_env.get("POSTGRES_PASSWORD", "exemplo"),
        "connect_timeout": 8,
    }


def separar_nome_endereco(texto: str) -> tuple[str, str]:
    texto = texto.strip()
    separador = PADRAO_SEPARADOR_ENDERECO.search(texto)
    if separador:
        nome = texto[:separador.start()].strip()
        endereco = texto[separador.end():].strip()
        if nome and endereco:
            return nome, endereco
    # Se a linha não permitir separar com segurança, preserva o texto inteiro nos dois campos.
    return texto, texto


def interpretar_linha(linha: str, numero_linha: int) -> dict[str, object] | None:
    linha = linha.strip()
    if not linha:
        return None

    match = PADRAO_LINHA.match(linha)
    if not match:
        raise ValueError(f"Linha {numero_linha}: formato inesperado: {linha}")

    codigo_fonte, nome_e_endereco, coordenadas = match.groups()
    nome, endereco = separar_nome_endereco(nome_e_endereco)
    if not nome:
        raise ValueError(f"Linha {numero_linha}: nome do abrigo vazio.")
    if len(nome) > 150:
        raise ValueError(f"Linha {numero_linha}: nome excede 150 caracteres ({len(nome)}).")
    if len(endereco) > 255:
        raise ValueError(f"Linha {numero_linha}: endereço excede 255 caracteres ({len(endereco)}).")

    latitude = longitude = None
    coordenadas = coordenadas.strip()
    if coordenadas:
        partes = coordenadas.split()
        if len(partes) != 2:
            raise ValueError(f"Linha {numero_linha}: coordenadas devem ter latitude e longitude.")
        try:
            latitude = Decimal(partes[0])
            longitude = Decimal(partes[1])
        except InvalidOperation as exc:
            raise ValueError(f"Linha {numero_linha}: coordenadas inválidas: {coordenadas}") from exc
        if not Decimal("-90") <= latitude <= Decimal("90"):
            raise ValueError(f"Linha {numero_linha}: latitude fora do intervalo válido: {latitude}")
        if not Decimal("-180") <= longitude <= Decimal("180"):
            raise ValueError(f"Linha {numero_linha}: longitude fora do intervalo válido: {longitude}")

    return {
        "codigo_fonte": int(codigo_fonte),  # é o índice do TXT, não o ID do banco
        "nome": nome,
        "endereco": endereco,
        "latitude": latitude,
        "longitude": longitude,
    }


def ler_registros() -> list[dict[str, object]]:
    if not ARQUIVO_ENTRADA.exists():
        raise FileNotFoundError(
            f"Não encontrei {ARQUIVO_ENTRADA}. Coloque este script na raiz do projeto, "
            "ao lado de centro_acolhida_geocodificado.txt."
        )

    registros = []
    with ARQUIVO_ENTRADA.open("r", encoding="utf-8-sig") as arquivo:
        for numero_linha, linha in enumerate(arquivo, start=1):
            registro = interpretar_linha(linha, numero_linha)
            if registro is not None:
                registros.append(registro)

    if not registros:
        raise ValueError("O arquivo não contém abrigos para importar.")
    return registros


def importar(registros: list[dict[str, object]]) -> tuple[int, int, int, int]:
    try:
        import psycopg2
    except ImportError as erro:
        raise RuntimeError("Pacote psycopg2-binary não instalado. Execute: python3 -m pip install psycopg2-binary") from erro

    cfg = configuracao_banco()
    criadas = 0
    localizacoes_criadas = 0
    existentes = 0
    sem_coordenadas = 0

    # O bloco with confirma a transação quando termina; qualquer erro faz rollback.
    with psycopg2.connect(**cfg) as conexao:
        with conexao.cursor() as cursor:
            for registro in registros:
                cursor.execute(
                    """SELECT i.id,
                              EXISTS (SELECT 1 FROM localizacao l WHERE l.instituicao_id = i.id)
                       FROM instituicao i
                       WHERE lower(btrim(i.nome)) = lower(btrim(%s))
                       ORDER BY i.id
                       LIMIT 1""",
                    (registro["nome"],),
                )
                encontrado = cursor.fetchone()

                if encontrado:
                    instituicao_id = encontrado[0]
                    tem_localizacao = encontrado[1]
                    existentes += 1
                else:
                    cursor.execute(
                        """INSERT INTO instituicao (conta_id, nome, status_verificacao)
                           VALUES (NULL, %s, 'PENDENTE')
                           RETURNING id""",
                        (registro["nome"],),
                    )
                    instituicao_id = cursor.fetchone()[0]
                    tem_localizacao = False
                    criadas += 1

                latitude = registro["latitude"]
                longitude = registro["longitude"]
                if latitude is None or longitude is None:
                    sem_coordenadas += 1
                    continue

                if not tem_localizacao:
                    cursor.execute(
                        """INSERT INTO localizacao (endereco, latitude, longitude, instituicao_id)
                           VALUES (%s, %s, %s, %s)""",
                        (registro["endereco"], latitude, longitude, instituicao_id),
                    )
                    localizacoes_criadas += 1

    return criadas, localizacoes_criadas, existentes, sem_coordenadas


def main() -> int:
    parser = argparse.ArgumentParser(description="Importar abrigos do TXT para o PostgreSQL do AcolheSP.")
    parser.add_argument("--dry-run", action="store_true", help="valida o TXT sem conectar nem gravar no banco")
    args = parser.parse_args()

    try:
        registros = ler_registros()
    except (OSError, UnicodeError, ValueError) as erro:
        print(f"ERRO ao validar arquivo: {erro}", file=sys.stderr)
        return 1

    com_coordenadas = sum(r["latitude"] is not None and r["longitude"] is not None for r in registros)
    sem_coordenadas = len(registros) - com_coordenadas
    print(f"Arquivo: {ARQUIVO_ENTRADA.name}")
    print(f"Registros lidos: {len(registros)}")
    print(f"Registros com coordenadas: {com_coordenadas}")
    print(f"Registros sem coordenadas: {sem_coordenadas}")

    if args.dry_run:
        print("DRY-RUN: nenhuma alteração foi feita no banco.")
        return 0

    try:
        criadas, locais, existentes, sem_coords_db = importar(registros)
    except Exception as erro:
        print("ERRO ao conectar ou importar. Se a conexão chegou a iniciar, a transação foi cancelada; confira banco, usuário, senha e esquema.", file=sys.stderr)
        print(f"Detalhe: {erro}", file=sys.stderr)
        return 2

    print("\nImportação concluída.")
    print(f"Instituições novas: {criadas}")
    print(f"Localizações novas: {locais}")
    print(f"Instituições já existentes encontradas: {existentes}")
    print(f"Linhas sem coordenadas: {sem_coords_db}")
    print("As instituições sem coordenadas foram cadastradas sem linha em localizacao; não inventamos coordenadas.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
