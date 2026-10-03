package GrafoLista;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import GrafoMatriz.TGrafo;
//definicao da classe de nós da lista
class TNo { // define uma struct (registro)
	public int w;  // vértice que é adjacente ao elemento da lista
	public TNo prox;
}

//definição de uma classe para armezanar um grafo
public class TGrafoList {
	// atributos privados
	private	int n; // quantidade de vértices
	private	int m; // quantidade de arestas
	private	TNo adj[]; // um vetor onde cada entrada guarda o inicio de uma lista
	// métodos públicos
	// Construtor do grafo com a lista de
	// adjacência
	public TGrafoList( int n ) {
	    // aloca a estrutura TGrafo
	    this.n = n;
	    this.m = 0;
	    // aloca m vetor para guardar lista de adjacencias
	    TNo adjac[] = new TNo[n];
	    // Inicia o vetor com nullL
		for(int i = 0; i< n; i++)
			adjac[i]=null;	
	    this.adj = adjac;
	};
	/*
	Método que cria uma aresta v-w no grafo. O método supõe que
	v e w são distintos, positivos e menores que V.
	Se o grafo já tem a aresta v-w, o método não faz nada.
	O método também atualiza a quantidade de arestas no grafo.
	*/
	public void insereA( int v, int w) {
		
	    TNo novoNo;
	    // anda na lista para chegar ao final
	    TNo no = adj[v];
	    TNo ant = null;
	    // anda na lista enquanto no != NULL E w  > no->w
	    while( no != null && w >= no.w ){
	        if( w == no.w)
	            return;
	        ant = no;
	        no = no.prox;
	    }
	    // cria o novo No para guardar w
	    novoNo = new TNo();
	    novoNo.w = w;
	    novoNo.prox = no;
	    // atualiza a lista
	    if( ant == null){
	        // insere no inicio
	        adj[v] = novoNo;
	    } else
	        // insere no final
	        ant.prox = novoNo;
	    m++;	
	}
	
	/*
	Método que remove do grafo a aresta que tem ponta inicial v
	e ponta final w. O método supõe que v e w são distintos,
	positivos e menores que V. Se não existe a aresta v-w,
	o método não faz nada. O método também atualiza a
	quantidade de arestas no grafo.
	*/
	public void removeA( int v, int w) {
	    // Obtém o início da lista do vértice v
	    TNo no = adj[v];
	    TNo ant = null;
	    // Percorre a lista do vértice v
	    // procurando w (se adjacente)
	    while( no != null && no.w != w ){
	    		ant = no;
	    		no = no.prox;
	    }
	    // Se w é adjacente, remove da lista de v
	    if (no != null){
	    	ant.prox = no.prox;
	    	no = null;
	    	m--;
		}	
	}
	/*
	Para cada vértice v do grafo, este método imprime, em
	uma linha, todos os vértices adjacentes ao vértice v
	(vizinhos ao vértice v).
	*/
	public void show() {
	    System.out.print("n: " + n);
	    System.out.print("\nm: " + m + "\n");
	    for( int i=0; i < n; i++){
	    	System.out.print("\n" + i + ": ");
	        // Percorre a lista na posição i do vetor
	        TNo no = adj[i];
	        while( no != null ){
	        	System.out.print(no.w + " ");
	            no = no.prox;
	        }
	    }
	    System.out.print("\n\nfim da impressao do grafo.\n");
	}

	// EX18) Método que calcula e retorna o grau de entrada de um vértice v de um grafo
	// dirigido
	public int inDegree(int v) {
		// inserindo o que seria o d-(x) por ser grafo dirigido (orientado)
		int dMinus = 0;

		for (int i = 0; i < n; i++) {
			TNo no = adj[i];
			
			while (no != null) {
				if (no.w == v) {
					dMinus += 1;
					break;
				}
				no = no.prox;
			}
		}
		return dMinus;
	}

	// EX19) Método que calcula e retorna o grau de saída de um vértice v de um grafo
	// dirigido
	public int outDegree(int v) {
		// inserindo o que seria o d-(x) por ser grafo dirigido (orientado)
		int dPlus = 0;

		TNo no = adj[v];
			
		while (no != null) {
			dPlus++;
			no = no.prox;
		}
		return dPlus;
	}

	// EX20) Médoto que calcula e retorna o grau do vértice de um grafo dirigido
	public int degree(int v) {
		return inDegree(v) + outDegree(v); // d(v) = d-(v) + d+(v)
	}

	// EX21) Método que decide se fois grafos direcionados são iguais (considerando ordenado na lista)
	public boolean iguais(TGrafoList a, TGrafoList b) {
        // Para serem IGUAIS:
        // V1 = V2 - Grafos devem possuir exatamente o mesmo conjunto finito de vértices
        // A1 = A2 - Grafos devem possuir exatamente o mesmo conjunto de arcos direcionados

        if (a.n == b.n && a.m == b.m) {
            for (int i = 0; i < a.n; i++) {
                TNo no_a = a.adj[i];
                TNo no_b = b.adj[i];
                while (no_a != null && no_b != null) {
                    if (no_a.w != no_b.w) {
                        return false;
                    }
                    no_a = no_a.prox;
                    no_b = no_b.prox;
                }
                if (no_a != null || no_b != null) {
                    return false;
                }
            }
        } else {
            return false;
        }
        return true;
    }

	// EX21) Método que decide se fois grafos direcionados são iguais (desnconsiderando ordenado na lista)
	// public boolean iguais(TGrafo a, TGrafo b) {
	// 	// Para serem IGUAIS:
	// 	// V1 = V2 - Grafos devem possuir exatamente o mesmo conjunto finito de vértices
	// 	// A1 = A2 - Grafos devem possuir exatamente o mesmo conjunto de arcos direcionados

	// 	if (a.n == b.n && a.m == b.m) {
	// 		for (int i = 0; i < a.n; i++) {
	// 			TNo no_a = a.adj[i];
	// 			TNo no_b = b.adj[i];

	// 			while (no_a != null) {
	// 				boolean encontrado = false;
	// 				while(no_b != null) {
	// 					if(no_a.w == no_b.w) {
	// 						encontrado = true;
	// 						break;
	// 					}
	// 					no_b = no_b.prox;
	// 				}
	// 				if (encontrado == false) {
	// 					return false;
	// 				}
	// 				no_a = no_a.prox;
	// 				no_b = b.adj[i];
	// 			}
	// 		}
	// 	} else {
	// 		return false;
	// 	}
	// 	return true;
	// }

	// EX22) Método que converte o grafo armazenado em matriz de adjacência em lista de adjacência
	public TGrafoList matriceToList(TGrafo a, TGrafoList grafo) {
		int tam = a.adj.length;

		for (int i = 0; i < tam; i++) {
			for (int j = 0; j < tam; j++) {
				if (a.adj[i][j] != Float.POSITIVE_INFINITY) { // Ta dando erro porque a matriz é private, ver se precisaria ser public
					grafo.insereA(i, j);
				}
			}	
		}
		return grafo;
	}

	// EX23) Método que recebe grafo armazenado em lista de adjacência e inverte a lista de todos os
	// vértices do grafo
	public TGrafoList inverteLista(TGrafoList a) {
		for (int i = 0; i < a.n; i++) {
			TNo ant = null;
			TNo no = a.adj[i];
			TNo prox;
			while (no != null) {
				prox = no.prox;
				no.prox = ant;
				ant = no;
				no = prox;
			}
			a.adj[i] = ant;
		}

		return a;
	}

	// EX24) Método que recebe grafo e um vértice como parâmetro e retorna 1 se vértice for uma
	// fonte (grau de saída maior que 0 e grau de entrada igual a 0), ou 0 caso contrário.
	public int ehFonte(TGrafoList a, int v) {
		if (a.outDegree(v) > 0 && a.inDegree(v) == 0) return 1;
		else return 0;
	}

	// EX25) Método que receba um grafo e um vértice como parâmetro, retorna 1 se vértice for uma
	// sorvedouro (grau de entrada maior que 0 e grau de saída igual a 0), ou 0 caso contrário.
	public int ehSorvedouro(TGrafoList a, int v) {
		if (a.inDegree(v) > 0 && a.outDegree(v) == 0) return 1;
		else return 0;
	}

	// EX26) Método que receba um grafo dirigido como parâmetro e retorna 1 se o grafo for simétrico
	// e 0 caso contrário.
	public int ehSimetrico(TGrafoList a) {
		TNo no;

		for (int i = 0; i < a.n; i++) {
			no = a.adj[i];
			while (no != null) {
				TNo no_2 = a.adj[no.w];
				int encontrado = 0; // Econtrado verifica se foi encontrado simetria

				while (no_2 != null) {
					if (no_2.w == i) {
						encontrado = 1;
						break;
					}
					no_2 = no_2.prox;
				}
				if (encontrado == 0) return 0;
				no = no.prox;
			}
		}
		return 1;
	}

	// EX27) Método que recebe nome de arquivo com formato do professor e construa a representação
	// da lista de adjacência do grafo
	public static TGrafoList buildGraph(String arq) {
		System.out.println(System.getProperty("user.dir"));
		try (BufferedReader br = new BufferedReader(new FileReader(arq))) {
			String linha;
			int contador = 0;
			int V = 0;
			int A = 0;
			TGrafoList grafo = null;

			while ((linha = br.readLine()) != null) {
				if (contador == 0) {
					V = Integer.parseInt(linha); // Numero de vértices
					grafo = new TGrafoList(V);
					contador++;
				} else if (contador == 1) {
					contador++; // Ignora numero de arestas já que insereA já conta numero de arestas
				} else {
					String[] valores = linha.split(" ");
					int v = Integer.parseInt(valores[0]);
					int w = Integer.parseInt(valores[1]);
					grafo.insereA(v, w);
				}
			}

			return grafo;

		} catch (IOException e) {
			System.out.print("Erro ao abrir o arquivo: " + e.getMessage());
		}

		return null;
	}

	// Ex 29) remove um vértice v do grafo DIRIGIDO, junto com todas as arestas associadas
	public TGrafoList removeVertice(int v) {
		int novoN = n - 1;
		TGrafoList novo = new TGrafoList(novoN);

		for (int i = 0; i < n; i++) {
			if (i == v) continue;
			int novoI = i > v ? i - 1 : i;

			TNo no = adj[i];
			while (no != null) {
				int w = no.w;
				if (w != v) {
					int novoW = w > v ? w - 1 : w;
					novo.insereA(novoI, novoW);
				}
				no = no.prox;
			}
		}

		this.n = novo.n;
		this.m = novo.m;
		this.adj = novo.adj;
		return this;
	}

	// Ex 30) verifica se o grafo (dirigido) é completo
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