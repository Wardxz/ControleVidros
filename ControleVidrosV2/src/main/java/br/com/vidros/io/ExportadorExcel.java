package br.com.vidros.io;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.Component;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ExportadorExcel {

    public static void exportarTabela(JTable tabela, Component parentComponent) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Salvar Tabela como Excel");
        fileChooser.setSelectedFile(new File("Relatorio_Vidros.xlsx"));

        if (fileChooser.showSaveDialog(parentComponent) == JFileChooser.APPROVE_OPTION) {
            File arquivo = fileChooser.getSelectedFile();
            if (!arquivo.getName().toLowerCase().endsWith(".xlsx")) {
                arquivo = new File(arquivo.getParentFile(), arquivo.getName() + ".xlsx");
            }

            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Relatório");
                TableModel model = tabela.getModel();

                // 1. Cria o cabeçalho
                Row headerRow = sheet.createRow(0);
                for (int col = 0; col < model.getColumnCount(); col++) {
                    Cell cell = headerRow.createCell(col);
                    cell.setCellValue(model.getColumnName(col));

                    // Estilo simples para negrito
                    CellStyle style = workbook.createCellStyle();
                    Font font = workbook.createFont();
                    font.setBold(true);
                    style.setFont(font);
                    cell.setCellStyle(style);
                }

                // 2. Preenche os dados
                for (int row = 0; row < tabela.getRowCount(); row++) {
                    Row excelRow = sheet.createRow(row + 1);
                    for (int col = 0; col < model.getColumnCount(); col++) {
                        Cell cell = excelRow.createCell(col);
                        Object valor = tabela.getValueAt(row, col);

                        if (valor != null) {
                            if (valor instanceof Number) {
                                cell.setCellValue(((Number) valor).doubleValue());
                            } else {
                                cell.setCellValue(valor.toString());
                            }
                        }
                    }
                }

                // Ajusta largura das colunas
                for (int col = 0; col < model.getColumnCount(); col++) {
                    sheet.autoSizeColumn(col);
                }

                try (FileOutputStream fileOut = new FileOutputStream(arquivo)) {
                    workbook.write(fileOut);
                }

                JOptionPane.showMessageDialog(parentComponent, "Exportação Concluída com sucesso!");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(parentComponent, "Erro ao salvar arquivo: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }
}
