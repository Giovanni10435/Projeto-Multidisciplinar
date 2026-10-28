package com.AcolheSP.demo.teoriagrafo.graphFinal.coloracao;

class TNoColoracao {
    public int vertice;
    public TNoColoracao prox;

    TNoColoracao(int vertice) {
        this.vertice = vertice;
    }
}

/** Estrutura de listas ligadas que guarda os vértices atribuídos a cada cor. */
public class TColoracao {
    private final TNoColoracao[] cores;
    public int numCores;

    public TColoracao(int quantidadeVertices) {
        if (quantidadeVertices < 0) throw new IllegalArgumentException("Quantidade de vértices inválida");
        cores = new TNoColoracao[quantidadeVertices];
    }

    public void insereVerticeCor(int cor, int vertice) {
        if (cor < 0 || cor >= cores.length) throw new IllegalArgumentException("Cor inválida: " + cor);
        TNoColoracao novo = new TNoColoracao(vertice);
        novo.prox = cores[cor];
        cores[cor] = novo;
    }

    public TNoColoracao getCores(int cor) {
        if (cor < 0 || cor >= cores.length) throw new IllegalArgumentException("Cor inválida: " + cor);
        return cores[cor];
    }
}
