package br.com.vidros.io;

import br.com.vidros.modelo.Vidro;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ImportadorExcel {

    // Índice da primeira linha de dados
    private static final int LINHA_INICIO_DADOS = 7;

    // Definição dos índices das colunas
    private static final int COL_POSICAO = 1;
    private static final int COL_TIPO = 2;
    private static final int COL_ESPECIFICACAO = 3;
    private static final int COL_LARGURA = 4;
    private static final int COL_ALTURA = 5;
    private static final int COL_QUANTIDADE = 9;

    /*
     * Importa dados de uma planilha Excel (ou CSV/XLSX/XLSM) para uma lista de objetos Vidro.
     * Esta função é o ponto onde o nome da obra e da lista de origem são vinculados ao dado.
     * @param arquivo O arquivo Excel/CSV a ser lido.
     * @param nomeObra O nome da obra
     * @param listaOrigem O nome da lista
     * @return Uma lista de objetos Vidro criados a partir das linhas do arquivo.
     */

    public static List<Vidro> importar(File arquivo, String nomeObra, String listaOrigem) {
        List<Vidro> listaVidros = new ArrayList<>();

        // WorkbookFactory para suportar .xlsx, .xls e .xlsm
        try (FileInputStream fileIn = new FileInputStream(arquivo);
             Workbook workbook = WorkbookFactory.create(fileIn)) {

            // Assume que os dados estão na primeira aba
            Sheet sheet = workbook.getSheetAt(0);

            // Itera sobre as linhas, começando pelo inicio dos dados
            Iterator<Row> rowIterator = sheet.iterator();
            int linhaAtual = 0;

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                linhaAtual++;

                // Pula as linhas de cabeçalho e informações da obra
                if (linhaAtual < LINHA_INICIO_DADOS) {
                    continue;
                }

                // Tenta extrair os dados e criar o objeto vidro
                try {
                    String posicao = lerCelulaString(row, COL_POSICAO);
                    String tipoEsquadria = lerCelulaString(row, COL_TIPO);
                    String especificacao = lerCelulaString(row, COL_ESPECIFICACAO);
                    int larguraMM = lerCelulaInteira(row, COL_LARGURA);
                    int alturaMM = lerCelulaInteira(row, COL_ALTURA);
                    int quantidade = lerCelulaInteira(row, COL_QUANTIDADE);

                    // Validação simples: Ignora linhas que não tem posição ou quantidade
                    if (posicao.isEmpty() || quantidade <= 0) {
                        continue;
                    }

                    // 1. Cria o objeto Vidro
                    Vidro vidro = new Vidro(
                            nomeObra,
                            listaOrigem,
                            posicao,
                            especificacao + " (" + tipoEsquadria + ")",
                            larguraMM,
                            alturaMM,
                            quantidade
                    );

                    // 2. Adiciona o vidro à lista
                    listaVidros.add(vidro);

                } catch (NumberFormatException e) {
                    // Ignora a linha se houver erro de conversão
                    System.err.println("Aviso: Linha" + linhaAtual + " ignorada devido a formato de número inválido. Detalhe: " +
                            e.getMessage());
                } catch (IllegalStateException e) {
                    // Ignora a linha se houver erro de estado (célula vazia em string)
                    System.err.println("Aviso: Linha " + linhaAtual + " ignorada devido a célula vazia/inválida. Detalhe: " +
                            e.getMessage());
                }
            }

            System.out.println("Importação finalizada. Total de " + listaVidros.size() + " itens de vidros importados.");

        } catch (IOException e) {
            System.err.println("ERRO de I/O ao processar o arquivo: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("ERRO inesperado na importação: " + e.getMessage());
            e.printStackTrace();
        }

        return listaVidros;
    }

    // Funções de Leitura Robusta de Células

    private static String lerCelulaString(Row row, int colIndex) {
        Cell cell = row.getCell(colIndex);
        if (cell == null) {
            return "";
        }
        // Configura a célula para retornar o valor como String
        cell.setCellType(CellType.STRING);
        return cell.getStringCellValue().trim();
    }

    private static int lerCelulaInteira(Row row, int colIndex) throws NumberFormatException {
        Cell cell = row.getCell(colIndex);
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            return 0; // Retorna 0 se a célula estiver vazia
        }

        switch (cell.getCellType()) {
            case NUMERIC:
                return (int) cell.getNumericCellValue();
            case STRING:
                try {
                    return Integer.parseInt(cell.getStringCellValue().trim()); // Remove espaços
                } catch (NumberFormatException e) {
                    throw new NumberFormatException("Valor não é um número inteiro na coluna " + colIndex + ": "
                    + cell.getStringCellValue());
                }
            case FORMULA:
                return (int) cell.getNumericCellValue();
            default:
                return 0;
        }
    }

}
