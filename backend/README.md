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

---

## Hello World TG2: rodar e testar (Codespaces ou local) 🚀

Stack do teste: **Vue (5173) → Spring Boot (8080) → PostgreSQL (5432)**.

No **Codespaces**, Java, Node e Docker já vêm do `.devcontainer`. Use 3 terminais (ícone **+** do painel do terminal):

- **T1:** banco e teste (fica livre)
- **T2:** backend (fica rodando)
- **T3:** frontend (fica rodando)

### 1. Ligar o banco (T1, na raiz do projeto)

```bash
docker compose up -d
docker ps
```
Esperado: container `postgres:16` com status **Up**.

### 2. Ligar o backend (T2)

```bash
cd backend/demo
chmod +x mvnw
./mvnw spring-boot:run
```
Esperado: `Started DemoApplication in X seconds`. Deixe aberto.

> Aqui usamos `./mvnw` (Maven Wrapper). Não precisa instalar o Maven.

### 3. Ligar o frontend (T3)

```bash
cd frontend
npm install
npm run dev
```
Esperado: `VITE ready` e `Local: http://localhost:5173/`. Deixe aberto.


### Problema: `CMD.EXE` / `UNC` / `vite não é reconhecido` / `EISDIR` (WSL)

**Sintoma:** ao rodar `npm install` ou `npm run dev` no WSL aparece `CMD.EXE foi iniciado...`, `caminhos UNC`, `EISDIR` ou `'vite' não é reconhecido`.

**Causa:** o terminal está usando o npm do **Windows**, e não o do Linux.

**Solução:**

1. Trocar para o npm do Linux:
```bash
source ~/.nvm/nvm.sh
nvm use 22
nvm alias default 22
which npm
```
Esperado: o caminho tem `.nvm` (nada de `/mnt/c/...`).

2. Limpar e reinstalar:
```bash
cd frontend
rm -rf node_modules
npm install
npm run dev
```
Esperado: `VITE ready`, sem o aviso do `CMD.EXE`.

3. Se o `which npm` ainda mostrar `/mnt/c/...`, rode uma vez e abra um terminal novo:
```bash
echo 'export NVM_DIR="$HOME/.nvm"; [ -s "$NVM_DIR/nvm.sh" ] && . "$NVM_DIR/nvm.sh"; nvm use 22 >/dev/null' >> ~/.bashrc
```
Depois repita o passo 2.

**Node não instalado no WSL:**
```bash
curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.40.3/install.sh | bash
source ~/.bashrc
nvm install 22
```

### 4. Acessar no navegador

**Backend direto (API):**

- Local: `http://localhost:8080/api/hello-world`
- Codespaces: aba **PORTS** > porta **8080** > ícone do globo. Na URL que abrir, acrescente `/api/hello-world` no final.

Esperado: `{"mensagem":"Hello World do AcolheSP, direto do PostgreSQL!"}`

**Tela (frontend):**

- Local: `http://localhost:5173`
- Codespaces: aba **PORTS** > porta **5173** > ícone do globo.

Esperado: **Conectado com Sucesso** e a mensagem do banco.

### 5. Teste único (passa pelo front, back e banco)

No **T1**, na raiz do projeto:

```bash
docker compose exec database psql -U app -d acolhesp -c "update mensagem set texto = 'Hello World alterado direto no banco';"
```
Esperado: `UPDATE 1`.

Agora, na **tela**, clique em **Testar Conexão Novamente**.

Esperado:

- **Front:** a tela mostra **Hello World alterado direto no banco**.
- **Back (T2):** aparece o log abaixo.
- **Banco:** o texto lido é o que você acabou de gravar.

```
[1/3] Requisicao recebida do frontend em /api/hello-world
Hibernate: select m1_0.id,m1_0.texto from mensagem m1_0
[2/3] Mensagem lida do PostgreSQL: Hello World alterado direto no banco
[3/3] Respondendo ao frontend em JSON
```

### 6. Desligar

1. T3: `Ctrl+C`
2. T2: `Ctrl+C`
3. T1: `docker compose stop`

Não use `docker compose down -v` (apaga os dados do banco).

### Se der erro

| Erro | Faça |
|---|---|
| `Connection refused 5432` no backend | `docker compose up -d` na raiz |
| `Cannot connect to the Docker daemon` | `sudo service docker start` |
| `Permission denied` no `mvnw` | `chmod +x mvnw` |
| `curl`/navegador na 8080 dá 401 | Conferir se existe `SecurityConfig.java` |
| Tela: "Erro de Comunicação" | Subir o backend (T2) |
| `sh: vite: Permission denied` | `cd frontend && rm -rf node_modules && npm install` |
| `Blocked request. This host is not allowed` (Codespaces) | No `frontend/vite.config.js`, dentro de `server`, adicionar `allowedHosts: true` e reiniciar o `npm run dev` |
| Nada aparece no log do backend | Clicar no botão da tela (o log só aparece quando chega requisição) |
| Porta ocupada | `pkill -f vite` ou `pkill -f spring-boot` |