package com.AcolheSP.demo.teoriagrafo.graphFinal.GrafoLista;

//definição de uma classe para armazenar um grafo não-dirigido em lista de adjacência
public class TGrafoListND {
	// atributos privados
	private int n; // quantidade de vértices
	private int m; // quantidade de arestas
	private TNo adj[]; // um vetor onde cada entrada guarda o início de uma lista

	public TGrafoListND(int n) {
		this.n = n;
		this.m = 0;
		TNo adjac[] = new TNo[n];
		for (int i = 0; i < n; i++) adjac[i] = null;
		this.adj = adjac;
	}

	// insere w na lista de v, sem mexer em m. Retorna true se inseriu de fato.
	private boolean inserirSentido(int v, int w) {
		TNo novoNo;
		TNo no = adj[v];
		TNo ant = null;
		while (no != null && w >= no.w) {
			if (w == no.w) return false; // já existe
			ant = no;
			no = no.prox;
		}
		novoNo = new TNo();
		novoNo.w = w;
		novoNo.prox = no;
		if (ant == null) adj[v] = novoNo;
		else ant.prox = novoNo;
		return true;
	}

	// insere a aresta v-w nos dois sentidos (v->w e w->v), atualizando m uma única vez
	public void insereA(int v, int w) {
		boolean inseriu = inserirSentido(v, w);
		if (inseriu) {
			inserirSentido(w, v);
			m++;
		}
	}

	// remove w da lista de v, sem mexer em m. Retorna true se removeu de fato.
	private boolean removerSentido(int v, int w) {
		TNo no = adj[v];
		TNo ant = null;
		while (no != null && no.w != w) {
			ant = no;
			no = no.prox;
		}
		if (no == null) return false; // não existe
		if (ant == null) adj[v] = no.prox;
		else ant.prox = no.prox;
		return true;
	}

	// remove a aresta v-w dos dois sentidos, atualizando m uma única vez
	public void removeA(int v, int w) {
		boolean removeu = removerSentido(v, w);
		if (removeu) {
			removerSentido(w, v);
			m--;
		}
	}

	public void show() {
		System.out.print("n: " + n);
		System.out.print("\nm: " + m + "\n");
		for (int i = 0; i < n; i++) {
			System.out.print("\n" + i + ": ");
			TNo no = adj[i];
			while (no != null) {
				System.out.print(no.w + " ");
				no = no.prox;
			}
		}
		System.out.print("\n\nfim da impressao do grafo nao direcionado.\n");
	}

	// Ex 28) remove um vértice v do grafo NÃO-DIRIGIDO, junto com todas as arestas associadas
	public void removeVertice(int v) {
		int novoN = n - 1;
		TGrafoListND novo = new TGrafoListND(novoN);

		for (int i = 0; i < n; i++) {
			if (i == v) continue; // pula o vértice removido
			int novoI = i > v ? i - 1 : i; // reindexa: quem vinha depois de v anda uma posição pra trás

			TNo no = adj[i];
			while (no != null) {
				int w = no.w;
				if (w != v) { // ignora arestas que iam pro vértice removido
					int novoW = w > v ? w - 1 : w;
					novo.insereA(novoI, novoW); // insereA já evita duplicar (a aresta aparece 2x, uma em cada lista)
				}
				no = no.prox;
			}
		}

		this.n = novo.n;
		this.m = novo.m;
		this.adj = novo.adj;
	}

	// Ex 30) verifica se o grafo (não-dirigido) é completo
	public boolean isCompleto() {
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				if (i == j) continue;
				if (!existeAresta(i, j)) return false;
			}
		}
		return true;
	}

	private boolean existeAresta(int v, int w) {
		TNo no = adj[v];
		while (no != null) {
			if (no.w == w) return true;
			no = no.prox;
		}
		return false;
	}
}