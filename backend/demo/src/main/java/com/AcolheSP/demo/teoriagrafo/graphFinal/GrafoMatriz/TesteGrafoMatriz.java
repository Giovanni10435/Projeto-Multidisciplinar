package com.AcolheSP.demo.teoriagrafo.graphFinal.GrafoMatriz;

public class TesteGrafoMatriz {
	public static void main(String args[]) throws Exception {
		//  chama o construtor para criar um grafo 4x4
		TGrafo gD = new TGrafo(4);
		TGrafoND gND = new TGrafoND(4);
		//insere as arestas do grafo
		//A={(0,1),(0,2),(2,1),(2,3),(1,3)}
		
		// Atualização do Ex 6)
		// O grafo é Valorado / Ponderado
		gD.insereA(0,1,5.5f);
		gD.insereA(1,0,5.5f);
		gD.insereA(0,2,2.0f);
		gD.insereA(2,0,2.0f);
		gD.insereA(2,1,3.2f);
		gD.insereA(1,2,3.2f);
		gD.insereA(1,3,7.0f);
		gD.insereA(3,1,7.0f);
		// mostra o grafo direcionado preenchido
		gD.show();

		// Ex 1)
		System.out.println("\nGrau de entrada do Vértice 1: " + gD.inDegree(1) + "\n");
		// Ex 2)
		System.out.println("Grau de saída do Vértice 1: " + gD.outDegree(1) + "\n");
		// Ex 3)
		System.out.println("Grau total do Vértice 1: " + gD.Degree(1) + "\n");

		// Ex 4)
		if (gD.fonte(1) == 1) {
			System.out.println("O Vértice é uma fonte!\n");
		} else {
			System.out.println("O Vértice não é uma fonte!\n");
		}

		// Ex 5)
		if (gD.sorvedouro(1) == 1) {
			System.out.println("O Vértice é sorvedouro!\n");
		} else {
			System.out.println("O Vértice não é sorvedouro!\n");
		}
		
		// Ex 6)
		if (gD.is_Simetrico() == 1) {
			System.out.println("O grafo é simétrico!\n");
		} else {
			System.out.println("O grafo não é simétrico!\n");
		}

		// Ex 7)

		System.out.println("\n--- Ex 7: leitura do arquivo ---");
		TGrafo gArquivo = TGrafo.lerArquivo("grafo.txt");
		gArquivo.show();
         
		//Exc 8
		System.out.println("\n--- Ex 8: grafo não-dirigido (TGrafoND) ---");
		gND.insereA(0,1);
		gND.insereA(0,2);
		gND.insereA(2,1);
		gND.insereA(2,3);
		gND.insereA(1,3);
		// mostra o grafo nao direcionado preenchido
		gND.show();

		// Ex 9)
		System.out.println("\n--- Ex 9: graus do vértice 1 (TGrafoND) ---");
		System.out.println("\nGrau de entrada do Vértice 1: " + gND.inDegree(1) + "\n");
		System.out.println("Grau de saída do Vértice 1: " + gND.outDegree(1) + "\n");
		System.out.println("Grau total do Vértice 1: " + gND.Degree(1) + "\n");
	

			// =====test 11 a 17 =====

		// Ex 12 - completo? (não-dirigido)
		System.out.println("\n--- Ex 12/13: completo ---");
		System.out.println("gND é completo? " + gND.isCompleto());
		System.out.println("gD é completo? " + gD.isCompleto());

		// Ex 14 - complemento
		System.out.println("\n--- Ex 14: complemento ---");
		TGrafoND compND = gND.complemento();
		compND.show();
		TGrafo compD = gD.complemento();
		compD.show();

		// Ex 15 - conexidade não-dirigida
		System.out.println("\n--- Ex 15: conexidade (ND) ---");
		System.out.println("Resultado (0=conexo, 1=desconexo): " + gND.conexidade());

		// Ex 16 - conexidade dirigida
		System.out.println("\n--- Ex 16: conexidade (D) ---");
		System.out.println("Categoria (0..3): " + gD.conexidade());

		// Ex 17 - grafo reduzido
		System.out.println("\n--- Ex 17: grafo reduzido ---");
		TGrafo reduzido = gD.grafoReduzido();
		reduzido.show();

		// Ex 11 - remove vértice (por último, porque muda o grafo)
		System.out.println("\n--- Ex 11: remove vértice ---");
		gD.removeVertice(2);
		System.out.println("gD depois de remover vértice 2:");
		gD.show();

		gND.removeVertice(2);
		System.out.println("gND depois de remover vértice 2:");
		gND.show();

        // Teste nao valorado
		System.out.println("\n--- Teste não valorado ---");
		TGrafo gNaoValorado = new TGrafo(4);

		gNaoValorado.insereA(0, 1);
		gNaoValorado.insereA(0, 2);

		gNaoValorado.show();



		// =====================================================
		// Grafos obrigatórios do exercício de Busca (BFS/DFS)
		// =====================================================

		System.out.println("\n=== G10 (dirigido) - a=0, b=1, c=2, d=3, e=4, f=5, g=6, h=7 ===");
		TGrafo g10 = new TGrafo(8);
		g10.insereA(0, 1); // a->b
		g10.insereA(0, 2); // a->c
		g10.insereA(0, 4); // a->e
		g10.insereA(1, 3); // b->d
		g10.insereA(1, 4); // b->e
		g10.insereA(2, 5); // c->f
		g10.insereA(2, 6); // c->g
		g10.insereA(5, 4); // f->e
		g10.insereA(5, 6); // f->g
		g10.insereA(3, 7); // d->h
		g10.insereA(4, 7); // e->h
		g10.insereA(6, 7); // g->h
		g10.show();

		System.out.println("\n--- DFS em G10 a partir de a (0) ---");
		g10.dfs(0, new boolean[8], false);

		System.out.println("\n--- BFS em G10 a partir de a (0) ---");
		g10.bfs(0, false);

		System.out.println("\n=== G9 (não-dirigido, representado com aresta nos dois sentidos) - a=0, b=1, c=2, d=3 ===");
		TGrafo g9 = new TGrafo(4);
		g9.insereA(0, 1); g9.insereA(1, 0); // a-b
		g9.insereA(0, 2); g9.insereA(2, 0); // a-c
		g9.insereA(1, 3); g9.insereA(3, 1); // b-d
		g9.insereA(2, 3); g9.insereA(3, 2); // c-d
		g9.show();

		System.out.println("\n--- DFS em G9 a partir de a (0) ---");
		g9.dfs(0, new boolean[4], false);

		System.out.println("\n--- BFS em G9 a partir de a (0) ---");
		g9.bfs(0, false);
  }

}