package GrafoLista;

public class TesteGrafoLista {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
	    // Cria um objeto para um Grafo com 4 vértices
	    // Armazendo em uma lista de adjacência
	    TGrafo g = new TGrafo(4);
	    //insere as arestas do grafo
	    //A={(0,1),(0,2),(0,3),(2,1),(2,3),(1,3)}
	    g.insereA(0,1);
	    g.insereA(0,2);
	    g.insereA(0,3);
	    g.insereA(2,1);
	    g.insereA(2,3);
	    g.insereA(1,3);
	    // mostra o grafo preenchido
	    g.show();
	    g.removeA(0,3);  // remove a aresta (0,3)
	    // mostra o grafo preenchido
	    System.out.print("\nDepois da remoção da aresta (0,3)\n");
	    g.show();
	}

}
