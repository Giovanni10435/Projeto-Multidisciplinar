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
	}
}
