package com.ward.bancovidro.app;

import com.ward.bancovidro.controle.GerenciadorListas;
import com.ward.bancovidro.io.LeitorListaCorte;
import com.ward.bancovidro.modelo.RegistroLista;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public class BancoListasApp extends JFrame {

    private GerenciadorListas gerenciador;
    private JTable tabela;
    private DefaultTableModel model;
    private JLabel lblStatus;

    private JComboBox<String> cbFiltroObra;
    private JComboBox<String> cbFiltroLista;

    public BancoListasApp() {
        gerenciador = new GerenciadorListas();
        configurar();
        initUI();
        atualizarCombos();
        atualizarTabela();
    }

    private void configurar() {
        setTitle("Banco de Demandas - Gestão Completa       ");
        setSize(1200, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
    }

    private void initUI() {
        // Painel Superior
        JPanel painelNorte = new JPanel(new BorderLayout());

        // Filtros
        JPanel panelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelFiltros.setBorder(BorderFactory.createTitledBorder("Filtros / Seleção de Exclusão "));

        cbFiltroObra = new JComboBox<>();
        cbFiltroObra.addItem("TODAS");

        cbFiltroLista = new JComboBox<>();
        cbFiltroLista.addItem("TODAS");
        cbFiltroLista.setEnabled(false);

        JButton btnAplicarFiltro = new JButton("Filtrar Tabela");

        panelFiltros.add(new JLabel("Obra:"));
        panelFiltros.add(cbFiltroObra);
        panelFiltros.add(Box.createHorizontalStrut(15));
        panelFiltros.add(new JLabel("Lista:"));
        panelFiltros.add(cbFiltroLista);
        panelFiltros.add(Box.createHorizontalStrut(15));
        panelFiltros.add(btnAplicarFiltro);

        // Botões de Ação
        JPanel panelBotoes = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton btnImportar = new JButton("Importar (Excel)");
        btnImportar.setBackground(new Color(200, 230, 255));

        JButton btnExcluirItem = new JButton("Excluir Item (Linha)");
        btnExcluirItem.setBackground(new Color(255, 228, 115));

        JButton btnExcluirObra = new JButton("Excluir Obra Atual");
        btnExcluirObra.setBackground(new Color(255, 128, 128));
        btnExcluirObra.setToolTipText("Exclui todos os itens da Obra selecionada no filtro acima");

        JButton btnExcluirLista = new JButton("Excluir Lista Atual");
        btnExcluirLista.setBackground(new Color(251, 85, 85));
        btnExcluirLista.setToolTipText("Exclui a Lista selecionada no filtro acima");

        JButton btnLimparTudo = new JButton("LIMPAR TUDO");
        btnLimparTudo.setBackground(new Color(220, 50, 50));

        panelBotoes.add(btnImportar);
        panelBotoes.add(btnExcluirItem);
        panelBotoes.add(btnExcluirObra);
        panelBotoes.add(btnExcluirLista);
        panelBotoes.add(btnLimparTudo);

        painelNorte.add(panelFiltros, BorderLayout.NORTH);
        painelNorte.add(panelBotoes, BorderLayout.SOUTH);

        add(painelNorte, BorderLayout.NORTH);

        // Centro
        String[] cols = {"ID", "Data", "Obra", "Lista", "Especificação", "Qtd", "Área Total", "Detalhamento (Posições)", "Status"};
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return column == 8;  }
        };

        tabela = new JTable(model);
        tabela.setRowHeight(25);
        tabela.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));

        // Por demanda, achei necessário expor o ID
        tabela.getColumnModel().getColumn(0).setMinWidth(0);
        tabela.getColumnModel().getColumn(0).setMaxWidth(0);
        tabela.getColumnModel().getColumn(4).setPreferredWidth(300);
        tabela.getColumnModel().getColumn(7).setPreferredWidth(200);
        tabela.setAutoCreateRowSorter(true);

        // Evento Duplo Clique
        tabela.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) mostrarDetalhesVidro();
            }
        });

        add(new JScrollPane(tabela), BorderLayout.CENTER);

        // Rodapé
        JPanel panelRodape = new JPanel(new FlowLayout(FlowLayout.LEFT));
        lblStatus = new JLabel("Pronto. " + gerenciador.getTodos().size() + " registros carregados.");
        panelRodape.add(lblStatus);
        add(panelRodape, BorderLayout.SOUTH);

        // Listeners
        cbFiltroObra.addActionListener(e -> {
            String obra = (String) cbFiltroObra.getSelectedItem();

            cbFiltroLista.removeAllItems();
            cbFiltroLista.addItem("TODAS");

            if (obra != null && !obra.equals("TODAS")) {
                List<String> listas = gerenciador.getListasDaObra(obra);

                for (String l : listas) {
                    cbFiltroLista.addItem(l);
                }
                cbFiltroLista.setEnabled(true);
            } else {
                cbFiltroLista.setEnabled(false);
            }

            atualizarTabela();
        });

        cbFiltroLista.addActionListener(e -> atualizarTabela());
        btnAplicarFiltro.addActionListener(e -> atualizarTabela());

        btnImportar.addActionListener(e -> acaoImportar());
        btnExcluirItem.addActionListener(e -> acaoExcluirItem());
        btnExcluirObra.addActionListener(e -> acaoExcluirObra());
        btnExcluirLista.addActionListener(e -> acaoExcluirLista());
        btnLimparTudo.addActionListener(e -> acaoLimparTudo());
    }

    private void acaoImportar() {
        String nomeObra = JOptionPane.showInputDialog(this, "Digite o NOME DA OBRA:");
        if (nomeObra == null || nomeObra.trim().isEmpty()) return;

        JFileChooser fc = new JFileChooser();
        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                List<RegistroLista> novos = LeitorListaCorte.lerArquivo(fc.getSelectedFile(), nomeObra.trim());
                if (!novos.isEmpty()) {
                    gerenciador.adicionarLista(novos);
                    atualizarCombos();
                    cbFiltroObra.setSelectedItem(nomeObra.trim().toUpperCase());
                    atualizarTabela();
                    JOptionPane.showMessageDialog(this, "Importação Concluída!");
                }
            } catch (Exception ex) { ex.printStackTrace(); }
        }
    }

    private void acaoExcluirItem() {
        int row = tabela.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Selecione uma linha na tabela para excluir.");
            return;
        }

        int modelRow = tabela.convertRowIndexToModel(row);
        String id = model.getValueAt(modelRow, 0).toString();

        if(JOptionPane.showConfirmDialog(this, "Excluir este item?", "Confirmar", JOptionPane.YES_NO_OPTION) == 0) {
            gerenciador.removerRegistro(id);
            atualizarTabela();
        }
    }

    private void acaoExcluirObra() {
        String obra = (String) cbFiltroObra.getSelectedItem();
        if (obra == null || obra.equals("TODAS")) {
            JOptionPane.showMessageDialog(this, "Selecione uma OBRA específica no filtro acima para excluir.");
            return;
        }

        int conf = JOptionPane.showConfirmDialog(this,
                "ATENÇÃO: Você vai apagar TODOS os registros da obra:\n\n" + obra + "\n\nTem certeza?",
                "Excluir Obra Inteira", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (conf == JOptionPane.YES_OPTION) {
            gerenciador.excluirPorObra(obra);
            JOptionPane.showMessageDialog(this, "Obra excluída com sucesso.");
            atualizarCombos();
            atualizarTabela();
        }
    }

    private void acaoExcluirLista() {
        String obra = (String) cbFiltroObra.getSelectedItem();
        String lista = (String) cbFiltroLista.getSelectedItem();

        if (obra == null || obra.equals("TODAS") || lista == null || lista.equals("TODAS")) {
            JOptionPane.showMessageDialog(this, "Selecione uma OBRA e uma LISTA específica nos filtros acima.");
            return;
        }

        int conf = JOptionPane.showConfirmDialog(this,
                "Deseja excluir apenas a lista '" + lista + "' da obra '" + obra + "'?",
                "Excluir Lista Específica", JOptionPane.YES_NO_OPTION);

        if (conf == JOptionPane.YES_OPTION) {
            gerenciador.excluirPorLista(obra, lista);
            JOptionPane.showMessageDialog(this, "Lista excluída.");

            cbFiltroLista.removeAllItems();
            cbFiltroLista.addItem("TODAS");
            for(String l : gerenciador.getListasDaObra(obra)) cbFiltroLista.addItem(l);

            atualizarTabela();
        }
    }

    private void acaoLimparTudo() {
        int conf1 = JOptionPane.showConfirmDialog(this,
                "PERIGO: Isso vai apagar TODO O BANCO DE DADOS.\nTodas as obras e listas serão perdidas.\n\nDeseja continuar?",
                "Limpar Tudo - Passo 1", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);

        if (conf1 == JOptionPane.YES_OPTION) {
            int conf2 = JOptionPane.showConfirmDialog(this,
                    "Tem certeza absoluta? Não há como desfazer.",
                    "Limpar Tudo - Confirmação Final", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);

            if (conf2 == JOptionPane.YES_OPTION) {
                gerenciador.limparTudo();
                atualizarCombos();
                atualizarTabela();
                JOptionPane.showMessageDialog(this, "Banco de dados resetado.");
            }
        }
    }

    private void mostrarDetalhesVidro() {
        int row = tabela.getSelectedRow();
        if (row == -1) return;
        int modelRow = tabela.convertRowIndexToModel(row);
        String id = model.getValueAt(modelRow, 0).toString();

        RegistroLista item = gerenciador.getTodos().stream()
                .filter(r -> r.getId().equals(id)).findFirst().orElse(null);

        if (item != null) {
            String msg = "VIDRO: " + item.getEspec() + "\n" +
                    "DIMENSÕES: " + item.getLargura() + " x " + item.getAltura() + " mm\n" +
                    "QTD: " + item.getQtd() + "\n" +
                    "ÁREA: " + String.format("%.2f", item.getAreaTotal()) + " m²\n\n" +
                    "POSIÇÕES (Rastreio):\n" + item.getDetalhamento();
            JOptionPane.showMessageDialog(this, msg, "Detalhes", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void atualizarTabela() {
        model.setRowCount(0);
        List<RegistroLista> dados = gerenciador.getTodos();

        String obraSel = (String) cbFiltroObra.getSelectedItem();
        String listaSel = (String) cbFiltroLista.getSelectedItem();

        // Filtro em memória
        if (obraSel != null && !obraSel.equals("TODAS")) {
            dados = dados.stream().filter(r -> r.getObra().equalsIgnoreCase(obraSel)).collect(Collectors.toList());
            if (listaSel != null && !listaSel.equals("TODAS")) {
                dados = dados.stream().filter(r -> r.getLista().equalsIgnoreCase(listaSel)).collect(Collectors.toList());
            }
        }

        double areaTotal = 0;
        for (RegistroLista r : dados) {
            model.addRow(new Object[] {
                    r.getId(),
                    r.getData(),
                    r.getObra(),
                    r.getLista(),
                    r.getEspec(),
                    r.getQtd(),
                    String.format("%.2f", r.getAreaTotal()),
                    r.getDetalhamento(),
                    r.getStatus()
            });
            areaTotal += r.getAreaTotal();
        }
        lblStatus.setText("Itens listados: " + dados.size() + " | Área Total: " + String.format("%.2f", areaTotal) + " m²");
    }

    private void atualizarCombos() {
        Object sel = cbFiltroObra.getSelectedItem();
        cbFiltroObra.removeAllItems();
        cbFiltroObra.addItem("TODAS");
        for (String obra : gerenciador.getObrasUnicas()) cbFiltroObra.addItem(obra);
        if (sel != null) cbFiltroObra.setSelectedItem(sel);
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new BancoListasApp().setVisible(true));
    }
}
