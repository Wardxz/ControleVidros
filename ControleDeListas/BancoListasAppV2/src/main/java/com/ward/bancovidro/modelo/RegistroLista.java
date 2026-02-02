package com.ward.bancovidro.modelo;

import java.util.UUID;

public class RegistroLista {
    private String id;
    private String data;
    private String obra;
    private String lista;
    private String espec;
    private int qtd;
    private double areaTotal;
    private String detalhamento;
    private String status;

    public RegistroLista(String data, String obra, String lista, String espec, int qtd, double areaTotal,
                         String detalhamento, String status) {
        this.id = UUID.randomUUID().toString();
        this.data = data;
        this.obra = obra;
        this.lista = lista;
        this.espec = espec;
        this.qtd = qtd;
        this.areaTotal = areaTotal;
        this.detalhamento = detalhamento;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public String getData() {
        return data;
    }

    public String getObra() {
        return obra;
    }

    public String getLista() {
        return lista;
    }

    public String getEspec() {
        return espec;
    }

    public int getQtd() {
        return qtd;
    }

    public double getAreaTotal() {
        return areaTotal;
    }

    public String getDetalhamento() {
        return detalhamento;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
