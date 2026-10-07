-- =====================================================================
-- AcolheSP - Banco de dados (PostgreSQL 16)
-- Baseado em docs/generics/DiagramaEntidadeRelacionamento.puml
-- Roda automaticamente na 1ª vez que o container "database" sobe
-- (via /docker-entrypoint-initdb.d).
-- =====================================================================

-- ---------------------------------------------------------------------
-- CONTA (superclasse) - role vem do Keycloak: admin | abrigo | usuario
-- ---------------------------------------------------------------------
CREATE TABLE conta (
    id             SERIAL       PRIMARY KEY,
    keycloak_id    UUID         UNIQUE,            -- "sub" do token JWT do Keycloak
    email          VARCHAR(255) NOT NULL UNIQUE,
    senha_hash     VARCHAR(255),                   -- NULL: quem autentica é o Keycloak
    role           VARCHAR(20)  NOT NULL
                   CHECK (role IN ('admin', 'abrigo', 'usuario')),
    ativo          BOOLEAN      NOT NULL DEFAULT TRUE,
    data_cadastro  DATE         NOT NULL DEFAULT CURRENT_DATE
);

-- ---------------------------------------------------------------------
-- VOLUNTARIO (role = usuario)
-- ---------------------------------------------------------------------
CREATE TABLE voluntario (
    id              INT          PRIMARY KEY REFERENCES conta(id) ON DELETE CASCADE,
    nome            VARCHAR(150) NOT NULL,
    telefone        VARCHAR(20),
    disponibilidade VARCHAR(255)
);

-- ---------------------------------------------------------------------
-- INSTITUICAO (role = abrigo)
-- ---------------------------------------------------------------------
CREATE TABLE instituicao (
    id                  INT          PRIMARY KEY REFERENCES conta(id) ON DELETE CASCADE,
    nome                VARCHAR(150) NOT NULL,
    cnpj                VARCHAR(18)  UNIQUE,
    descricao           TEXT,
    telefone            VARCHAR(20),
    email_contato       VARCHAR(255),
    status_verificacao  VARCHAR(20)  NOT NULL DEFAULT 'PENDENTE'
                        CHECK (status_verificacao IN ('PENDENTE', 'VERIFICADA', 'RECUSADA'))
);

-- ---------------------------------------------------------------------
-- LOCALIZACAO (1 instituição : 1 localização)
-- ---------------------------------------------------------------------
CREATE TABLE localizacao (
    id             SERIAL         PRIMARY KEY,
    endereco       VARCHAR(255)   NOT NULL,
    cep            VARCHAR(9),
    latitude       DECIMAL(9, 6)  NOT NULL,
    longitude      DECIMAL(9, 6)  NOT NULL,
    instituicao_id INT            NOT NULL UNIQUE
                   REFERENCES instituicao(id) ON DELETE CASCADE
);

-- ---------------------------------------------------------------------
-- NECESSIDADE (instituição 1 : N necessidades)
-- ---------------------------------------------------------------------
CREATE TABLE necessidade (
    id                    SERIAL       PRIMARY KEY,
    descricao             VARCHAR(255) NOT NULL,
    quantidade_solicitada INT          NOT NULL CHECK (quantidade_solicitada > 0),
    quantidade_recebida   INT          NOT NULL DEFAULT 0 CHECK (quantidade_recebida >= 0),
    status                VARCHAR(20)  NOT NULL DEFAULT 'ABERTA'
                          CHECK (status IN ('ABERTA', 'ATENDIDA', 'ENCERRADA')),
    instituicao_id        INT          NOT NULL
                          REFERENCES instituicao(id) ON DELETE CASCADE
);

-- ---------------------------------------------------------------------
-- DOACAO (voluntário 1 : N doações / necessidade 1 : N doações)
-- ---------------------------------------------------------------------
CREATE TABLE doacao (
    id             SERIAL      PRIMARY KEY,
    data           DATE        NOT NULL DEFAULT CURRENT_DATE,
    quantidade     INT         NOT NULL CHECK (quantidade > 0),
    status         VARCHAR(20) NOT NULL DEFAULT 'PENDENTE'
                   CHECK (status IN ('PENDENTE', 'CONFIRMADA', 'ENTREGUE', 'CANCELADA')),
    voluntario_id  INT         NOT NULL REFERENCES voluntario(id)  ON DELETE CASCADE,
    necessidade_id INT         NOT NULL REFERENCES necessidade(id) ON DELETE CASCADE
);

-- ---------------------------------------------------------------------
-- VOLUNTARIADO (voluntário solicita / instituição avalia)
-- ---------------------------------------------------------------------
CREATE TABLE voluntariado (
    id               SERIAL      PRIMARY KEY,
    data_solicitacao DATE        NOT NULL DEFAULT CURRENT_DATE,
    data_inicio      DATE,
    data_fim         DATE,
    status           VARCHAR(20) NOT NULL DEFAULT 'PENDENTE'
                     CHECK (status IN ('PENDENTE', 'APROVADO', 'RECUSADO',
                                       'EM_ANDAMENTO', 'FINALIZADO', 'CANCELADO')),
    voluntario_id    INT         NOT NULL REFERENCES voluntario(id)  ON DELETE CASCADE,
    instituicao_id   INT         NOT NULL REFERENCES instituicao(id) ON DELETE CASCADE,
    CHECK (data_fim IS NULL OR data_inicio IS NULL OR data_fim >= data_inicio)
);

-- ---------------------------------------------------------------------
-- Índices nas FKs (consultas mais comuns)
-- ---------------------------------------------------------------------
CREATE INDEX idx_necessidade_instituicao  ON necessidade(instituicao_id);
CREATE INDEX idx_doacao_voluntario        ON doacao(voluntario_id);
CREATE INDEX idx_doacao_necessidade       ON doacao(necessidade_id);
CREATE INDEX idx_voluntariado_voluntario  ON voluntariado(voluntario_id);
CREATE INDEX idx_voluntariado_instituicao ON voluntariado(instituicao_id);

-- =====================================================================
-- SEED: 3 usuários de teste (1 por role do Keycloak)
-- >>> Troque email e keycloak_id pelos usuários que estão no Keycloak <<<
-- keycloak_id = campo "ID" do usuário em Users no painel do Keycloak.
-- Enquanto NULL, o vínculo pode ser feito pelo email.
-- =====================================================================
INSERT INTO conta (keycloak_id, email, role) VALUES
    (NULL, 'admin.teste@acolhesp.com',   'admin'),
    (NULL, 'abrigo.teste@acolhesp.com',  'abrigo'),
    (NULL, 'usuario.teste@acolhesp.com', 'usuario');

-- role = usuario -> voluntario
INSERT INTO voluntario (id, nome, telefone, disponibilidade)
SELECT id, 'Maria Souza', '(11) 90000-0001', 'Fins de semana'
FROM conta WHERE email = 'usuario.teste@acolhesp.com';

-- role = abrigo -> instituicao (+ localizacao)
INSERT INTO instituicao (id, nome, cnpj, descricao, telefone, email_contato, status_verificacao)
SELECT id, 'Abrigo Esperança', '12.345.678/0001-90',
       'Abrigo de teste para pessoas em situação de rua.',
       '(11) 3000-0001', 'abrigo.teste@acolhesp.com', 'VERIFICADA'
FROM conta WHERE email = 'abrigo.teste@acolhesp.com';

INSERT INTO localizacao (endereco, cep, latitude, longitude, instituicao_id)
SELECT 'Praça da Sé, São Paulo - SP', '01001-000', -23.550520, -46.633308, id
FROM conta WHERE email = 'abrigo.teste@acolhesp.com';

-- necessidades de exemplo do abrigo de teste
INSERT INTO necessidade (descricao, quantidade_solicitada, quantidade_recebida, status, instituicao_id)
SELECT 'Cobertores', 50, 10, 'ABERTA', id
FROM conta WHERE email = 'abrigo.teste@acolhesp.com';

INSERT INTO necessidade (descricao, quantidade_solicitada, quantidade_recebida, status, instituicao_id)
SELECT 'Alimentos não perecíveis (kg)', 100, 0, 'ABERTA', id
FROM conta WHERE email = 'abrigo.teste@acolhesp.com';

-- role = admin -> só existe em conta (não tem tabela própria no diagrama)