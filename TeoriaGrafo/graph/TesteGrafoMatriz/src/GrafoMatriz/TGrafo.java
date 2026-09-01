package GrafoMatriz;

//definição de uma estrutura Matriz de Adjacência para armezanar um grafo
public class TGrafo {
	// Atributos Privados
	private	int n; // quantidade de vértices
	private	int m; // quantidade de arestas
	private	int adj[][]; //matriz de adjacência
	// Métodos Públicos
	public TGrafo(int n) {  // construtor
	    this.n = n;
	    // No início dos tempos não há arestas
	    this.m = 0; 
	    // alocação da matriz do TGrafo
	    this.adj = new int[n][n];

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
	        m++; // atualiza qtd arestas
	    }
	}
	
	// remove uma aresta v->w do Grafo	
	public void removeA(int v, int w) {
	    // testa se temos a aresta
	    if(adj[v][w] == 1 ){
	        adj[v][w] = 0;
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
	    System.out.println("\n\nfim da impressao do grafo direcionado." );
	}

	// Ex 1)
	public int inDegree(int v) {
		int grau = 0;

		for (int i = 0; i < n; i++) {
			if (adj[i][v] == 1) {
				grau++;
			}
		}
		return grau;
	}

	// Ex 2)
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
		return inDegree(v) + outDegree(v);
	}

	// Ex 4)
	public int fonte(int v) {
		if (outDegree(v) > 0 && inDegree(v) == 0) {
			return 1;
		}
		return 0;
	}

	// Ex 5)
	public int sorvedouro(int v) {
		if (inDegree(v) > 0 && outDegree(v) == 0) {
			return 1;
		}
		return 0;
	}

	// Ex 13)
	public boolean isCompleto() {
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				if (i == j) continue; // ignora a diagonal
				if (adj[i][j] == 0) {
					return false; // falta a aresta i -> j
				}
			}
		}
		return true;
	}

	// Ex 14)
	public TGrafo complemento() {
		TGrafo g = new TGrafo(n);

		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				if (i == j) continue;
				if (adj[i][j] == 0) {
					g.insereA(i, j);
				}
			}
		}
		return g;
	}

	// Ex 16)
	public int conexidade() {
		if (isFortementeConexo()) return 3;
		if (isUnilateralmenteConexo()) return 2;
		if (isFracamenteConexo()) return 1;
		return 0;
	}

	// DFS genérica usando a Pilha. "inverso" = true percorre as arestas ao contrário (chegando em vez de saindo)
	private boolean[] dfs(int origem, boolean inverso) {
		boolean[] visitado = new boolean[n];
		Pilha pilha = new Pilha(n);
		pilha.push(origem);

		while (!pilha.isEmpty()) {
			int atual = pilha.pop();
			if (!visitado[atual]) {
				visitado[atual] = true;
				for (int w = 0; w < n; w++) {
					int existeAresta = inverso ? adj[w][atual] : adj[atual][w];
					if (existeAresta == 1 && !visitado[w]) {
						pilha.push(w);
					}
				}
			}
		}
		return visitado;
	}

	private boolean todosVisitados(boolean[] visitado) {
		for (boolean v : visitado) {
			if (!v) return false;
		}
		return true;
	}

	// C3: de 0 alcança todo mundo (indo) E todo mundo alcança 0 (voltando)
	private boolean isFortementeConexo() {
		return todosVisitados(dfs(0, false)) && todosVisitados(dfs(0, true));
	}

	// C1: ignorando o sentido das setas, dá pra alcançar todo mundo a partir de 0
	private boolean isFracamenteConexo() {
		boolean[] visitado = new boolean[n];
		Pilha pilha = new Pilha(n);
		pilha.push(0);

		while (!pilha.isEmpty()) {
			int atual = pilha.pop();
			if (!visitado[atual]) {
				visitado[atual] = true;
				for (int w = 0; w < n; w++) {
					if ((adj[atual][w] == 1 || adj[w][atual] == 1) && !visitado[w]) {
						pilha.push(w);
					}
				}
			}
		}
		return todosVisitados(visitado);
	}

	// C2: para todo par (i, j), existe caminho i->j OU j->i
	private boolean isUnilateralmenteConexo() {
		boolean[][] alcanca = new boolean[n][n];
		for (int i = 0; i < n; i++) {
			alcanca[i] = dfs(i, false); // quem o vértice i consegue alcançar
		}

		for (int i = 0; i < n; i++) {
			for (int j = i + 1; j < n; j++) {
				if (!alcanca[i][j] && !alcanca[j][i]) {
					return false; // nenhum dos dois alcança o outro - falha
				}
			}
		}
		return true;
	}


	// Ex 17)
	public TGrafo grafoReduzido() {
		// descobre quem alcança quem (mesma ideia do Ex 16)
		boolean[][] alcanca = new boolean[n][n];
		for (int i = 0; i < n; i++) {
			alcanca[i] = dfs(i, false);
		}

		// agrupa vértices na mesma componente fortemente conexa:
		// i e j estão no mesmo grupo se i alcança j E j alcança i
		int[] grupo = new int[n];
		java.util.Arrays.fill(grupo, -1);
		int totalGrupos = 0;

		for (int i = 0; i < n; i++) {
			if (grupo[i] == -1) {
				grupo[i] = totalGrupos;
				for (int j = i + 1; j < n; j++) {
					if (grupo[j] == -1 && alcanca[i][j] && alcanca[j][i]) {
						grupo[j] = totalGrupos;
					}
				}
				totalGrupos++;
			}
		}

		//monta o grafo reduzido: um vértice por grupo
		TGrafo reduzido = new TGrafo(totalGrupos);

		//para cada aresta original entre grupos DIFERENTES, cria aresta no reduzido
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				if (adj[i][j] == 1 && grupo[i] != grupo[j]) {
					reduzido.insereA(grupo[i], grupo[j]);
				}
			}
		}

		return reduzido;
	}

}