package br.com.vidros.modelo;

import java.io.Serializable;

public class Vidro implements Serializable {

    // === 1. Identificação Única (Para Filtragem por Obra/Lista) ===

    // Campo Chave para organização
    private String nomeObra;

    // Identifica lista que origina o vidro
    private String listaOrigem;

    // ID único -> Obra + Posição + Dimensões
    private String idItemUnico;

    // === 2. Especificações (Dados da Lista) ===

    private String posicao;
    private String tipoEsquadria;
    private String especificacao;
    private int larguraMM;  // A lista sempre vem com números inteiros
    private int alturaMM;
    private int quantidadeTotal; // Quantidade por peça

    // === 3. Controle e Status (Novas Funcionalidades) ===

    // A. Rastreamento de Chegada na Fábrica
    private int qtdChegouFabrica; // Quantidade recebida (Dar Entrada)
    private String nfeEntrada;  // Número da NFE (para rastreio)

    // B. Rastreamento de Produção (Corte/Usinagem)
    private int qtdCortada;     // Quantidade em Baixa (Dar Baixa no Corte)

    // C. Status Calculado
    private String statusGeral; // PENDENTE, EM FALTA, COMPLETO, EM CORTE

    // Constructor Geral
    public Vidro(String nomeObra, String listaOrigem, String idItemUnico, String posicao,
                 String tipoEsquadria, String especificacao, int larguraMM, int alturaMM, int quantidadeTotal,
                 int qtdChegouFabrica, String nfeEntrada, int qtdCortada, String statusGeral) {
        this.nomeObra = nomeObra;
        this.listaOrigem = listaOrigem;
        this.idItemUnico = idItemUnico;
        this.posicao = posicao;
        this.tipoEsquadria = tipoEsquadria;
        this.especificacao = especificacao;
        this.larguraMM = larguraMM;
        this.alturaMM = alturaMM;
        this.quantidadeTotal = quantidadeTotal;
        this.qtdChegouFabrica = qtdChegouFabrica;
        this.nfeEntrada = nfeEntrada;
        this.qtdCortada = qtdCortada;
        this.statusGeral = statusGeral;
    }

    // Getters
    public String getNomeObra() {
        return nomeObra;
    }

    public String getListaOrigem() {
        return listaOrigem;
    }

    public String getIdItemUnico() {
        return idItemUnico;
    }

    public String getPosicao() {
        return posicao;
    }

    public String getTipoEsquadria() {
        return tipoEsquadria;
    }

    public String getEspecificacao() {
        return especificacao;
    }

    public int getLarguraMM() {
        return larguraMM;
    }

    public int getAlturaMM() {
        return alturaMM;
    }

    public int getQuantidadeTotal() {
        return quantidadeTotal;
    }

    public int getQtdChegouFabrica() {
        return qtdChegouFabrica;
    }

    public String getNfeEntrada() {
        return nfeEntrada;
    }

    public int getQtdCortada() {
        return qtdCortada;
    }

    public String getStatusGeral() {
        return statusGeral;
    }

    // Setters
    public void setQtdChegouFabrica(int qtdChegouFabrica) {
        this.qtdChegouFabrica = qtdChegouFabrica;
    }

    public void setNfeEntrada(String nfeEntrada) {
        this.nfeEntrada = nfeEntrada;
    }

    public void setQtdCortada(int qtdCortada) {
        this.qtdCortada = qtdCortada;
    }

    // Função para definir Status
    public void calcularStatus() {
        if (qtdChegouFabrica < quantidadeTotal) {
            this.statusGeral = "FALTA MATERIAL (" + (quantidadeTotal - qtdChegouFabrica) + " unid.)";
        } else if (qtdCortada == quantidadeTotal) {
            this.statusGeral = "FINALIZADO";
        } else if (qtdCortada > 0 && qtdCortada < quantidadeTotal) {
            this.statusGeral = "EM PRODUÇÃO";
        } else {
            this.statusGeral = "AGUARDANDO CORTE";
        }
    }

    // Função para definir o ID ÚNICO
    public void gerarIdUnico() {
        this.idItemUnico = this.nomeObra.toUpperCase().replaceAll("\\s", "_") + "_" + this.posicao.trim()
                + "_" + this.larguraMM + "x" + this.alturaMM;
    }

    // Metódo para exibir as especificações do vidro
    @Override
    public String toString() {
        return String.format("Obra: %s | Lista: %s | Posição: %s | Especificação: %s | Status: %s", nomeObra,
                listaOrigem, posicao, especificacao, statusGeral);
    }

    public Vidro(String nomeObra, String listaOrigem, String posicao,
                 String especificacao, int larguraMM, int alturaMM,
                 int quantidadeTotal) {

        // Define os dados vindos da planilha
        this.nomeObra = nomeObra;
        this.listaOrigem = listaOrigem;
        this.posicao = posicao;
        this.especificacao = especificacao;
        this.larguraMM = larguraMM;
        this.alturaMM = alturaMM;
        this.quantidadeTotal = quantidadeTotal;

        // Define os valores padrão para os campos de controle (os outros 6 campos)
        this.qtdChegouFabrica = 0;
        this.qtdCortada = 0;
        this.nfeEntrada = "";

        // Chama os métodos que calculam o ID e o Status inicial
        gerarIdUnico();
        calcularStatus();
    }
}
