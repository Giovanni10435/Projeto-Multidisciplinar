package GrafoMatriz;

public class TesteGrafoMatriz {
	public static void main(String args[]) {
		//  chama o construtor para criar um grafo 4x4
		TGrafo gD = new TGrafo(4);
		TGrafoND gND = new TGrafoND(4);
		//insere as arestas do grafo
		//A={(0,1),(0,2),(2,1),(2,3),(1,3)}
		
		gD.insereA(0,1);
		gD.insereA(0,2);
		gD.insereA(2,1);
		gD.insereA(2,3);
		gD.insereA(1,3);
		// mostra o grafo direcionado preenchido
		gD.show();

		// Ex 1)
		System.out.println("\nGrau de entrada do Vértice 1: " + gD.inDegree(1) + "\n");
		// Ex 2)
		System.out.println("Grau de saída do Vértice 1: " + gD.outDegree(1) + "\n");
		// Ex 3)
		System.out.println("Grau total do Vértice 1: " + gD.Degree(1) + "\n");

		if (gD.fonte(1) == 1) {
			System.out.println("O Vértice é uma fonte!\n");
		} else {
			System.out.println("O Vértice não é uma fonte!\n");
		}

		if (gD.sorvedouro(1) == 1) {
			System.out.println("O Vértice é sorvedouro!\n");
		} else {
			System.out.println("O Vértice não é sorvedouro!\n");
		}
		

		gND.insereA(0,1);
		gND.insereA(0,2);
		gND.insereA(2,1);
		gND.insereA(2,3);
		gND.insereA(1,3);
		// mostra o grafo nao direcionado preenchido
		gND.show();

		// Ex 9)
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
  }
}