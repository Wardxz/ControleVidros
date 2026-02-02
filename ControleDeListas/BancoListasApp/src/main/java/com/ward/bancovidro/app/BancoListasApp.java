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
    private JLabel lblTotalArea;

    public BancoListasApp() {
        gerenciador = new GerenciadorListas();
        configurar();
        initUI();
        atualizarTabela();
    }

    private void configurar() {
        setTitle("Banco de Demandas - Lista de Cortes Recebidas");
        setSize(1100, 600);
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

        topo.add(btnImportar);
        topo.add(Box.createHorizontalStrut(20));
        topo.add(btnExcluir);

        add(topo, BorderLayout.NORTH);

        // Centro
        String[] cols = {"ID", "Data Entrada", "Obra", "Ref. Lista", "Tipo de Vidro", "Qtd Peças", "Área Total"};
        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        tabela = new JTable(model);
        tabela.getColumnModel().getColumn(0).setMinWidth(0);
        tabela.getColumnModel().getColumn(0).setMaxWidth(0);
        tabela.getColumnModel().getColumn(4).setPreferredWidth(300);
        tabela.setAutoCreateRowSorter(true);

        add(new JScrollPane(tabela), BorderLayout.CENTER);

        // Rodapé
        JPanel rodape = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        lblTotalArea = new JLabel("Total Acumulado: 0.00 m²");
        lblTotalArea.setFont(new Font("Arial", Font.BOLD, 14));
        rodape.add(lblTotalArea);
        add(rodape, BorderLayout.SOUTH);

        // Listeners
        btnImportar.addActionListener(e -> acaoImportar());
        btnExcluir.addActionListener(e -> acaoExcluir());
    }

    private void atualizarTabela() {
        model.setRowCount(0);
        List<RegistroLista> lista = gerenciador.getTodos();
        double areaTotalGeral = 0;

        for (RegistroLista r : lista) {
            model.addRow(new Object[] {
                    r.getId(),
                    r.getDataRegistro(),
                    r.getNomeObra(),
                    r.getNumeroLista(),
                    r.getTipoVidro(),
                    r.getQtdPecas(),
                    String.format("%.2f", r.getAreaTotal())
            });
            areaTotalGeral += r.getAreaTotal();
        }
        lblTotalArea.setText("Total Acumulado no Banco: " + String.format("%.2f", areaTotalGeral) + "m²");
    }

    private void acaoImportar() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Selecione o Arquivo da Lista (CSV/Excel)");

        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = fc.getSelectedFile();
            List<RegistroLista> novos = LeitorListaCorte.lerArquivo(f);

            if (novos.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Não foi possível ler os dados. Verifique o Layout.");
                return;
            }

            String resumo = "Obra: " + novos.get(0).getNomeObra() + "\n" +
                    "Lista: " + novos.get(0).getNumeroLista() + "\n" +
                    "Itens Identificados: " + novos.size() + " tipos de vidro.\n\n" +
                    "Deseja Registrar essa entrada de demanda?";

            int op = JOptionPane.showConfirmDialog(this, resumo, "Confirmar Entrada", JOptionPane.YES_NO_OPTION);

            if (op == JOptionPane.YES_OPTION) {
                for (RegistroLista r : novos) {
                    gerenciador.adicionarRegistro(r);
                }
                atualizarTabela();
                JOptionPane.showMessageDialog(this, "Lista registrada com sucesso!");
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
