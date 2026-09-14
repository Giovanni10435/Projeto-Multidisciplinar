# Modelo Conceitual - AcolheSP

---

## Proposições

1. O usuário pode buscar por instituições de apoio em regiões próximas a ele utilizando sua localização atual.
2. O sistema apresenta as instituições em formato de lista e em um mapa geográfico.
3. O usuário pode consultar informações sobre a instituição, como local, formas de contato e necessidades atuais.
4. As instituições podem se cadastrar e registrar suas solicitações de produtos e recursos necessários.
5. O usuário pode consultar as necessidades de uma instituição.
6. Instituições podem cadastrar oportunidades de voluntariado, informando dias e horários disponíveis.
7. A instituição pode atualizar o status de uma necessidade após receber uma doação, retirando-a da lista de necessidades disponíveis.

---

## Questionamentos Sistemáticos

### Quem são os usuários do sistema?

* Pessoas que desejam realizar doações ou voluntariado.
* Responsáveis por abrigos e instituições de apoio.

### Como o usuário encontra instituições próximas?

* Por meio da localização atual ou de uma busca manual por região/endereço.

### Quais informações das instituições são importantes?

* Nome
* Endereço
* Contato
* Serviços oferecidos
* Necessidades
* Oportunidades de voluntariado

### Como garantir que as informações das instituições sejam confiáveis?

* Os cadastros devem passar por um processo de validação.

### Como os abrigos divulgam suas necessidades?

* Cadastrando os produtos necessários, suas quantidades e o status da necessidade.

### Como o usuário sabe o que pode doar?

* Consultando as necessidades atuais de cada instituição.

### Como as oportunidades de voluntariado são divulgadas?

* A instituição informa a atividade, os dias, horários e quantidade de voluntários necessários.

### Como garantir que o sistema seja fácil de usar?

* Utilizando uma interface simples, linguagem clara, botões destacados e poucos passos para realizar cada ação.

### O que acontece quando uma necessidade é atendida?

* A instituição atualiza o status e a necessidade deixa de aparecer como pendente.

### O usuário precisa compartilhar sua localização?

* Não necessariamente. O sistema também pode oferecer uma alternativa de busca manual.

### Como o usuário pode escolher entre diferentes instituições?

* O sistema pode permitir filtros por distância, tipo de serviço oferecido e necessidades.
* O sistema também fornece uma área de busca no topo superior esquerdo.

### Como o usuário sabe quais instituições estão mais próximas?

* O mapa pode apresentar a distância aproximada entre o usuário e cada instituição.

### Como evitar que uma instituição receba doações de produtos que não necessita mais?

* As instituições devem manter suas necessidades atualizadas e marcar os pedidos como atendidos quando necessário.

### Como o responsável pelo abrigo saberá que alguém deseja ser voluntário?

* O sistema deve registrar o interesse do usuário e disponibilizar seus dados de contato para a instituição, conforme as regras de privacidade.

### O que acontece caso o usuário não encontre uma instituição adequada?

* O sistema pode permitir ampliar a área de busca ou utilizar filtros diferentes para encontrar outras instituições.

### Como o sistema pode facilitar o acesso para pessoas com pouca experiência tecnológica?

* A interface deve possuir navegação simples, textos objetivos, ícones compreensíveis, instruções claras e feedback das ações realizadas.

---

# GOMS

## Metas (Goals)

### Para o usuário

* Encontrar uma instituição próxima que possa ajudar com doações ou como voluntário.

### Para as instituições

* Fornecer as necessidades e buscar por doadores e voluntários para apoio da instituição.

---

## Operadores

### Acessar o AcolheSP

#### Usuário

* Permitir registrar suas informações.
* Visualização em mapa.
* Selecionar uma instituição.
* Visualizar suas necessidades.
* Informar interesse na doação.
* Realizar doação.

#### Instituições

* Acessar o sistema.
* Cadastrar a instituição.
* Preencher os dados do abrigo.
* Cadastrar uma necessidade.
* Informar produto e quantidade.
* Publicar a necessidade.
* Editar uma necessidade.
* Marcar uma necessidade como atendida.
* Cadastrar uma oportunidade de voluntariado.
* Definir dias e horários.
* Visualizar interessados.
* Atualizar informações da instituição.

---

# Métodos

## Usuário

### Método 1: Encontrar instituição próxima

1. Acessar o AcolheSP.
2. Permitir acesso à localização ou fornecer endereço para busca manual.
3. Visualizar instituições próximas.
4. Selecionar a instituição.
5. Visualizar as informações.

### Método 2: Identificar uma Necessidade

1. Acessar a área de necessidades da instituição.
2. Visualizar os produtos solicitados.
3. Escolher um produto que deseja doar.
4. Selecionar a opção de realizar a doação.
5. Confirmar o interesse.

### Método 3: Cadastrar para voluntário

1. Acessar a área de voluntariados.
2. Visualizar datas e horários disponíveis.
3. Visualizar as tarefas que serão realizadas.
4. Selecionar a opção para se voluntariar.
5. Confirmar interesse.

---

## Instituição

### Método 1: Registrar necessidades

1. Acessar o AcolheSP.
2. Selecionar a opção de **"Registrar necessidades"**.
3. Registrar os itens necessários ou em falta.
4. Registrar a quantidade de itens necessários.
5. Realizar a **"postagem"**.

### Método 2: Registrar Voluntários

1. Acessar o AcolheSP.
2. Selecionar a opção de **"Registrar voluntários"**.
3. Selecionar datas disponíveis.
4. Selecionar os horários.
5. Escrever a descrição das tarefas e funções.
6. Realizar a **"postagem"**.

---

# Regras de Seleção

## Usuário

**SE** usuário buscar instituição de apoio

**ENTÃO** execute o método 1.

**SE** usuário for realizar doação

**ENTÃO** execute o método 2.

**SE** usuário for ajudar como voluntário

**ENTÃO** execute o método 3.

## Instituição de apoio

**SE** instituição necessita informar produtos e recursos necessários

**ENTÃO** execute o método 1.

**SE** instituição necessita de voluntários

**ENTÃO** execute o método 2.
