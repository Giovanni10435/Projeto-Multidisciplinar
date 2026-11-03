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
    id                  INT          PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    conta_id            INT          UNIQUE REFERENCES conta(id) ON DELETE SET NULL,
    nome                VARCHAR(150) NOT NULL,
    cnpj                VARCHAR(18)  UNIQUE,
    descricao           TEXT,
    telefone            VARCHAR(20),
    email_contato       VARCHAR(255),
    capacidade          INT,
    ocupacao_atual      INT,
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


-- ---------------------------------------------------------------------
-- Voluntário: mantém o ID compartilhado com a conta
-- ---------------------------------------------------------------------

INSERT INTO voluntario (id, nome, telefone, disponibilidade)
SELECT
    id,
    'Maria Souza',
    '(11) 90000-0001',
    'Fins de semana'
FROM conta
WHERE email = 'usuario.teste@acolhesp.com';


-- ---------------------------------------------------------------------
-- Abrigo Esperança: ID próprio e conta vinculada por conta_id
-- ---------------------------------------------------------------------

INSERT INTO instituicao (
    conta_id,
    nome,
    cnpj,
    descricao,
    telefone,
    email_contato,
    status_verificacao
)
SELECT
    c.id,
    'Abrigo Esperança',
    '12.345.678/0001-90',
    'Abrigo de teste para pessoas em situação de rua.',
    '(11) 3000-0001',
    'abrigo.teste@acolhesp.com',
    'VERIFICADA'
FROM conta c
WHERE c.email = 'abrigo.teste@acolhesp.com';


INSERT INTO localizacao (
    endereco,
    cep,
    latitude,
    longitude,
    instituicao_id
)
SELECT
    'Praça da Sé, São Paulo - SP',
    '01001-000',
    -23.550520,
    -46.633308,
    i.id
FROM instituicao i
JOIN conta c ON c.id = i.conta_id
WHERE c.email = 'abrigo.teste@acolhesp.com';


-- Necessidades do Abrigo Esperança

INSERT INTO necessidade (
    descricao,
    quantidade_solicitada,
    quantidade_recebida,
    status,
    instituicao_id
)
SELECT
    'Cobertores',
    50,
    10,
    'ABERTA',
    i.id
FROM instituicao i
JOIN conta c ON c.id = i.conta_id
WHERE c.email = 'abrigo.teste@acolhesp.com';


INSERT INTO necessidade (
    descricao,
    quantidade_solicitada,
    quantidade_recebida,
    status,
    instituicao_id
)
SELECT
    'Alimentos não perecíveis (kg)',
    100,
    0,
    'ABERTA',
    i.id
FROM instituicao i
JOIN conta c ON c.id = i.conta_id
WHERE c.email = 'abrigo.teste@acolhesp.com';


-- role = admin -> só existe em conta


-- =====================================================================
-- SEED EXTRA
-- =====================================================================

INSERT INTO conta (keycloak_id, email, role) VALUES
    (NULL, 'abrigo2.teste@acolhesp.com',  'abrigo'),
    (NULL, 'abrigo3.teste@acolhesp.com',  'abrigo'),
    (NULL, 'usuario2.teste@acolhesp.com', 'usuario'),
    (NULL, 'usuario3.teste@acolhesp.com', 'usuario');


-- ---------------------------------------------------------------------
-- Voluntários extras
-- ---------------------------------------------------------------------

INSERT INTO voluntario (id, nome, telefone, disponibilidade)
SELECT
    id,
    'Carlos Lima',
    '(11) 90000-0002',
    'Sábados de manhã'
FROM conta
WHERE email = 'usuario2.teste@acolhesp.com';


INSERT INTO voluntario (id, nome, telefone, disponibilidade)
SELECT
    id,
    'Ana Pereira',
    '(11) 90000-0003',
    'Noites de segunda a sexta'
FROM conta
WHERE email = 'usuario3.teste@acolhesp.com';


-- ---------------------------------------------------------------------
-- Instituições extras, ambas com conta vinculada
-- ---------------------------------------------------------------------

INSERT INTO instituicao (
    conta_id,
    nome,
    cnpj,
    descricao,
    telefone,
    email_contato,
    status_verificacao
)
SELECT
    c.id,
    'Casa Acolher',
    '98.765.432/0001-10',
    'Casa de acolhimento com refeições e pernoite.',
    '(11) 3000-0002',
    'abrigo2.teste@acolhesp.com',
    'VERIFICADA'
FROM conta c
WHERE c.email = 'abrigo2.teste@acolhesp.com';


INSERT INTO instituicao (
    conta_id,
    nome,
    cnpj,
    descricao,
    telefone,
    email_contato,
    status_verificacao
)
SELECT
    c.id,
    'Lar Santa Luzia',
    '11.222.333/0001-44',
    'Lar para idosos (aguardando verificação).',
    '(11) 3000-0003',
    'abrigo3.teste@acolhesp.com',
    'PENDENTE'
FROM conta c
WHERE c.email = 'abrigo3.teste@acolhesp.com';


-- ---------------------------------------------------------------------
-- Localizações extras
-- ---------------------------------------------------------------------

INSERT INTO localizacao (
    endereco,
    cep,
    latitude,
    longitude,
    instituicao_id
)
SELECT
    'Av. Paulista, 1000, São Paulo - SP',
    '01310-100',
    -23.563210,
    -46.654250,
    i.id
FROM instituicao i
JOIN conta c ON c.id = i.conta_id
WHERE c.email = 'abrigo2.teste@acolhesp.com';


INSERT INTO localizacao (
    endereco,
    cep,
    latitude,
    longitude,
    instituicao_id
)
SELECT
    'Rua dos Pinheiros, 800, São Paulo - SP',
    '05422-001',
    -23.566900,
    -46.692000,
    i.id
FROM instituicao i
JOIN conta c ON c.id = i.conta_id
WHERE c.email = 'abrigo3.teste@acolhesp.com';


-- ---------------------------------------------------------------------
-- Necessidades extras
-- ---------------------------------------------------------------------

INSERT INTO necessidade (
    descricao,
    quantidade_solicitada,
    quantidade_recebida,
    status,
    instituicao_id
)
SELECT
    'Roupas de inverno',
    80,
    20,
    'ABERTA',
    i.id
FROM instituicao i
JOIN conta c ON c.id = i.conta_id
WHERE c.email = 'abrigo2.teste@acolhesp.com';


INSERT INTO necessidade (
    descricao,
    quantidade_solicitada,
    quantidade_recebida,
    status,
    instituicao_id
)
SELECT
    'Produtos de higiene',
    60,
    0,
    'ABERTA',
    i.id
FROM instituicao i
JOIN conta c ON c.id = i.conta_id
WHERE c.email = 'abrigo2.teste@acolhesp.com';


INSERT INTO necessidade (
    descricao,
    quantidade_solicitada,
    quantidade_recebida,
    status,
    instituicao_id
)
SELECT
    'Leite em pó (latas)',
    30,
    30,
    'ATENDIDA',
    i.id
FROM instituicao i
JOIN conta c ON c.id = i.conta_id
WHERE c.email = 'abrigo2.teste@acolhesp.com';


INSERT INTO necessidade (
    descricao,
    quantidade_solicitada,
    quantidade_recebida,
    status,
    instituicao_id
)
SELECT
    'Fraldas geriátricas',
    40,
    0,
    'ABERTA',
    i.id
FROM instituicao i
JOIN conta c ON c.id = i.conta_id
WHERE c.email = 'abrigo3.teste@acolhesp.com';


-- ---------------------------------------------------------------------
-- Doações do Abrigo Esperança e da Casa Acolher
-- A busca identifica a necessidade pela descrição e pela instituição.
-- ---------------------------------------------------------------------

INSERT INTO doacao (
    quantidade,
    status,
    voluntario_id,
    necessidade_id
)
SELECT
    5,
    'PENDENTE',
    (
        SELECT v.id
        FROM voluntario v
        JOIN conta c ON c.id = v.id
        WHERE c.email = 'usuario.teste@acolhesp.com'
    ),
    n.id
FROM necessidade n
JOIN instituicao i ON i.id = n.instituicao_id
JOIN conta c ON c.id = i.conta_id
WHERE n.descricao = 'Cobertores'
  AND c.email = 'abrigo.teste@acolhesp.com';


INSERT INTO doacao (
    quantidade,
    status,
    voluntario_id,
    necessidade_id
)
SELECT
    10,
    'CONFIRMADA',
    (
        SELECT v.id
        FROM voluntario v
        JOIN conta c ON c.id = v.id
        WHERE c.email = 'usuario2.teste@acolhesp.com'
    ),
    n.id
FROM necessidade n
JOIN instituicao i ON i.id = n.instituicao_id
JOIN conta c ON c.id = i.conta_id
WHERE n.descricao = 'Roupas de inverno'
  AND c.email = 'abrigo2.teste@acolhesp.com';


INSERT INTO doacao (
    quantidade,
    status,
    voluntario_id,
    necessidade_id
)
SELECT
    3,
    'CANCELADA',
    (
        SELECT v.id
        FROM voluntario v
        JOIN conta c ON c.id = v.id
        WHERE c.email = 'usuario3.teste@acolhesp.com'
    ),
    n.id
FROM necessidade n
JOIN instituicao i ON i.id = n.instituicao_id
JOIN conta c ON c.id = i.conta_id
WHERE n.descricao = 'Cobertores'
  AND c.email = 'abrigo.teste@acolhesp.com';


-- ---------------------------------------------------------------------
-- Voluntariados
-- ---------------------------------------------------------------------

INSERT INTO voluntariado (
    status,
    voluntario_id,
    instituicao_id
)
SELECT
    'PENDENTE',
    (
        SELECT v.id
        FROM voluntario v
        JOIN conta c ON c.id = v.id
        WHERE c.email = 'usuario.teste@acolhesp.com'
    ),
    i.id
FROM instituicao i
JOIN conta c ON c.id = i.conta_id
WHERE c.email = 'abrigo.teste@acolhesp.com';


INSERT INTO voluntariado (
    status,
    data_inicio,
    data_fim,
    voluntario_id,
    instituicao_id
)
SELECT
    'APROVADO',
    CURRENT_DATE + 7,
    CURRENT_DATE + 37,
    (
        SELECT v.id
        FROM voluntario v
        JOIN conta c ON c.id = v.id
        WHERE c.email = 'usuario3.teste@acolhesp.com'
    ),
    i.id
FROM instituicao i
JOIN conta c ON c.id = i.conta_id
WHERE c.email = 'abrigo2.teste@acolhesp.com';

-- =====================================================================
-- Senha das contas de teste: 123456 (bcrypt)
-- =====================================================================
UPDATE conta SET senha_hash = '$2b$10$s4FSy8E5OCXGmtFafS5YnOkTKgEJAD6M2Kel7qze9NghFtyiDiCN6'
WHERE email LIKE '%.teste@acolhesp.com';