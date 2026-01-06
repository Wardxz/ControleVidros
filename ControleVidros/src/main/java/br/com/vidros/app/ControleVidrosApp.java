package br.com.vidros.app;

import br.com.vidros.controle.GerenciadorVidros;
import br.com.vidros.io.ImportadorExcel;
import br.com.vidros.modelo.Vidro;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ControleVidrosApp extends JFrame {

    private final GerenciadorVidros gerenciador;
    private JTable tabelaVidros;
    private DefaultTableModel tableModel;
    private JComboBox<String> cbFiltroObra;
    private JLabel lblStatusRodape;

    public ControleVidrosApp() {
        // Inicializa o gerenciador (carrega o JSON automaticamente)
        this.gerenciador = new GerenciadorVidros();

        configurarJanela();
        inicializarComponentes();

        // Carrega os dados iniciais na tabela
        atualizarComboObras();
        atualizarTabela();
    }

    private void configurarJanela() {
        setTitle("Sistema de Controle Geral de Vidros - MultiObras");
        setSize(1200,700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);    // Centraliza na tela
        setLayout(new BorderLayout());
    }

    private void inicializarComponentes() {
        // === 1. PAINEL SUPERIOR (BOTÕES E FILTROS) ===
        JPanel painelSuperior = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton btnImportar = new JButton("Importar Nova Lista");
        btnImportar.setIcon(UIManager.getIcon("FileView.directoryIcon"));

        JLabel lblFiltro = new JLabel("Filtrar por Obra: ");
        cbFiltroObra = new JComboBox<>();
        cbFiltroObra.addItem("TODAS AS OBRAS");

        JButton btnAtualizar = new JButton("Atualizar Tabela");

        painelSuperior.add(btnImportar);
        painelSuperior.add(Box.createHorizontalStrut(20)); // Espaçamento
        painelSuperior.add(lblFiltro);
        painelSuperior.add(cbFiltroObra);
        painelSuperior.add(btnAtualizar);

        add(painelSuperior, BorderLayout.NORTH);

        // === 2. PAINEL CENTRAL ===
        String[] colunas = {
            "ID Único", "Obra", "Tipologia", "Especificação",
            "L (mm)", "H (mm)", "Total", "Chegou", "Cortado", "Status"
        };

        // Modelo de Tabela não editável
        tableModel = new DefaultTableModel(colunas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tabelaVidros = new JTable(tableModel);

        // Aplica o renderizador de cores
        tabelaVidros.setDefaultRenderer(Object.class, new StatusColorRenderer());

        // Ajuste da largura das colunas
        tabelaVidros.getColumnModel().getColumn(0).setPreferredWidth(150);  // ID
        tabelaVidros.getColumnModel().getColumn(3).setPreferredWidth(300);  // Espec
        tabelaVidros.getColumnModel().getColumn(9).setPreferredWidth(150);  //Status

        JScrollPane scrollPane = new JScrollPane(tabelaVidros);
        add(scrollPane, BorderLayout.CENTER);

        // === 3. PAINEL INFERIOR (AÇÕES DE CONTROLE)
        JPanel painelInferior = new JPanel(new FlowLayout(FlowLayout.CENTER));
        painelInferior.setBorder(BorderFactory.createTitledBorder("Ações de Controle"));

        JButton btnDarEntrada = new JButton("Dar Entrada (Fábrica)");
        btnDarEntrada.setBackground(new Color(200, 230, 255));  // Azul claro

        JButton btnDarBaixa = new JButton("Dar Baixa (Corte Finalizado)");
        btnDarBaixa.setBackground(new Color(200, 255, 200));    // Verde Claro

        JButton btnVerDetalhes = new JButton("Ver Detalhes / Lista");

        painelInferior.add(btnDarEntrada);
        painelInferior.add(Box.createHorizontalStrut(20));
        painelInferior.add(btnDarBaixa);
        painelInferior.add(Box.createHorizontalStrut(20));
        painelInferior.add(btnVerDetalhes);

        add(painelInferior, BorderLayout.SOUTH);

        // === 4. LISTENERS (AÇÕES DOS BOTÕES) ===

        // Ação: Importar
        btnImportar.addActionListener(e -> acaoImportarExcel());

        // Ação: Filtro de Obra
        cbFiltroObra.addActionListener(e -> atualizarTabela());

        // Ação: Atualizar
        btnAtualizar.addActionListener(e -> atualizarTabela());

        // Ação: Dar Entrada (Chegada na Fábrica)
        btnDarEntrada.addActionListener(e -> acaoDarEntrada());

        // Ação: Dar Baixa (Corte)
        btnDarBaixa.addActionListener(e -> acaoDarBaixa());

        // Ação: Ver Detalhes
        btnVerDetalhes.addActionListener(e -> acaoVerDetalhes());
    }

    // LÓGICA DE NEGÓCIO DA INTERFACE

    private void atualizarComboObras() {
        // Guarda a seleção atual para restaurar depois
        Object selecaoAtual = cbFiltroObra.getSelectedItem();

        cbFiltroObra.removeAllItems();
        cbFiltroObra.addItem("Todas as Obras");

        // Pega todas as obras únicas do gerenciador
        List<Vidro> todos = gerenciador.getTodosVidros();
        Set<String> obras = todos.stream()
                .map(Vidro::getNomeObra).collect(Collectors.toSet());

        for (String obra : obras) {
            cbFiltroObra.addItem(obra);
        }

        if (selecaoAtual != null) {
            cbFiltroObra.setSelectedItem(selecaoAtual);
        }
    }

    private void atualizarTabela() {
        tableModel.setRowCount(0);  // Limpa a tabela
        String obraSelecionada = (String) cbFiltroObra.getSelectedItem();
        List<Vidro> listaExibir;

        if (obraSelecionada == null || obraSelecionada.equals("Todas as Obras")) {
            listaExibir = gerenciador.getTodosVidros();
        } else {
            listaExibir = gerenciador.filtrarPorObra(obraSelecionada);
        }

        for (Vidro v : listaExibir) {
            Object[] linha = {
                v.getIdItemUnico(),
                v.getNomeObra(),
                v.getPosicao(),
                v.getEspecificacao(),
                v.getLarguraMM(),
                v.getAlturaMM(),
                v.getQuantidadeTotal(),
                v.getQtdChegouFabrica(),
                v.getQtdCortada(),
                v.getStatusGeral()
            };
            tableModel.addRow(linha);
        }
    }

    private void acaoImportarExcel() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Selecione a lista de Vidros (Excel/CSV)");

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File arquivo = fileChooser.getSelectedFile();

            // Pergunta o Nome da Obra
            String nomeObra = JOptionPane.showInputDialog(this,
                    "Digite o NOME da OBRA para esta lista:\n",
                    "Identificação da Obra", JOptionPane.QUESTION_MESSAGE);

            if (nomeObra == null || nomeObra.trim().isEmpty()) return;

            // Pergunta o Nome da Lista
            String nomeLista = JOptionPane.showInputDialog(this,
                    "Digite o NOME / REVISÃO desta lista:\n",
                    "Identificação da Lista", JOptionPane.QUESTION_MESSAGE);

            if (nomeLista == null) nomeLista = "Indefinida";

            try {
                // Chama o importador passando os parâmetros de organização
                List<Vidro> novosVidros = ImportadorExcel.importar(arquivo, nomeObra.toUpperCase(), nomeLista);

                if (!novosVidros.isEmpty()) {
                    gerenciador.adicionarListaDeObra(novosVidros);
                    atualizarComboObras();
                    atualizarTabela();
                    JOptionPane.showMessageDialog(this, novosVidros.size() + "itens importados com sucesso!");
                } else {
                    JOptionPane.showMessageDialog(this, "Nenhum item encontrado. Verfique o layout da planilha.");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro ao importar: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    private void acaoDarEntrada() {
        int row = tabelaVidros.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Selecione um item na tabela para dar entrada.");
            return;
        }

    }

    private void acaoDarBaixa() {
        int row = tabelaVidros.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Selecione um item para dar baixa (Corte Realizado)");
            return;
        }

        String idUnico = (String) tableModel.getValueAt(row, 0);
        String qtdChegou = tableModel.getValueAt(row, 7).toString();
        String qtdCortada = tableModel.getValueAt(row, 8).toString();

        String input = JOptionPane.showInputDialog(this,
                "Quantidade Cortada Agora:\n(Disponível na fábrica: " + qtdChegou + " | Já Cortado: " +
                qtdCortada + ")");

        if (input != null) {
            try {
                int qtd = Integer.parseInt(input);
                gerenciador.darBaixaCorte(idUnico, qtd);
                atualizarTabela();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Número inválido.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "ERRO: " + ex.getMessage());
            }
        }
    }

    private void acaoVerDetalhes() {
        int row = tabelaVidros.getSelectedRow();
        if (row == -1) return;

        String idUnico = (String) tableModel.getValueAt(row, 0);

        // Busca o objeto completo na lista do gerenciador
        Vidro vidro = gerenciador.getTodosVidros().stream()
                .filter(v -> v.getIdItemUnico().equals(idUnico))
                .findFirst().orElse(null);

        if (vidro != null) {
            JOptionPane.showMessageDialog(this,
                    "Detalhes do item:\n\n" +
                    "Obra: " + vidro.getNomeObra() + "\n" +
                    "Lista Origem: " + vidro.getListaOrigem() + "\n" +
                    "Posição: " + vidro.getPosicao() + "\n" +
                    "Dimensões: " + vidro.getLarguraMM() + " x " + vidro.getAlturaMM() + " mm\n" +
                    "Status: " + vidro.getStatusGeral() + "\n\n" +
                    "Histórico:\n" +
                    "Chegou na Fábrica: " + vidro.getQtdChegouFabrica() + " (NFE: " + vidro.getNfeEntrada() + ")\n" +
                    "Cortado: " + vidro.getQtdCortada(), "Detalhes do Vidro", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // Coloração das Linhas (Renderer)
    static class StatusColorRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            // Pega o valor da Coluna Status - índice 9
            String status = (String) table.getModel().getValueAt(row, 9);

            if (!isSelected) {  // Mantém a cor de seleção padrão se selecionado
                if (status.contains("FINALIZADO") || status.contains("COMPLETO")) {
                    c.setBackground(new Color(200, 255, 200));  // Verde Claro
                } else if (status.contains("FALTA MATERIAL")) {
                    c.setBackground(new Color(255, 200, 200));  // Vermelho Claro
                } else if (status.contains("EM PRODUÇÃO") || status.contains("EM CORTE")) {
                    c.setBackground(new Color(255, 255, 200));  // Amarelo Claro
                } else {
                    c.setBackground(Color.WHITE);
                }
                c.setForeground(Color.BLACK);
            }

            return c;
        }
    }

    public static void main(String[] args) {
        // Tenta usar o visual do sistema operacional
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            new ControleVidrosApp().setVisible(true);
        });
    }
}
