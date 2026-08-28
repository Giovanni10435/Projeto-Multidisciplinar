## UC01 - Consultar locais de assistência
**Ator:** Pessoa interessada em ajudar
**O que faz:** usuário busca abrigos cadastrados na plataforma, podendo filtrar por região ou tipo de necessidade
**Inclui:** UC03 (mostra os resultados no mapa), UC02 (ao selecionar um abrigo da lista)

## UC02 - Visualizar informações do abrigo
**Ator:** Pessoa interessada em ajudar
**O que faz:** exibe os dados completos do abrigo (endereço, contato, serviços oferecidos, ocupação)
**Inclui:** UC04 (mostra as necessidades atuais do abrigo)

## UC03 - Localizar abrigo no mapa
**Ator:** Pessoa interessada em ajudar
**O que faz:** mostra os abrigos num mapa interativo, ordenados por proximidade (usa Dijkstra no grafo de distâncias)

## UC04 - Consultar necessidades
**Ator:** Pessoa interessada em ajudar
**O que faz:** lista o que o abrigo está precisando no momento (alimento, roupa, higiene, voluntário etc), com quantidade e urgência

## UC05 - Realizar doação
**Ator:** Pessoa interessada em ajudar
**O que faz:** usuário registra a doação de um item para suprir uma necessidade específica do abrigo

## UC06 - Demonstrar interesse em voluntariado
**Ator:** Pessoa interessada em ajudar
**O que faz:** usuário se candidata como voluntário de um abrigo, informando contato e disponibilidade

## UC07 - Cadastrar abrigo
**Ator:** Administrador do abrigo
**O que faz:** administrador registra um novo abrigo/instituição na plataforma

## UC08 - Atualizar informações do abrigo
**Ator:** Administrador do abrigo
**O que faz:** administrador edita dados de um abrigo já cadastrado (endereço, capacidade, serviços)

## UC09 - Cadastrar necessidade
**Ator:** Administrador do abrigo
**O que faz:** administrador registra uma nova necessidade (item, recurso ou vaga de voluntariado) do abrigo

## UC10 - Atualizar necessidade
**Ator:** Administrador do abrigo
**O que faz:** administrador edita quantidade/urgência de uma necessidade ou marca como suprida

## UC11 - Gerenciar solicitações de produtos
**Ator:** Administrador do abrigo
**O que faz:** administrador acompanha e aprova/recusa as doações e manifestações de interesse recebidas