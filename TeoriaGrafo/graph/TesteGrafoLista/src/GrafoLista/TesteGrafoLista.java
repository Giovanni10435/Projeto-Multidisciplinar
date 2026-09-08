package GrafoLista;

public class TesteGrafoLista {

    public static void main(String[] args) {

        // TODO Auto-generated method stub

        // Cria um objeto para um Grafo com 4 vértices
        // Armazendo em uma lista de adjacência
        TGrafo g = new TGrafo(4);

        //GrafoMatriz.TGrafo matriz = new GrafoMatriz.TGrafo(4);

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


        // =====================================================
        // TESTE DO inDegree
        // =====================================================

        System.out.println("\n\nTeste inDegree:");

        System.out.println("inDegree(0): " + g.inDegree(0));
        System.out.println("inDegree(1): " + g.inDegree(1));
        System.out.println("inDegree(2): " + g.inDegree(2));
        System.out.println("inDegree(3): " + g.inDegree(3));


        // =====================================================
        // TESTE DO outDegree
        // =====================================================

        System.out.println("\nTeste outDegree:");

        System.out.println("outDegree(0): " + g.outDegree(0));
        System.out.println("outDegree(1): " + g.outDegree(1));
        System.out.println("outDegree(2): " + g.outDegree(2));
        System.out.println("outDegree(3): " + g.outDegree(3));


        // =====================================================
        // TESTE DO degree
        // =====================================================

        System.out.println("\nTeste degree:");

        System.out.println("degree(0): " + g.degree(0));
        System.out.println("degree(1): " + g.degree(1));
        System.out.println("degree(2): " + g.degree(2));
        System.out.println("degree(3): " + g.degree(3));


        // =====================================================
        // TESTE DO iguais
        // =====================================================

        System.out.println("\nTeste iguais:");

        TGrafo g2 = new TGrafo(4);

        g2.insereA(0,1);
        g2.insereA(0,2);
        g2.insereA(2,1);
        g2.insereA(2,3);
        g2.insereA(1,3);

        System.out.println("g e g2 são iguais? " + g.iguais(g, g2));


        TGrafo g3 = new TGrafo(4);

        g3.insereA(0,1);
        g3.insereA(0,2);
        g3.insereA(0,3);
        g3.insereA(2,1);
        g3.insereA(2,3);
        g3.insereA(1,3);

        System.out.println("g e g3 são iguais? " + g.iguais(g, g3));


        // =====================================================
        // TESTE DO ehFonte
        // =====================================================

        System.out.println("\nTeste ehFonte:");

        System.out.println("0 é fonte? " + g.ehFonte(g, 0));
        System.out.println("1 é fonte? " + g.ehFonte(g, 1));
        System.out.println("2 é fonte? " + g.ehFonte(g, 2));
        System.out.println("3 é fonte? " + g.ehFonte(g, 3));


        // =====================================================
        // TESTE DO ehSorvedouro
        // =====================================================

        System.out.println("\nTeste ehSorvedouro:");

        System.out.println("0 é sorvedouro? " + g.ehSorvedouro(g, 0));
        System.out.println("1 é sorvedouro? " + g.ehSorvedouro(g, 1));
        System.out.println("2 é sorvedouro? " + g.ehSorvedouro(g, 2));
        System.out.println("3 é sorvedouro? " + g.ehSorvedouro(g, 3));


        // =====================================================
        // TESTE DO ehSimetrico
        // =====================================================

        System.out.println("\nTeste ehSimetrico:");

        System.out.println("g é simétrico? " + g.ehSimetrico(g));


        TGrafo g4 = new TGrafo(4);

        g4.insereA(0,1);
        g4.insereA(1,0);

        g4.insereA(0,2);
        g4.insereA(2,0);

        g4.insereA(1,3);
        g4.insereA(3,1);

        System.out.println("g4 é simétrico? " + g4.ehSimetrico(g4));


        // =====================================================
        // TESTE DO buildGraph
        // =====================================================

        System.out.println("\nTeste buildGraph:");

        TGrafo g5 = TGrafo.buildGraph("grafo.txt");

        if (g5 != null) {
            System.out.println("Grafo construído pelo arquivo:");
            g5.show();
        }
    }
}