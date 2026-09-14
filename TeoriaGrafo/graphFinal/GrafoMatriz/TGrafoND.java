package GrafoMatriz;
import PPilha.Pilha;

//definição de uma estrutura Matriz de Adjacência para armezanar um grafo
public class TGrafoND {
	// Atributos Privados
	private	int n; // quantidade de vértices
	private	int m; // quantidade de arestas
	private	int adj[][]; //matriz de adjacência
	// Métodos Públicos
	public TGrafoND(int n) {  // construtor
	    this.n = n;
	    // No início dos tempos não há arestas
	    this.m = 0; 
	    // alocação da matriz do TGrafoND
	    this.adj = new int [n][n];

	    // Inicia a matriz com zeros
		for(int i = 0; i< n; i++)
			for(int j = 0; j< n; j++)
				this.adj[i][j]=0;	
	}

	// Insere uma aresta no Grafo tal que
	// v é adjacente a w
	public void insereA(int v, int w) {
	    // testa se nao temos a aresta
	    if(adj[v][w] == 0 ){
	        adj[v][w] = 1;
            // Ex 8)
            adj[w][v] = 1; // Duplica a atualização na matriz de adjacencia
	        m++; // atualiza qtd arestas
	    }
	}
	
	// remove uma aresta v->w do Grafo	
	public void removeA(int v, int w) {
	    // testa se temos a aresta
	    if(adj[v][w] == 1 ){
	        adj[v][w] = 0;
            // Ex 8)
            adj[w][v] = 0; // Duplica a atualização na matriz de adjacencia
	        m--; // atualiza qtd arestas
	    }
	}
	// Apresenta o Grafo contendo
	// número de vértices, arestas
	// e a matriz de adjacência obtida	
	public void show() {
	    System.out.println("n: " + n );
	    System.out.println("m: " + m );
	    for( int i=0; i < n; i++){
	    	System.out.print("\n");
	        for( int w=0; w < n; w++)
	            if(adj[i][w] == 1)
	            	System.out.print("Adj[" + i + "," + w + "]= 1" + " ");
	            else System.out.print("Adj[" + i + "," + w + "]= 0" + " ");
	    }
	    System.out.println("\n\nfim da impressao do grafo nao direcionado." );
	}

    
	// Ex 9)
	public int inDegree(int v) {
		int grau = 0;

		for (int i = 0; i < n; i++) {
			if (adj[i][v] == 1) {
				grau++;
			}
		}
		return grau;
	}

	// Ex 9)
	public int outDegree(int v) {
		int grau = 0;

		for (int i = 0; i < n; i++) {
			if (adj[v][i] == 1) {
				grau++;
			}
		}
		return grau;
	}

	// Ex 3)
	public int Degree(int v) {
		return outDegree(v);
	}

	// Ex 11)
	public void removeVertice(int v) {
		int novoN = n - 1;
		int novaAdj[][] = new int[novoN][novoN];
		int novoM = 0;

		int linhaAtual = 0;
		for (int i = 0; i < n; i++) {
			if (i == v) continue;
			int colunaAtual = 0;
			for (int j = 0; j < n; j++) {
				if (j == v) continue;
				novaAdj[linhaAtual][colunaAtual] = adj[i][j];
				colunaAtual++;
			}
			linhaAtual++;
		}

		// conta arestas certo: percorre só metade da matriz (acima da diagonal),
		// já que cada aresta não-dirigida aparece em 2 células simétricas
		for (int i = 0; i < novoN; i++) {
			for (int j = i + 1; j < novoN; j++) {
				if (novaAdj[i][j] == 1) novoM++;
			}
		}

		this.n = novoN;
		this.m = novoM;
		this.adj = novaAdj;
	}


	
	// Ex 12)
	public boolean isCompleto() {
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				if (i == j) continue; // ignora a diagonal (vértice não tem aresta com ele mesmo)
				if (adj[i][j] == 0) {
					return false; // achou um par de vértices SEM aresta - não é completo
				}
			}
		}
		return true; // percorreu tudo e não achou nenhum par faltando - é completo
	}

	// Ex 14)
	public TGrafoND complemento() {
		TGrafoND g = new TGrafoND(n); // grafo novo, mesmo tamanho, começa vazio

		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				if (i == j) continue; // nunca cria laço
				if (adj[i][j] == 0) {
					g.insereA(i, j); // não tinha aresta aqui -> no complemento, tem
				}
			}
		}
		return g;
	}

	// Ex 15)
	public int conexidade() {
		boolean[] marcado = new boolean[n];
		dfs(0, marcado);

		for (int i = 0; i < n; i++) {
			if (!marcado[i]) return 1; // desconexo
		}
		return 0; // conexo
	}

	// DFS usando Pilha
	private void dfs(int vInicio, boolean[] marcado) {
		Pilha p = new Pilha(n);
		marcado[vInicio] = true;
		p.push(vInicio);

		while (!p.isEmpty()) {
			int atual = p.pop();
			int m = proximoNaoMarcado(atual, marcado);
			while (m != -1) {
				marcado[m] = true;
				p.push(atual);
				atual = m;
				m = proximoNaoMarcado(atual, marcado);
			}
		}
	}

	// procura um vizinho de v ainda não marcado (retorna -1 se não achar)
	private int proximoNaoMarcado(int v, boolean[] marcado) {
		for (int w = 0; w < n; w++) {
			if (adj[v][w] == 1 && !marcado[w]) return w;
		}
		return -1;
	}
}