package br.com.vidros.app;

import br.com.vidros.controle.GerenciadorVidros;
import br.com.vidros.io.ImportadorExcel;
import br.com.vidros.io.ExportadorExcel;
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
        setSize(1366,768);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);    // Centraliza na tela
        setLayout(new BorderLayout());
    }

    private void inicializarComponentes() {
        // === 1. PAINEL SUPERIOR (BOTÕES E FILTROS) ===
        JPanel painelSuperior = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton btnImportar = new JButton("Importar Nova Lista");
        btnImportar.setBackground(new Color(230, 230, 250));    // Lavanda
        btnImportar.setIcon(UIManager.getIcon("FileView.directoryIcon"));

        JButton btnExcluirLista = new JButton("Excluir Lista");
        btnExcluirLista.setBackground(new Color(255, 200, 200));    // Vermelho Claro
        btnExcluirLista.setForeground(Color.RED);

        JButton btnExportar = new JButton("Exportar Tabela para Excel");
        btnExportar.setBackground(new Color(255, 255, 224));    // Amarelo Claro
        btnExportar.setIcon(UIManager.getIcon("FileView.floppyDriveIcon")); // Ícone de disquete

        JLabel lblFiltro = new JLabel(" | Filtrar por Obra: ");
        cbFiltroObra = new JComboBox<>();
        cbFiltroObra.addItem("TODAS AS OBRAS");

        painelSuperior.add(btnImportar);
        painelSuperior.add(btnExportar);
        painelSuperior.add(btnExcluirLista);
        painelSuperior.add(Box.createHorizontalStrut(20)); // Espaçamento
        painelSuperior.add(lblFiltro);
        painelSuperior.add(cbFiltroObra);

        add(painelSuperior, BorderLayout.NORTH);

        // === 2. PAINEL CENTRAL ===
        String[] colunas = {
            "ID", "Obra", "Lista", "Posição", "Tipologia", "Especificação",
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

        // Ordenação automática ao clicar no cabeçalho
        tabelaVidros.setAutoCreateRowSorter(true);

        // Ajuste da largura das colunas
        tabelaVidros.getColumnModel().getColumn(0).setPreferredWidth(60);   // ID
        tabelaVidros.getColumnModel().getColumn(2).setPreferredWidth(60);   // Lista
        tabelaVidros.getColumnModel().getColumn(3).setPreferredWidth(100);  // Posição
        tabelaVidros.getColumnModel().getColumn(4).setPreferredWidth(80);  // Espec
        tabelaVidros.getColumnModel().getColumn(5).setPreferredWidth(300);  // Espec
        tabelaVidros.getColumnModel().getColumn(11).setPreferredWidth(140); //Status

        JScrollPane scrollPane = new JScrollPane(tabelaVidros);
        add(scrollPane, BorderLayout.CENTER);

        // === 3. PAINEL INFERIOR (AÇÕES DE CONTROLE)
        JPanel painelInferior = new JPanel(new FlowLayout(FlowLayout.CENTER));
        painelInferior.setBorder(BorderFactory.createTitledBorder("Ações de Controle"));

        JButton btnDarEntrada = new JButton("Dar Entrada (Fábrica)");
        btnDarEntrada.setBackground(new Color(200, 230, 255));  // Azul claro

        JButton btnDarBaixa = new JButton("Dar Baixa (Corte Finalizado)");
        btnDarBaixa.setBackground(new Color(200, 255, 200));    // Verde Claro

        JButton btnReposicao = new JButton("Solicitar Reposição");
        btnReposicao.setBackground(new Color(255, 160,122));    // Salmão Laranja

        JButton btnVerDetalhes = new JButton("Ver Detalhes / Lista");

        painelInferior.add(btnDarEntrada);
        painelInferior.add(Box.createHorizontalStrut(20));
        painelInferior.add(btnDarBaixa);
        painelInferior.add(Box.createHorizontalStrut(30));
        painelInferior.add(btnReposicao);
        painelInferior.add(Box.createHorizontalStrut(20));
        painelInferior.add(btnVerDetalhes);

        add(painelInferior, BorderLayout.SOUTH);

        // === 4. LISTENERS (AÇÕES DOS BOTÕES) ===

        btnImportar.addActionListener(e -> acaoImportarExcel());
        btnExportar.addActionListener(e -> ExportadorExcel.exportarTabela(tabelaVidros, this));
        btnExcluirLista.addActionListener(e -> acaoExcluirTabela());
        cbFiltroObra.addActionListener(e -> atualizarTabela());

        btnDarEntrada.addActionListener(e -> acaoDarEntrada());
        btnDarBaixa.addActionListener(e -> acaoDarBaixa());
        btnReposicao.addActionListener(e -> acaoReposicao());
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

        if (selecaoAtual != null) cbFiltroObra.setSelectedItem(selecaoAtual);
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
                v.getIdItemUnico(), v.getNomeObra(), v.getListaOrigem(), v.getPosicao(),
                v.getTipologia(), v.getEspecificacao(), v.getLarguraMM(), v.getAlturaMM(),
                v.getQuantidadeTotal(), v.getQtdChegouFabrica(),
                v.getQtdCortada(), v.getStatusGeral()
            };
            tableModel.addRow(linha);
        }
    }

    private void acaoExcluirTabela() {
        // 1. Escolher a Obra
        List<Vidro> todos = gerenciador.getTodosVidros();
        Object[] obras = todos.stream().map(Vidro::getNomeObra).distinct().toArray();

        if (obras.length == 0) {
            JOptionPane.showMessageDialog(this, "Não há obras cadastradas.");
            return;
        }

        String obraEscolhida = (String) JOptionPane.showInputDialog(
                this, "Selecione a Obra da qual deseja excluir uma Lista:",
                "Excluir Lista - Passo 1", JOptionPane.QUESTION_MESSAGE, null, obras, obras[0]);

        if (obraEscolhida == null) return;

        // 2. Escolher a Lista dentro da Obra
        List<String> listas = gerenciador.getListasDaObra(obraEscolhida);
        if (listas.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nenhuma lista encontrada para esta obra.");
            return;
        }

        String listaEscolhida = (String) JOptionPane.showInputDialog(
                this, "Selecione a Lista para EXCLUIR DEFINITIVAMENTE:",
                "Excluir Lista - Passo 2", JOptionPane.WARNING_MESSAGE, null, listas.toArray(), listas.get(0));

        if (listaEscolhida == null) return;

        // 3. Confirmar
        int confirm = JOptionPane.showConfirmDialog(this,
                "Tem certeza que deseja excluir a lista '" + listaEscolhida + "' da obra " + obraEscolhida + "'?\n" +
                "Essa ação não pode ser desfeita.",
                "Confirmação Perigosa", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean sucesso = gerenciador.excluirListaDeObra(obraEscolhida, listaEscolhida);
            if (sucesso) {
                JOptionPane.showMessageDialog(this, "Lista excluída com sucesso.");
                atualizarComboObras();
                atualizarTabela();
            } else {
                JOptionPane.showMessageDialog(this, "Erro ao excluir lista.");
            }
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
                    JOptionPane.showMessageDialog(this, novosVidros.size() + " itens importados com sucesso!");
                } else {
                    JOptionPane.showMessageDialog(this, "Nenhum item encontrado. Verfique o layout da planilha.");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro ao importar: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    private String getIdSelecionado() {
        int row = tabelaVidros.getSelectedRow();
        if (row == -1) return null;
        int modelRow = tabelaVidros.convertRowIndexToModel(row);
        return (String) tableModel.getValueAt(modelRow, 0);
    }

    private void acaoDarEntrada() {
        int row = tabelaVidros.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Selecione uma linha.");
            return;
        }

        int modelRow = tabelaVidros.convertRowIndexToModel(row);
        String idUnico = (String) tableModel.getValueAt(modelRow, 0);

        String qtdChegou = tableModel.getValueAt(modelRow, 9).toString();
        String total = tableModel.getValueAt(modelRow, 8).toString();

        JPanel panel = new JPanel(new GridLayout(0, 1));
        JTextField txtQtd = new JTextField();
        panel.add(new JLabel("Qtd que chegou (Já tem " + qtdChegou + " de " + total + "):"));
        panel.add(txtQtd);

        if (JOptionPane.showConfirmDialog(null, panel, "Entrada", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            try {
                int qtd = Integer.parseInt(txtQtd.getText());

                // Chama o gerenciador e verifica se deu certo
                boolean sucesso = gerenciador.darEntradaFabrica(idUnico, qtd);

                if (sucesso) {
                    atualizarTabela();
                } else {
                    JOptionPane.showMessageDialog(this, "Erro ao atualizar item (ID não encontrado.");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Número inválido");
            }
        }
    }

    private void acaoDarBaixa() {
        int row = tabelaVidros.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Selecione um item para dar baixa (Corte Realizado)");
            return;
        }

        int modelRow = tabelaVidros.convertRowIndexToModel(row);

        String idUnico = (String) tableModel.getValueAt(modelRow, 0);
        String qtdChegou = tableModel.getValueAt(modelRow, 9).toString();
        String qtdCortada = tableModel.getValueAt(modelRow, 10).toString();

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

    private void acaoReposicao() {
        String id = getIdSelecionado();
        if (id == null) { JOptionPane.showMessageDialog(this, "Selecione um item."); return; }

        // Painel customizado para o Popup
        JPanel panel = new JPanel(new GridLayout(0, 1));
        JTextField txtQtd = new JTextField();
        String[] opcoesOrigem = {"Veio Errado / Quebrado (Reduzir Entrada)", "Quebrou no Corte (Reduzir Corte)"};
        JComboBox<String> cbOrigem = new JComboBox<>(opcoesOrigem);

        panel.add(new JLabel("Quantidade para Reposição:"));
        panel.add(txtQtd);
        panel.add(new JLabel("Onde ocorreu o problema?"));
        panel.add(cbOrigem);

        int result = JOptionPane.showConfirmDialog(null, panel, "Registrar Quebra/Erro", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                int qtd = Integer.parseInt(txtQtd.getText());
                String origem = cbOrigem.getSelectedIndex() == 0 ? "CHEGADA" : "CORTE";

                boolean ok = gerenciador.registrarReposicao(id, qtd, origem);
                if (ok) {
                    atualizarTabela();
                    JOptionPane.showMessageDialog(this, "Reposição Registrada!");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Número inválido.");
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
                    "Lista: " + vidro.getListaOrigem() + "\n" +
                    "Posição: " + vidro.getPosicao() + "\n" +
                    "Tipologia: " + vidro.getTipologia() + "\n" +
                    "Especificação: " + vidro.getEspecificacao() + "\n" +
                    "Dimensões: " + vidro.getLarguraMM() + " x " + vidro.getAlturaMM() + " mm\n" +
                    "---STATUS: " + vidro.getStatusGeral() + "---\n\n" +
                    "Histórico:\n" +
                    "Chegou na Fábrica: " + vidro.getQtdChegouFabrica() + "\n" +
                    "Cortado: " + vidro.getQtdCortada() + "\n" +
                    "Em Reposição: " + vidro.getQtdReposicao(), "Detalhes do Vidro", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    // Coloração das Linhas (Renderer)
    static class StatusColorRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

            Object valorStatus = table.getModel().getValueAt(table.convertRowIndexToModel(row), 11);

            String status = (valorStatus != null) ? valorStatus.toString() : "";

            if (!isSelected) {  // Mantém a cor de seleção padrão se selecionado
                if (status.contains("REPOSIÇÃO")) {
                    c.setBackground(new Color(255, 100, 100));  // Vermelho Forte
                } else if (status.contains("FINALIZADO") || status.contains("COMPLETO")) {
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

        SwingUtilities.invokeLater(() -> new ControleVidrosApp().setVisible(true));
    }
}
