# BACKEND

## Configuração do Ambiente Linux ⚒️

Abra um terminal na pasta onde está localizado o arquivo `pom.xml` e execute o seguinte comando:

```bash
mvn clean install
```
**Importante:** Caso queira rodar no Windows, necessário colocar o maven nas **Variáveis de Ambiente**.

## Configuração do Ambiente Windows ⚒️

Para configurar o **Apache Maven** nas Variáveis de Ambiente do Windows e fazer com que o comando `mvn` funcione no seu terminal, siga o passo a passo detalhado abaixo:

### Passo 1: Copiar o caminho do Maven
1. Navegue até a pasta onde você descompactou o Maven (por exemplo, `C:\Program Files\apache-maven-3.9.x`).
2. **Copie o caminho completo** dessa pasta principal na barra de endereços do Explorador de Arquivos.

### Passo 2: Abrir a tela de Variáveis de Ambiente
1. Pressione a tecla **Windows** no teclado.
2. Digite **"editar as variáveis de ambiente do sistema"** e clique na opção correspondente que aparecer.
3. Na janela que se abrir (*Propriedades do Sistema*), clique no botão **"Variáveis de Ambiente..."** localizado na parte inferior.

### Passo 3: Criar a variável `MAVEN_HOME`
1. Na seção **Variáveis do Sistema** (a metade inferior da tela), clique no botão **Novo...**.
2. Preencha os campos da seguinte forma:
   * **Nome da variável:** `MAVEN_HOME`
   * **Valor da variável:** Cole o caminho copiado no Passo 1 (exemplo: `C:\Program Files\apache-maven-3.9.x`).
3. Clique em **OK**.

### Passo 4: Adicionar ao `Path` do sistema
1. Ainda em **Variáveis do Sistema**, role a lista até encontrar a variável chamada **`Path`** (ou `PATH`).
2. Selecione-a e clique em **Editar...**.
3. Na nova janela, clique no botão **Novo** do lado direito.
4. Digite exatamente o seguinte texto: `%MAVEN_HOME%\bin`
5. Clique em **OK** na janela do Path, depois em **OK** na janela de Variáveis de Ambiente e em **OK** na de Propriedades do Sistema para salvar tudo.

---

### Passo 5: Validar a configuração
Para garantir que tudo foi configurado perfeitamente:

1. Feche todos os terminais que estiverem abertos para atualizar o sistema.
2. Abra um **novo terminal**.
3. Digite o comando abaixo e aperte Enter:
   ```bash
   mvn -version
   ```

Se a configuração estiver correta, o terminal exibirá informações detalhadas sobre a versão do Maven. Depois disso, você já poderá rodar o seu comando `mvn clean install` na pasta do seu projeto.

## Como Rodar a Aplicação Spring Boot ⚙️

Após realizar a build do projeto com sucesso, você pode iniciar a aplicação de duas formas utilizando o terminal:

### Usando o Plugin do Spring Boot
Abra o terminal na pasta raiz do projeto (onde está o arquivo `pom.xml`) e execute o seguinte comando:

```bash
mvn spring-boot:run
```

### Validando a Inicialização
Após executar qualquer um dos comandos acima, fique atento ao terminal:
* A aplicação estará rodando com sucesso quando você vir o logotipo do **Spring** em arte ASCII e a mensagem `Started [NomeDaAplicacao] in X seconds`.
* Por padrão, você poderá acessar a aplicação pelo navegador através do endereço: **`http://localhost:8080`**.

