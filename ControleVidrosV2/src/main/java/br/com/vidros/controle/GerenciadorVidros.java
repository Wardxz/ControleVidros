package br.com.vidros.controle;

import br.com.vidros.modelo.Vidro;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class GerenciadorVidros {

    // Lista central que armazena todos os vidros
    private List<Vidro> controleGeral = new ArrayList<>();

    // Nome do arquivo onde todos os dados serão salvos
    private final String ARQUIVO_DADOS = "controle_vidros.json";

    // Objeto Gson, formatando o Json (pretty printing)
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public GerenciadorVidros() {
        carregarDados();
    }

    // Funções de Persistência de Dados (List -> JSON File)

    public void salvarDados() {
        // Try-with-resources como garantia para o FileWriter ser fechado
        try (FileWriter writer = new FileWriter(ARQUIVO_DADOS)) {

            // 1. Converte a linha de objetos Vidro para uma String JSON
            String jsonDados = gson.toJson(controleGeral);

            // 2. Escreve a String JSON no arquivo
            writer.write(jsonDados);

            System.out.println("Dados salvos com sucesso no arquivo: " + ARQUIVO_DADOS);

        } catch (IOException e) {
            System.err.println("ERRO: Falha ao salvar os dados no arquivo JSON.");
            e.printStackTrace();
        }
    }

    // (JSON File -> List)
    public void carregarDados() {
        File arquivo = new File(ARQUIVO_DADOS);

        // Se o arquivo não existir, inicia com uma lista vazia
        if (!arquivo.exists()) {
            System.out.println("Arquivo de dados não encontrado. Iniciando novo controle.");
            controleGeral = new ArrayList<>();
            return;
        }

        // Try-with-resources como garantia para o FileReader ser fechado
        try (FileReader reader = new FileReader(arquivo)) {

            // Define o Tipo específico que o Gson deve deserializar
            Type tipoListaVidro = new TypeToken<ArrayList<Vidro>>() {}.getType();

            // 1. Lê o arquivo e usa Gson para converter o JSON de volta para a lista
            List<Vidro> listaCarregada = gson.fromJson(reader, tipoListaVidro);

            // 2. Garante que a lista não é nula e a atribui ao controleGeral
            if (listaCarregada != null) {
                this.controleGeral = listaCarregada;
                System.out.println("Dados carregados com sucesso. Total de itens: " + controleGeral.size());
            } else {
                this.controleGeral = new ArrayList<>();
                System.out.println("Arquivo JSON vazio ou inválido. Iniciando novo controle.");
            }
        } catch (IOException e) {
            System.err.println("ERRO: Falha ao carregar os dados do arquivo JSON.");
            e.printStackTrace();
            this.controleGeral = new ArrayList<>(); // Garante que o programa inicia mesmo com erro
        }
    }

    // Funções de Gestão (CRUD)

    // Categoria de Lista de Obra
    public void adicionarListaDeObra(List<Vidro> novaLista) {
        this.controleGeral.addAll(novaLista);
        this.salvarDados(); // Sempre salva após adicionar novos dados
    }

    public List<Vidro> getTodosVidros() {
        return controleGeral; // retorna a lista de todos os vidros
    }

    public List<Vidro> filtrarPorObra(String nomeObra) {
        return controleGeral.stream()
                .filter(v -> v.getNomeObra().equalsIgnoreCase(nomeObra))
                .collect(Collectors.toList());
    }

    // Rastreia quais vidros chegaram à fábrica e quais faltam
    public List<Vidro> getFaltantesFabrica() {
        return controleGeral.stream()
                .filter(v -> v.getQtdChegouFabrica() < v.getQuantidadeTotal())
                .collect(Collectors.toList());
    }

    // Chegada na fábrica
    public boolean darEntradaFabrica(String idItemUnico, int quantidade, String nfe) {
        // Remove espaços extras do ID para garantir o match
        String idBusca = idItemUnico.trim();

        Vidro item = controleGeral.stream()
                .filter(v -> v.getIdItemUnico().trim().equals(idBusca))
                .findFirst().orElse(null);

        if (item != null) {
            int novaQtd = item.getQtdChegouFabrica() + quantidade;
            item.setQtdChegouFabrica(novaQtd);
            item.setNfeEntrada(nfe);
            item.calcularStatus();
            this.salvarDados();
            return true;
        } else {
            System.err.println("ERRO: Item não encontrado com ID: " + idBusca);
            return false;
        }
    }

    // Vidros Cortados
    public boolean darBaixaCorte(String idItemUnico, int quantidade) {
        String idBusca = idItemUnico.trim();

        Vidro item = controleGeral.stream()
                .filter(v -> v.getIdItemUnico().equals(idBusca))
                .findFirst().orElse(null);

        if (item != null) {
            int novaQtd = item.getQtdCortada() + quantidade;
            item.setQtdCortada(novaQtd);
            item.calcularStatus();
            this.salvarDados();
            return true;
        } else {
            System.err.println("ERRO: Item não encontrado para baixa: " + idBusca);
            return false;
        }
    }

    // Visualizar a Lista de Origem
    public List<String> getListasDaObra(String nomeObra) {
        return controleGeral.stream()
                .filter(v -> v.getNomeObra().equalsIgnoreCase(nomeObra))
                .map(Vidro::getListaOrigem)
                .distinct() // Apenas valores únicos
                .collect(Collectors.toList());
    }
}
