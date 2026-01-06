package br.com.vidros.modelo;

import java.io.Serializable;

public class Vidro implements Serializable {

    // === 1. Identificação Única (Para Filtragem por Obra/Lista) ===
    private String nomeObra;
    private String listaOrigem;
    private String idItemUnico;

    // === 2. Especificações (Dados da Lista) ===

    private String posicao;
    private String tipologia;
    private String especificacao;

    private int larguraMM;
    private int alturaMM;

    // === 3. Controle e Status (Novas Funcionalidades) ===

    private int quantidadeTotal; // Quantidade por peça

    // A. Rastreamento de Chegada na Fábrica
    private int qtdChegouFabrica; // Quantidade recebida (Dar Entrada)

    // B. Rastreamento de Produção (Corte/Usinagem)
    private int qtdCortada;     // Quantidade em Baixa (Dar Baixa no Corte)

    // C. Sistema de Reposioção (Acidentes)
    private int qtdReposicao;   // Vidros que vieram ou foram quebrados

    // D. Status Calculado
    private String statusGeral; // PENDENTE, EM FALTA, COMPLETO, EM CORTE


    // Constructor Geral
    public Vidro(String nomeObra, String listaOrigem, String posicao,
                 String tipologia, String especificacao, int larguraMM, int alturaMM, int quantidadeTotal) {
        this.nomeObra = nomeObra;
        this.listaOrigem = listaOrigem;
        this.posicao = posicao;
        this.tipologia = tipologia;
        this.especificacao = especificacao;
        this.larguraMM = larguraMM;
        this.alturaMM = alturaMM;
        this.quantidadeTotal = quantidadeTotal;

        this.qtdChegouFabrica = 0;
        this.qtdCortada = 0;
        this.qtdReposicao = 0;

        this.idItemUnico = gerarIdUnico();
        calcularStatus();
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

    public String getTipologia() { return tipologia; }

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

    public int getQtdCortada() {
        return qtdCortada;
    }

    public int getQtdReposicao() {return qtdReposicao;}

    public String getStatusGeral() {
        return statusGeral;
    }

    // Setters
    public void setQtdChegouFabrica(int qtdChegouFabrica) {
        this.qtdChegouFabrica = qtdChegouFabrica;
    }

    public void setQtdCortada(int qtdCortada) {
        this.qtdCortada = qtdCortada;
    }

    public void setQtdReposicao(int qtdReposicao) {this.qtdReposicao = qtdReposicao;}

    // Função para definir Status
    public void calcularStatus() {
        if (qtdCortada >= quantidadeTotal) {
            this.statusGeral = "FINALIZADO";
        } else if (qtdReposicao > 0) {
            this.statusGeral = "EM REPOSIÇÃO (" + qtdReposicao + ")";
        } else if (qtdChegouFabrica >= quantidadeTotal) {
            this.statusGeral = "PRONTO P/ CORTE";
        } else if (qtdChegouFabrica > 0 && qtdChegouFabrica < quantidadeTotal) {
            this.statusGeral = "FALTA MATERIAL (" + (quantidadeTotal - qtdChegouFabrica) + ")";
        } else {
            this.statusGeral = "AGUARDANDO FORNECEDOR";
        }
    }

    // Função para definir o ID ÚNICO
    public String gerarIdUnico() {
        // Usa Posição + Tipologia para garantir unicidade
        String base = (posicao != null ? posicao : "") + (tipologia != null ? tipologia : "");
        if (!base.isEmpty()) {
            return base + "-" + larguraMM + "-" + alturaMM;
        }
        return "ITEM-" + Math.abs((nomeObra + especificacao + larguraMM + alturaMM).hashCode());
    }

    // Metódo para exibir as especificações do vidro
    @Override
    public String toString() {
        return String.format("Obra: %s | Lista: %s | Posição: %s | Especificação: %s | Status: %s", nomeObra,
                listaOrigem, posicao, especificacao, statusGeral);
    }
}
