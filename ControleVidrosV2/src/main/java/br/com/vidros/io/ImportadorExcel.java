package br.com.vidros.io;

import br.com.vidros.modelo.Vidro;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ImportadorExcel {

    // Código descobre os índices lendo o cabeçalho

    public static List<Vidro> importar(File arquivo, String nomeObra, String listaOrigem) {

        List<Vidro> listaVidros = new ArrayList<>();

        // Variáveis para guardar os índices das colunas descobertos
        int idxPosicao = -1;
        int idxTipo = -1;
        int idxEspecificacao = -1;
        int idxLargura = -1;
        int idxAltura = -1;
        int idxQuantidade = -1;

        boolean cabecalhoEncontrado = false;

        try (FileInputStream fileIn = new FileInputStream(arquivo);
        Workbook workbook = WorkbookFactory.create(fileIn)) {

            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();

            int numeroLinha = 0;

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                numeroLinha++;

                // 1. Tenta encontrar a linha de Cabeçalho
                if (!cabecalhoEncontrado) {
                    for (Cell cell : row) {
                        String textoCelula = lerCelulaString(cell).toUpperCase();

                        // Normaliza para remover acentos
                        textoCelula = removerAcentos(textoCelula);

                        // Mapeia as colunas baseadas em palavras-chave
                        if (textoCelula.contains("POSICAO")) idxPosicao = cell.getColumnIndex();
                        else if (textoCelula.contains("TIPO")) idxTipo = cell.getColumnIndex();
                        else if (textoCelula.contains("ESPECIFICACAO") || textoCelula.contains("DESCRICAO")) idxEspecificacao = cell.getColumnIndex();
                        else if (textoCelula.contains("LARGURA")) idxLargura = cell.getColumnIndex();
                        else if (textoCelula.contains("ALTURA")) idxAltura = cell.getColumnIndex();
                        else if (textoCelula.contains("QUANT") || textoCelula.contains("QTD")) idxQuantidade = cell.getColumnIndex();
                    }

                    // Se encontrar as colunas essenciais, marca como achado
                    if (idxLargura != -1 && idxAltura != -1 && idxQuantidade != -1) {
                        cabecalhoEncontrado = true;
                        System.out.println("Cabeçalho encontrado na linha " + numeroLinha);
                        System.out.println("Mapeamento: Largura=" + idxLargura + ", Altura=" + idxAltura +
                                ", Quantidade=" + idxQuantidade);
                    }
                    continue;
                }

                // 2. Processa as linhas de Dados
                try {
                    // Verifica se a linha tem dados suficientes
                    if (idxQuantidade == -1 || idxLargura == -1) continue;

                    String posicao = (idxPosicao != -1) ? lerCelulaString(row.getCell(idxPosicao)) : "N/A";
                    String tipo = (idxTipo != -1) ? lerCelulaString(row.getCell(idxTipo)) : "";
                    String especificacao = (idxEspecificacao != -1) ? lerCelulaString(row.getCell(idxEspecificacao)) : "";

                    int larguraMM = lerCelulaInteira(row.getCell(idxLargura));
                    int alturaMM = lerCelulaInteira(row.getCell(idxAltura));
                    int quantidade = lerCelulaInteira(row.getCell(idxQuantidade));

                    // Ignora linha se quantidade for zero ou posição vazia
                    if (quantidade <= 0 && larguraMM <= 0) continue;

                    // Se não tiver posição, usa um placeholder
                    if (posicao.isEmpty()) posicao = "S/N";

                    Vidro vidro = new Vidro(
                            nomeObra,
                            listaOrigem,
                            posicao,
                            especificacao + (tipo.isEmpty() ? "" : " (" + tipo + ")"),
                            larguraMM,
                            alturaMM,
                            quantidade
                    );

                    listaVidros.add(vidro);
                } catch (Exception e) {
                    // Log discreto para não poluir
                    // System.err.println("Linha " + numeroLinha + " ignorada: " + e.getMessage());
                }
            }

            if (listaVidros.isEmpty()) {
                System.err.println("AVISO: Nenhuma linha válida importada. Verifique se os nomes das colunas" +
                        "(Largura, Altura, Quant.) estão corretos no Excel.");
            } else {
                System.out.println("Importação finalizada. Total: " + listaVidros.size() + " itens.");
            }
        } catch (IOException e) {
            System.err.println("ERRO de I/O: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return listaVidros;
    }

    // Função Auxiliares

    private static String removerAcentos(String str) {
        return Normalizer.normalize(str, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
    }

    // Funções de Leitura Robusta de Células

    private static String lerCelulaString(Cell cell) {
        if (cell == null) return "";

        // Força leitura como texto para evitar erro
        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                return String.valueOf((int)cell.getNumericCellValue());
            }
            return cell.getStringCellValue().trim();
        } catch (Exception e) {
            return "";
        }
    }

    private static int lerCelulaInteira(Cell cell) {
        if (cell == null) return 0;
        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                return (int) cell.getNumericCellValue();
            } else if (cell.getCellType() == CellType.STRING) {
                String val = cell.getStringCellValue().trim();
                if (val.isEmpty()) return 0;

                // Remove caracteres não númericos
                val = val.replaceAll("[^0-9]", "");
                return Integer.parseInt(val);
            }
        } catch (Exception e) {
            return 0;
        }
        return 0;
    }
}
