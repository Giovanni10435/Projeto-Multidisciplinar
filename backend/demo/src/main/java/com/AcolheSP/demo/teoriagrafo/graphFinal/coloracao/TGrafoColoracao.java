package com.AcolheSP.demo.teoriagrafo.graphFinal.coloracao;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Grafo não direcionado por listas de adjacência, usado na coloração gulosa por classes. */
class TNo {
    public int w;
    public TNo prox;
    public float pesoAres;
}

public class TGrafoColoracao {
    private int n;
    private int m;
    private TNo[] adj;
    private String[] rotulo;
    private String[] pesoVert;

    public TGrafoColoracao(int n) {
        if (n < 0) throw new IllegalArgumentException("Número de vértices inválido");
        this.n = n;
        this.m = 0;
        this.adj = new TNo[n];
        this.rotulo = new String[n];
        this.pesoVert = new String[n];
    }

    /** Insere uma aresta não direcionada; uma aresta duplicada é ignorada. */
    public void insereA(int v, int w, float peso) {
        validarVertice(v);
        validarVertice(w);
        if (v == w) return;
        if (existeAresta(v, w)) return;
        inserirSentido(v, w, peso);
        inserirSentido(w, v, peso);
        m++;
    }

    private void inserirSentido(int origem, int destino, float peso) {
        TNo atual = adj[origem];
        TNo anterior = null;
        while (atual != null && destino > atual.w) {
            anterior = atual;
            atual = atual.prox;
        }
        TNo novo = new TNo();
        novo.w = destino;
        novo.pesoAres = peso;
        novo.prox = atual;
        if (anterior == null) adj[origem] = novo;
        else anterior.prox = novo;
    }

    private boolean existeAresta(int origem, int destino) {
        TNo atual = adj[origem];
        while (atual != null && atual.w <= destino) {
            if (atual.w == destino) return true;
            atual = atual.prox;
        }
        return false;
    }

    /** Insere novo vértice e devolve o índice associado. */
    public int insereV(String peso, String rotulo) {
        TNo[] novoAdj = new TNo[n + 1];
        String[] novoRotulo = new String[n + 1];
        String[] novoPesoVert = new String[n + 1];
        System.arraycopy(adj, 0, novoAdj, 0, n);
        System.arraycopy(this.rotulo, 0, novoRotulo, 0, n);
        System.arraycopy(this.pesoVert, 0, novoPesoVert, 0, n);
        novoRotulo[n] = rotulo;
        novoPesoVert[n] = peso;
        n++;
        adj = novoAdj;
        this.rotulo = novoRotulo;
        this.pesoVert = novoPesoVert;
        return n - 1;
    }

    private void insereVArquivo(int v, String coordenadas, String rotulo) {
        if (v < 0 || v >= n) throw new IllegalArgumentException("Índice de vértice inválido: " + v);
        this.rotulo[v] = rotulo;
        this.pesoVert[v] = coordenadas;
    }

    /**
     * Executa a coloração gulosa por classes, visitando primeiro vértices de maior grau.
     * Cada lista retornada contém os índices dos vértices que receberam a mesma cor.
     */
    public List<List<Integer>> coloracaoPorClasse() {
        if (n == 0) return List.of();

        TColoracao cores = new TColoracao(n);
        int[] ordem = new int[n];
        for (int i = 0; i < n; i++) ordem[i] = i;
        ordenaPorGrau(ordem);

        int cor = 0;
        while (temVerticeNaoColorido(ordem)) {
            for (int i = 0; i < n; i++) {
                int vertice = ordem[i];
                if (vertice == -1) continue;
                if (!temVizinhoNaCor(vertice, cores, cor)) {
                    cores.insereVerticeCor(cor, vertice);
                    ordem[i] = -1;
                }
            }
            cor++;
        }
        cores.numCores = cor;

        List<List<Integer>> grupos = new ArrayList<>(cor);
        for (int k = 0; k < cor; k++) {
            List<Integer> grupo = new ArrayList<>();
            TNoColoracao atual = cores.getCores(k);
            while (atual != null) {
                grupo.add(atual.vertice);
                atual = atual.prox;
            }
            grupos.add(List.copyOf(grupo));
        }
        return List.copyOf(grupos);
    }

    /** Método console mantido para o teste acadêmico. */
    public void coloracao_classe() {
        List<List<Integer>> grupos = coloracaoPorClasse();
        for (int k = 0; k < grupos.size(); k++) {
            System.out.print("Cor " + (k + 1) + ": {");
            List<Integer> grupo = grupos.get(k);
            for (int i = 0; i < grupo.size(); i++) {
                int vertice = grupo.get(i);
                String nome = rotulo[vertice] == null ? String.valueOf(vertice) : rotulo[vertice];
                System.out.print((i == 0 ? " " : ", ") + nome);
            }
            System.out.println(" }");
        }
    }

    private void ordenaPorGrau(int[] ordem) {
        for (int i = 0; i < ordem.length - 1; i++) {
            for (int j = i + 1; j < ordem.length; j++) {
                if (grau(ordem[j]) > grau(ordem[i])) {
                    int aux = ordem[i];
                    ordem[i] = ordem[j];
                    ordem[j] = aux;
                }
            }
        }
    }

    private int grau(int vertice) {
        int grau = 0;
        for (TNo atual = adj[vertice]; atual != null; atual = atual.prox) grau++;
        return grau;
    }

    private boolean temVerticeNaoColorido(int[] ordem) {
        for (int vertice : ordem) if (vertice != -1) return true;
        return false;
    }

    private boolean temVizinhoNaCor(int vertice, TColoracao cores, int cor) {
        TNo atual = adj[vertice];
        while (atual != null) {
            TNoColoracao membro = cores.getCores(cor);
            while (membro != null) {
                if (atual.w == membro.vertice) return true;
                membro = membro.prox;
            }
            atual = atual.prox;
        }
        return false;
    }

    private void validarVertice(int vertice) {
        if (vertice < 0 || vertice >= n) throw new IllegalArgumentException("Índice de vértice inválido: " + vertice);
    }

    /** Leitor simples para os arquivos de exercício: V, dados dos vértices, M, arestas. */
    public static TGrafoColoracao buildGraph(String arquivo) {
        try (BufferedReader br = new BufferedReader(new FileReader(arquivo))) {
            String linha = br.readLine();
            if (linha == null) return null;
            int quantidadeVertices = Integer.parseInt(linha.trim());
            TGrafoColoracao grafo = new TGrafoColoracao(quantidadeVertices);

            for (int lido = 0; lido < quantidadeVertices; lido++) {
                linha = br.readLine();
                if (linha == null) throw new IllegalArgumentException("Arquivo terminou antes dos vértices");
                String[] valores = linha.split("\"");
                if (valores.length < 4) throw new IllegalArgumentException("Linha de vértice inválida: " + linha);
                int vertice = Integer.parseInt(valores[0].trim().split("\\s+")[0]);
                grafo.insereVArquivo(vertice, valores[3], valores[1]);
            }

            linha = br.readLine();
            if (linha == null) throw new IllegalArgumentException("Arquivo não informa a quantidade de arestas");
            int quantidadeArestas = Integer.parseInt(linha.trim());
            for (int lido = 0; lido < quantidadeArestas; lido++) {
                linha = br.readLine();
                if (linha == null) throw new IllegalArgumentException("Arquivo terminou antes das arestas");
                String[] valores = linha.trim().split("\\s+");
                if (valores.length < 3) throw new IllegalArgumentException("Linha de aresta inválida: " + linha);
                grafo.insereA(Integer.parseInt(valores[0]), Integer.parseInt(valores[1]), Float.parseFloat(valores[2]));
            }
            return grafo;
        } catch (IOException | NumberFormatException e) {
            throw new IllegalArgumentException("Não foi possível ler o grafo em '" + arquivo + "': " + e.getMessage(), e);
        }
    }
}
