package com.ward.bancovidro.modelo;

public class RegistroLista {
    private String id;
    private String dataRegistro;
    private String nomeObra;
    private String numeroLista;
    private String tipoVidro;
    private int qtdPecas;
    private double areaTotal;   // Talvez seja melhor trocar depois, só usam largura x altura

    public RegistroLista(String id, String dataRegistro, String nomeObra, String numeroLista, String tipoVidro, int qtdPecas, double areaTotal) {
        this.id = id;
        this.dataRegistro = dataRegistro;
        this.nomeObra = nomeObra;
        this.numeroLista = numeroLista;
        this.tipoVidro = tipoVidro;
        this.qtdPecas = qtdPecas;
        this.areaTotal = areaTotal;
    }

    public String getId() {
        return id;
    }

    public String getDataRegistro() {
        return dataRegistro;
    }

    public String getNomeObra() {
        return nomeObra;
    }

    public String getNumeroLista() {
        return numeroLista;
    }

    public String getTipoVidro() {
        return tipoVidro;
    }

    public int getQtdPecas() {
        return qtdPecas;
    }

    public double getAreaTotal() {
        return areaTotal;
    }
}
