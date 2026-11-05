# Backend AcolheSP

API REST em Java 21 / Spring Boot, com controllers, services transacionais,
repositories JPA e PostgreSQL. As implementações desta revisão estão somente
em `backend/`; não há mudanças no frontend, no BFF nem no banco em produção.

## Issues abertas analisadas

Repositório: `Giovanni10435/Projeto-Multidisciplinar`. Base da revisão:
`2f48dd19f8471c8e70431fb3e14d3d928cc1de01` (`main`).

| Issue | Implementação no backend |
| --- | --- |
| [#12](https://github.com/Giovanni10435/Projeto-Multidisciplinar/issues/12) Autenticação | Login BCrypt, sessão Spring Security, CSRF, consulta da conta atual, logout, autorização por conta e instituição. |
| [#32](https://github.com/Giovanni10435/Projeto-Multidisciplinar/issues/32) Login de abrigo | Autentica a conta existente e retorna `instituicaoId`; conta de abrigo sem vínculo recebe 403. Nenhum novo cadastro público de abrigo foi criado. |
| [#33](https://github.com/Giovanni10435/Projeto-Multidisciplinar/issues/33) Necessidades e voluntariado | CRUD de necessidades protegido por titularidade; CRUD persistente dos horários, com validação de dia, função e intervalo. |
| [#34](https://github.com/Giovanni10435/Projeto-Multidisciplinar/issues/34) Candidaturas e doações | Candidatura geral ou ligada a horário; validação de disponibilidade e duplicidade; compromisso de doação respeita o saldo; entrega contabilizada uma vez. |

Essas issues têm apenas título (com exceção da #32), sem comentários ou
critérios adicionais. Para o voluntariado do abrigo, a implementação segue os
campos já presentes na interface: dia da semana, função, início e fim.
As issues não foram fechadas no GitHub e nenhum push foi feito.

## Banco existente e execução

A configuração de produção continua usando o PostgreSQL existente e
`spring.jpa.hibernate.ddl-auto=none`. Antes de iniciar esta versão, aplique
**uma vez**, com as credenciais do banco de destino, o script incremental:

```bash
# Na raiz do repositório; DATABASE_URL deve apontar para o banco desejado.
psql "$DATABASE_URL" -v ON_ERROR_STOP=1 -f backend/db/001_horarios_voluntariado.sql
```

Ele cria `horario_voluntariado` e adiciona a FK opcional `voluntariado.horario_id`.
As candidaturas antigas continuam válidas com `horario_id = NULL`. O script
não recria nem remove as tabelas existentes. Não é executado automaticamente
pela aplicação e não foi aplicado no banco de produção nesta revisão.

Depois:

```bash
cd backend/demo
mvn test
mvn spring-boot:run
```

A API usa a porta 8080. Configure o PostgreSQL pelas propriedades existentes
ou por `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME` e
`SPRING_DATASOURCE_PASSWORD`. Para HTTPS em produção, configure também
`SERVER_SERVLET_SESSION_COOKIE_SECURE=true`.

## Contrato de autenticação

1. Faça `GET /api/auth/csrf`. Preserve o cookie `JSESSIONID` e leia `headerName`
   e `token` do JSON retornado.
2. Envie `POST /api/auth/login` com o mesmo cookie, o token no header informado
   e `{"email":"...","senha":"..."}`. O backend troca o ID da sessão no login.
3. Preserve o cookie atualizado. Escritas autenticadas continuam exigindo
   cookie e header CSRF. Obtenha um novo token após login, logout ou troca de sessão.
4. `GET /api/auth/me` consulta a conta atual; `POST /api/auth/logout`, com CSRF,
   invalida a sessão e responde 204. A sessão expira após 30 minutos de inatividade.

O retorno do login mantém `id`, `email`, `role`, `nome` e `instituicaoId`.
Não retorna senha nem hash. E-mails são comparados sem diferenciar maiúsculas
e minúsculas, e novos cadastros são normalizados. Senhas informadas no cadastro
continuam exigindo ao menos seis caracteres, com limite BCrypt de 72 bytes UTF-8.
Credenciais incorretas recebem 401; conta inativa com senha válida recebe 403.

Consultas públicas: catálogo e detalhes de abrigos, mapa, necessidades e
horários de voluntariado. Cadastro de voluntário e login são públicos, mas
exigem CSRF. As demais operações exigem sessão. Um voluntário só pode agir
com seu próprio ID, e um gestor só pode administrar sua instituição. Uma
conta desativada perde o acesso às operações protegidas mesmo com sessão já
aberta. O cadastro já existente de abrigo ficou restrito a administradores.

**Integração com BFF/frontend:** o BFF atual não encaminha sessão e CSRF entre
o navegador e o backend. Para usar os endpoints protegidos através dele,
será necessário encaminhar `Cookie`, `Set-Cookie` e o header CSRF, além de
expor os endpoints de CSRF, sessão e horários. Isso não foi alterado nesta
revisão por estar fora da pasta autorizada. Os fluxos foram testados diretamente
na API do backend, não ponta a ponta no navegador. Não há fallback inseguro
que permita executar uma operação apenas enviando IDs ou roles no JSON.

## Rotas e regras

### Horários de voluntariado

Base: `/api/instituicoes/{instituicaoId}/horarios-voluntariado`.

- `GET`: lista horários ativos, ordenados por dia, início e ID. Consulta pública.
- `POST`: cria horário; requer o gestor do abrigo. Retorna 201 e `Location`.
- `PUT /{horarioId}`: substitui os campos do horário ativo; mesmo gestor.
- `DELETE /{horarioId}`: desativa o horário, preservando o histórico. Retorna 204.

Exemplo de corpo para criar/alterar:

```json
{"dia":"Sábado","funcao":"Cozinha","inicio":"08:00","fim":"12:00"}
```

Dias válidos: Segunda-feira, Terça-feira, Quarta-feira, Quinta-feira,
Sexta-feira, Sábado e Domingo. A função deve ter 1 a 150 caracteres, e o fim
precisa ser posterior ao início no mesmo dia.

### Candidaturas

`POST /api/instituicoes/{id}/voluntariados` mantém o contrato anterior e aceita
`horarioId` (ou `horario_id`) opcional. O `voluntarioId` deve ser o da sessão.
Horários de outro abrigo ou desativados são rejeitados. A resposta inclui
`horarioId`, além dos campos anteriores.

```json
{"voluntarioId":123,"horarioId":45,"dataInicio":"2026-11-14","telefone":"11999999999"}
```

A candidatura nasce PENDENTE. Uma candidatura geral ativa impede uma segunda
candidatura geral no mesmo abrigo; com horário, impede duplicação no mesmo
horário. Horários diferentes aceitam candidaturas distintas. PENDENTE,
APROVADO e EM_ANDAMENTO contam como ativos. Contato/disponibilidade informados
atualizam o cadastro. Datas invertidas e campos acima dos limites são rejeitados.

### Necessidades, doações e solicitações

As rotas existentes foram mantidas:

- `GET/POST /api/instituicoes/{id}/necessidades` e
  `PUT/DELETE /api/instituicoes/{id}/necessidades/{necessidadeId}`.
- `POST /api/doacoes`: `voluntarioId`, `necessidadeId` e `quantidade`.
- `GET /api/instituicoes/{id}/solicitacoes` e
  `PUT /api/instituicoes/{id}/solicitacoes/{solicitacaoId}`.

A quantidade disponível para novos compromissos é a solicitada menos a recebida
e menos as doações PENDENTES/CONFIRMADAS. Prometer acima desse saldo responde
409. Cancelamento/recusa libera o saldo, e apenas a entrega soma na quantidade
recebida. O gestor não pode reduzir a meta abaixo do recebido e comprometido.
Uma necessidade com doações não pode ser apagada: deve ser encerrada, mantendo
o histórico. Os conflitos de integridade são retornados como 409 sem expor SQL.

Fluxos de status:

- Doação: PENDENTE -> CONFIRMADA -> ENTREGUE; PENDENTE/CONFIRMADA podem ser
  CANCELADAS. É necessário confirmar antes de registrar a entrega.
- Voluntariado: PENDENTE -> APROVADO -> EM_ANDAMENTO -> FINALIZADO.
  PENDENTE pode ser RECUSADO; estados ativos podem ser CANCELADOS.
- Estados finais não podem ser reabertos. Os aliases existentes de status
  (APROVADA, RECUSADA, CONCLUÍDA, etc.) continuam aceitos.

Bloqueios transacionais serializam promessas do mesmo saldo, candidaturas no
mesmo abrigo e alterações de status. Duas confirmações simultâneas de entrega
não somam a mesma doação duas vezes; entregas distintas não perdem atualizações.

## Testes

```bash
cd backend/demo
mvn test
# Se todas as dependências já estiverem no cache:
mvn -o test
# Recompilar, testar e gerar o JAR sem artefatos anteriores:
mvn -o clean package
```

A suíte atual contém 177 testes: 152 existentes, 18 testes de fluxos da API,
quatro testes de concorrência e três de configuração/migração do banco.

Sem variáveis externas, os testes usam H2 em modo PostgreSQL, com criação e
descarte do esquema de teste. No GitHub Actions, as variáveis
`SPRING_DATASOURCE_*` selecionam o PostgreSQL de testes e
`SPRING_JPA_HIBERNATE_DDL_AUTO=none` preserva o esquema inicializado pelo workflow.
O driver é inferido pela URL; não há driver H2 fixo para uma URL PostgreSQL.

A migração de `backend/db/` é incluída somente no classpath dos testes e
executada depois da inicialização do Hibernate. Assim, o banco de testes
também recebe `horario_voluntariado` e `voluntariado.horario_id`, sem mudar
o workflow nem a inicialização do banco de produção. Use somente bancos
descartáveis para rodar a suíte com variáveis externas.

A suíte inclui
os testes existentes e testes de integração com filtros de segurança, login
real, CSRF real, titularidade, horários, candidaturas, doações e concorrência
com transações independentes. O Mockito é carregado como agente no início da
JVM para não depender de autoanexação em ambientes restritos.

Relatórios: `backend/demo/target/surefire-reports/` e
`backend/demo/target/site/jacoco/index.html`.
Os testes de regressão verificam a seleção do driver com as mesmas variáveis
do Actions e aplicam a migração real a um esquema anterior mínimo em H2.
A execução em PostgreSQL real e o script incremental ainda precisam ser
validados nesse ambiente; H2 não substitui essa verificação de implantação.
