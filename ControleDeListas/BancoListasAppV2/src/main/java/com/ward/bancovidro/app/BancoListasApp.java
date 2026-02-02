package com.ward.bancovidro.app;

import com.ward.bancovidro.controle.GerenciadorListas;
import com.ward.bancovidro.io.LeitorListaCorte;
import com.ward.bancovidro.modelo.RegistroLista;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.List;

public class BancoListasApp extends JFrame {

    private GerenciadorListas gerenciador;
    private JTable tabela;
    private DefaultTableModel model;
    private JLabel lblStatus;

    public BancoListasApp() {
        gerenciador = new GerenciadorListas();
        configurar();
        initUI();
        atualizarTabela();
    }

    private void configurar() {
        setTitle("Banco de Demandas - Lista de Cortes Recebidas");
        setSize(1200, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
    }

    private void initUI() {
        // Topo
        JPanel topo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnImportar = new JButton("Importar Lista de Corte (CSV/Excel)");
        btnImportar.setBackground(new Color(200, 230, 255));    // Azul esbranquiçado

        JButton btnExcluir = new JButton("Excluir Registro");
        btnExcluir.setBackground(new Color(255, 200, 200));     // Vermelho esbranquiçado

        JButton btnLimpar = new JButton("Limpar Tudo");
        btnLimpar.setBackground(new Color(220, 53, 69));        // Vermelho Forte

        topo.add(btnImportar);
        topo.add(Box.createHorizontalStrut(10));
        topo.add(btnExcluir);
        topo.add(Box.createHorizontalStrut(10));
        topo.add(btnLimpar);

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
        tabela.getColumnModel().getColumn(0).setPreferredWidth(50);
        tabela.getColumnModel().getColumn(4).setPreferredWidth(300);
        tabela.getColumnModel().getColumn(7).setPreferredWidth(200);
        tabela.setAutoCreateRowSorter(true);

        JScrollPane scrollPane = new JScrollPane(tabela);

        // Rodapé
        JPanel panelRodape = new JPanel(new FlowLayout(FlowLayout.LEFT));
        lblStatus = new JLabel("Pronto. " + gerenciador.getTodos().size() + " registros carregados.");
        panelRodape.add(lblStatus);

        // Listeners
        btnImportar.addActionListener(e -> acaoImportar());
        btnExcluir.addActionListener(e -> acaoExcluir());
        btnLimpar.addActionListener(e -> {
            if(JOptionPane.showConfirmDialog(this, "Tem certeza? Isso apaga tudo.") == 0) {
                gerenciador.limparTudo();
                atualizarTabela();
            }
        });


        add(topo, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(panelRodape, BorderLayout.SOUTH);
    }

    private void atualizarTabela() {
        model.setRowCount(0);
        List<RegistroLista> lista = gerenciador.getTodos();
        double areaTotalGeral = 0;

        for (RegistroLista r : lista) {
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
            areaTotalGeral += r.getAreaTotal();
        }
        lblStatus.setText("Total Acumulado no Banco: " + String.format("%.2f", areaTotalGeral) + "m²");
    }

    private void acaoImportar() {
        String nomeObra = JOptionPane.showInputDialog(this,
                "Digite o NOME DA OBRA para esta lista:",
                "Identificação da Obra",
                JOptionPane.QUESTION_MESSAGE);

        if (nomeObra == null || nomeObra.trim().isEmpty()) {
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Selecione a Lista de Corte (.xls, .xlsx, .csv)");

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File arquivo = fileChooser.getSelectedFile();
            try {
                List<RegistroLista> novos = LeitorListaCorte.lerArquivo(arquivo, nomeObra.trim());

                if (novos.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Nenhum vidro encontrado ou formato inválido.");
                } else {
                    gerenciador.adicionarLista(novos);
                    atualizarTabela();
                    JOptionPane.showMessageDialog(this, novos.size() + " itens importados para a obra: " + nomeObra);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Erro ao ler arquivo: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    private void acaoExcluir() {
        int row = tabela.getSelectedRow();
        if (row == -1) { JOptionPane.showMessageDialog(this, "Selecione uma linha."); return; }

        int modelRow = tabela.convertRowIndexToModel(row);
        String id = model.getValueAt(modelRow, 0).toString();

        gerenciador.removerRegistro(id);
        atualizarTabela();
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new BancoListasApp().setVisible(true));
    }
}
