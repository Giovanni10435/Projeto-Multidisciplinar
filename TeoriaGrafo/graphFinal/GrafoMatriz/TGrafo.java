package GrafoMatriz;
import FFilaCircular.FilaCircular;
import PPilha.Pilha;

//definição de uma estrutura Matriz de Adjacência para armezanar um grafo
public class TGrafo {
	// Atributos Privados
	private	int n; // quantidade de vértices
	private	int m; // quantidade de arestas
	public	float adj[][]; //matriz de adjacência
	// Métodos Públicos
	public TGrafo(int n) {  // construtor
	    this.n = n;
	    // No início dos tempos não há arestas
	    this.m = 0; 
	    // alocação da matriz do TGrafo
	    this.adj = new float[n][n];

		// Inicializa todas as posições com infinito
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                this.adj[i][j] = Float.POSITIVE_INFINITY;
            }
		}
	}

	// Insere uma aresta no Grafo tal que
	// v é adjacente a w
	public void insereA(int v, int w, float valor) {
	    // testa se nao temos a aresta
	    if(adj[v][w] == Float.POSITIVE_INFINITY){
	        adj[v][w] = valor;
	        m++; // atualiza qtd arestas
	    }
	}
	
	// remove uma aresta v->w do Grafo	
	public void removeA(int v, int w) {
	    // testa se temos a aresta
	    if(adj[v][w] != Float.POSITIVE_INFINITY){
	        adj[v][w] = Float.POSITIVE_INFINITY;
	        m--; // atualiza qtd arestas
	    }
	}
	// Apresenta o Grafo contendo
	// número de vértices, arestas
	// e a matriz de adjacência obtida	
	public void show() {
	    System.out.println("n: " + n );
	    System.out.println("m: " + m );
	    for(int i=0; i < n; i++){
	    	System.out.print("\n");
	        for(int w=0; w < n; w++)
	            if(adj[i][w] != Float.POSITIVE_INFINITY)
	            	System.out.print("Adj[" + i + "," + w + "]= " + adj[i][w] + " ");
	            else System.out.print("Adj[" + i + "," + w + "]= INF ");
	    }
	    System.out.println("\n\nfim da impressao do grafo direcionado." );
	}

	// Ex 1)
	public int inDegree(int v) {
		int grau = 0;

		for (int i = 0; i < n; i++) {
			if (adj[i][v] != Float.POSITIVE_INFINITY) {
				grau++;
			}
		}
		return grau;
	}

	// Ex 2)
	public int outDegree(int v) {
		int grau = 0;

		for (int i = 0; i < n; i++) {
			if (adj[v][i] != Float.POSITIVE_INFINITY) {
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


	// Ex 6)
	public int is_Simetrico() {
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++){
				if(adj[i][j] != adj[j][i]) {
					return 0;
				} 
			}
		}
		return 1;
	}

	// Ex 7)
	public static TGrafo lerArquivo(String nomeArquivo) throws Exception {
		java.util.Scanner entrada = new java.util.Scanner(new java.io.File(nomeArquivo));

		int n = entrada.nextInt();
		int quantidadeArestas = entrada.nextInt();

		TGrafo g = new TGrafo(n);

		for (int i = 0; i < quantidadeArestas; i++) {
			int v = entrada.nextInt();
			int w = entrada.nextInt();
			g.insereA(v, w, 1.0f);
		}
		entrada.close();
		return g;
	}

	// Ex 11)
	public void removeVertice(int v) {
		int novoN = n - 1;
		float novaAdj[][] = new float[novoN][novoN];
		int novoM = 0;
		int linhaAtual = 0;
		for (int i = 0; i < n; i++) {
			if (i == v) continue;
			int colunaAtual = 0;

			for (int j = 0; j < n; j++) {
				if (j == v) continue;
				novaAdj[linhaAtual][colunaAtual] = adj[i][j];
				if (adj[i][j] != Float.POSITIVE_INFINITY) novoM++;
				colunaAtual++;
			}
			linhaAtual++;
		}
		this.n = novoN;
		this.m = novoM;
		this.adj = novaAdj;
	}
	// Ex 13)
	public boolean isCompleto() {
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				if (i == j) continue; // ignora a diagonal
				if (adj[i][j] == Float.POSITIVE_INFINITY) {
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
				if (adj[i][j] == Float.POSITIVE_INFINITY) {
					g.insereA(i, j, 1.0f);
				}
			}
		}
		return g;
	}

	// Ex 16)
	public int conexidade() {
		if (isFortementeConexo()) return 3;
		if (isSemiFortementeConexo()) return 2;
		if (isSimplesmenteConexo()) return 1;
		return 0;
	}

	// DFS usando Pilha
	public void dfs(int vInicio, boolean[] marcado, boolean invertido) {
		int nroNosMarcados = 0;
		int[] nosMarcados = new int [n];

		for (int i = 0; i < n; i++) {
			nosMarcados[i] = -1;
		}

		int k, l;

		Pilha p = new Pilha(n);

		System.out.println("Vértice visitado: " + vInicio);
		nroNosMarcados = marcarNo(nosMarcados, nroNosMarcados, vInicio);
		marcado[vInicio] = true;

		p.push(vInicio);

		while (!p.isEmpty()) {
			k = p.pop();

			// Existe nó adjacente a n ainda não marcado
			while ((l = noAdjacente(k, nosMarcados, invertido)) != -1) {
				System.out.println("Vértice visitado: " + l);
				p.push(k);
				nroNosMarcados = marcarNo(nosMarcados, nroNosMarcados, l);
				marcado[l] = true;

				k = l;
			}
		}
	}

	// BFS usando lista
	public void bfs(int vInicio, boolean invertido) {
		int nroNosMarcados = 0;
		int[] nosMarcados = new int [this.n];
		int k, l;
		FilaCircular f = new FilaCircular();

		marcarNo(nosMarcados, nroNosMarcados, vInicio);

		System.out.println("Vértice visitado: " + vInicio);
		f.enqueue(vInicio);

		while (!f.qIsEmpty()) {
			k = f.dequeue();

			// Existe nó adjacente a n ainda não marcado
			while ((l = noAdjacente(k, nosMarcados, invertido)) != -1) {
				System.out.println("Vértice visitado: " + l);
				f.enqueue(l);
				nroNosMarcados = marcarNo(nosMarcados, nroNosMarcados, l);
			}
		}
	}

	private int marcarNo(int[] nosMarcados, int nroNosMarcados, int no) {
		nosMarcados[nroNosMarcados] = no;
		return nroNosMarcados + 1;
	}

	private int noAdjacente(int no, int[] nosMarcados, boolean invertido) {
		for (int i = 0; i < n; i++) {

			boolean existeAresta;

			if (!invertido) {
				existeAresta = adj[no][i] != Float.POSITIVE_INFINITY;
			} else {
				existeAresta = adj[i][no] != Float.POSITIVE_INFINITY;
			}
			// Verifica se aquele nó é vazio e se o numero de nos já marcados é 0 (não foi marcado ainda)
			// Caso seja, retorna o nó
			if (existeAresta && !estaMarcado(nosMarcados, i)) {
				return i;
			}
		}
		return -1;
	}

	private boolean estaMarcado(int[] nosMarcados, int no) {
		for (int i = 0; i < n; i++) {
			if (nosMarcados[i] == no) {
				return true;
			}
		}
		return false;
	}

	private boolean todosVisitados(boolean[] marcado) {
		for (boolean v : marcado) {
			if (!v) return false;
		}
		return true;
	}

	// C3: de 0 alcança todo mundo (indo) E todo mundo alcança 0 (voltando)
	private boolean isFortementeConexo() {
		System.out.println("f-conexo:");

		boolean[] vaiIndo = new boolean[n];
		dfs(0, vaiIndo, false);

		boolean[] vaiVoltando = new boolean[n];
		dfs(0, vaiVoltando, true);

		return todosVisitados(vaiIndo) && todosVisitados(vaiVoltando);
	}

	// C1: ignorando o sentido das setas, dá pra alcançar todo mundo a partir de 0
	private boolean isSimplesmenteConexo() {
		System.out.println("s-conexo:");
		boolean[] marcado = new boolean[n];
		Pilha p = new Pilha(n);
		marcado[0] = true;
		p.push(0);

		while (!p.isEmpty()) {
			int atual = p.pop();
			int m = proximoNaoMarcadoIgnorandoDirecao(atual, marcado);
			while (m != -1) {
				marcado[m] = true;
				p.push(atual);
				atual = m;
				m = proximoNaoMarcadoIgnorandoDirecao(atual, marcado);
			}
		}
		return todosVisitados(marcado);
	}

	private int proximoNaoMarcadoIgnorandoDirecao(int v, boolean[] marcado) {
    for (int w = 0; w < n; w++) {
        boolean temAresta = adj[v][w] != Float.POSITIVE_INFINITY || adj[w][v] != Float.POSITIVE_INFINITY;
        if (temAresta && !marcado[w]) return w;
    }
    return -1;
}
	// C2: para TODO par (i, j), existe caminho i->j OU j->i
	private boolean isSemiFortementeConexo() {
		System.out.println("Semi-Fortemente conexo:");
		boolean[][] alcanca = new boolean[n][n];

		for (int i = 0; i < n; i++) {
			boolean[] marcado = new boolean[n];
			dfs(i, marcado, false);
			for (int j = 0; j < n; j++) {
				alcanca[i][j] = marcado[j];
			}
		}

		for (int i = 0; i < n; i++) {
			for (int j = i + 1; j < n; j++) {
				if (!alcanca[i][j] && !alcanca[j][i]) {
					return false;
				}
			}
		}
		return true;
	}

	// Ex 17)
	public TGrafo grafoReduzido() {
		boolean[][] alcanca = new boolean[n][n];
		for (int i = 0; i < n; i++) {
			boolean[] marcado = new boolean[n];
			dfs(i, marcado, false);
		}

		int[] grupo = new int[n];
		for (int i = 0; i < n; i++) {
			grupo[i] = -1;
		}
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

		TGrafo reduzido = new TGrafo(totalGrupos);

		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				if (adj[i][j] != Float.POSITIVE_INFINITY && grupo[i] != grupo[j]) {
					reduzido.insereA(grupo[i], grupo[j], adj[i][j]);
				}
			}
		}

		return reduzido;
	}

}