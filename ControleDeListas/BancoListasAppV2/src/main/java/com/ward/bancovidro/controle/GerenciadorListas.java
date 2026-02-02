package com.ward.bancovidro.controle;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.ward.bancovidro.modelo.RegistroLista;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class GerenciadorListas {
    private List<RegistroLista> banco = new ArrayList<>();
    private final String ARQUIVO = "banco_listas.json";
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public GerenciadorListas() {
        carregar();
    }

    public void adicionarLista(List<RegistroLista> novosItens) {
        banco.addAll(novosItens);
        salvar();
    }

    public void removerRegistro(String id) {
        banco.removeIf(r -> r.getId().equals(id));
        salvar();
    }

    public void limparTudo() {
        banco.clear();
        salvar();
    }

    public List<RegistroLista> getTodos() {
        return banco;
    }

    private void salvar() {
        try (FileWriter writer = new FileWriter(ARQUIVO)) {
            writer.write(gson.toJson(banco));
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void carregar() {
        File f = new File(ARQUIVO);
        if (!f.exists()) return;
        try (FileReader reader = new FileReader(f)) {
            Type t = new TypeToken<ArrayList<RegistroLista>>() {}.getType();
            List<RegistroLista> l = gson.fromJson(reader, t);
            if (l != null) banco = l;
        }   catch (IOException e) { e.printStackTrace(); }
    }
}
