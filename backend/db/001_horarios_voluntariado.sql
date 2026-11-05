-- Execute uma vez no PostgreSQL existente antes de iniciar esta versao.
-- As tabelas originais continuam em database/init.sql (sem alteracoes).
BEGIN;
CREATE TABLE IF NOT EXISTS horario_voluntariado (
    id SERIAL PRIMARY KEY,
    instituicao_id INT NOT NULL REFERENCES instituicao(id),
    dia VARCHAR(20) NOT NULL CHECK (dia IN ('Segunda-feira', 'Terça-feira', 'Quarta-feira',
        'Quinta-feira', 'Sexta-feira', 'Sábado', 'Domingo')),
    funcao VARCHAR(150) NOT NULL CHECK (length(trim(funcao)) > 0),
    inicio TIME NOT NULL,
    fim TIME NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    CHECK (fim > inicio)
);
ALTER TABLE voluntariado ADD COLUMN IF NOT EXISTS horario_id INT REFERENCES horario_voluntariado(id);
CREATE INDEX IF NOT EXISTS idx_horario_instituicao ON horario_voluntariado(instituicao_id);
CREATE INDEX IF NOT EXISTS idx_voluntariado_horario ON voluntariado(horario_id);
COMMIT;
