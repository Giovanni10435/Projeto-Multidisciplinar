# Testar os fluxos pelo site no WSL

As rotas e telas existentes foram preservadas. O BFF agora repassa, por
requisição, o cookie de sessão e o header CSRF, e devolve os cookies atualizados
do backend ao navegador. Nenhuma sessão é compartilhada entre usuários.
O frontend obtém o token antes das escritas, renova após login/logout e
restaura a conta ao recarregar a página. A autorização continua no backend.

## 1. Banco e migração

No terminal do WSL, na raiz do projeto:

```bash
cd /home/yan/Projeto-Multidisciplinar
docker compose up -d database
```

Espere o banco ficar pronto e aplique a migração incremental (pode ser
reexecutada; não apaga as tabelas nem os registros existentes):

```bash
docker compose exec -T database sh -c 'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -v ON_ERROR_STOP=1' \
  < backend/db/001_horarios_voluntariado.sql
```

## 2. Iniciar os três serviços

Use três terminais, mantendo cada processo aberto.

Backend:

```bash
cd /home/yan/Projeto-Multidisciplinar/backend/demo
mvn spring-boot:run
```

BFF:

```bash
cd /home/yan/Projeto-Multidisciplinar/BFF/bff
mvn spring-boot:run
```

Frontend:

```bash
cd /home/yan/Projeto-Multidisciplinar/frontend
npm run dev
```

Abra **http://localhost:5173**. O proxy existente do Vite envia `/api` ao BFF
na porta 8081, e o BFF usa `BACKEND_URL`, com padrão `http://localhost:8080`.
As configurações de deploy e os arquivos `.env` não foram alterados.
O backend deve usar as mesmas credenciais do PostgreSQL local.

## 3. Testar como gestor

1. Clique em **Entrar**, escolha gestor e use uma conta já vinculada ao abrigo.
2. Abra o painel pelo botão com o nome do abrigo.
3. Em **Necessidades**, cadastre um item, edite a descrição/quantidade e,
   se necessário, encerre ou remova o item. A remoção pode ser recusada
   quando já há doações; o backend preserva o histórico.
4. Em **Voluntários**, cadastre dia, função, início e fim. Edite um horário.
5. Recarregue a página e reabra o painel para confirmar que os dados persistem.
6. Saia da conta usando o botão existente; o logout agora encerra a sessão real.

## 4. Testar como voluntário

1. Entre com uma conta de usuário/voluntário, ou cadastre uma e depois entre.
2. No mapa, abra **Mais informações** do abrigo usado no teste anterior.
3. Em **Necessidades**, informe uma quantidade e clique em **Quero doar**.
4. Em **Voluntários**, escolha o horário e clique em **Quero ser voluntário**.
   Telefone e disponibilidade são opcionais.
5. Tente repetir a candidatura: o backend deve recusar uma candidatura ativa
   duplicada no mesmo horário. Uma doação acima do saldo também é recusada.

Sem login, os botões de participação levam ao login; as consultas permanecem
públicas. O token é tratado automaticamente, sem preenchimento manual.

## 5. Aprovar e concluir

1. Saia da conta do voluntário e entre novamente como gestor.
2. Abra a aba **Solicitações** no painel do abrigo.
3. Confirme uma doação e depois clique em **Registrar entrega**.
4. Confira que a quantidade recebida da necessidade foi atualizada.
5. Aprove a candidatura, clique em **Iniciar** e depois em **Concluir**.
6. Estados finais não apresentam botões para reabrir; o backend também
   impede contabilizar uma entrega duas vezes e gerenciar outro abrigo.

Se o banco foi inicializado pelo `database/init.sql` atual e suas credenciais
não foram alteradas, existem contas de demonstração:

- Gestor: `abrigo.teste@acolhesp.com`.
- Voluntário: `usuario.teste@acolhesp.com`.
- Senha de demonstração dessas contas: `123456`.

Nenhuma senha de conta existente é redefinida automaticamente. O cadastro
público de novos abrigos continua restrito pelo backend, como definido na
issue #32; use um gestor de abrigo já existente para testar estes fluxos.

## Validação técnica

```bash
cd /home/yan/Projeto-Multidisciplinar/BFF/bff
mvn test

cd /home/yan/Projeto-Multidisciplinar/frontend
npm test
npm run build
```

Os testes do BFF usam requisições WebFlux em memória com respostas de backend
simuladas, incluindo cookies, CSRF, erros, rotas e isolamento entre sessões.
Os testes do frontend verificam as chamadas HTTP e a renovação do token.
Essas verificações não substituem o roteiro acima com os três serviços e
PostgreSQL real.
