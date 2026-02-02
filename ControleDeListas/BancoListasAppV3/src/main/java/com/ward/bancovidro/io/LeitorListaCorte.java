package com.ward.bancovidro.io;

import com.ward.bancovidro.modelo.RegistroLista;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.FileInputStream;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.*;

public class LeitorListaCorte {

    public static List<RegistroLista> lerArquivo(File arquivo, String nomeObraManual) {
        List<RegistroLista> resultado = new ArrayList<>();
        Map<String, Stats> agrupamento = new HashMap<>();

        String dataHoje = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
        String listaDetectada = "S/N";

        try (FileInputStream fis = new FileInputStream(arquivo);
            Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet sheet = workbook.getSheetAt(0);

            // 1. Varredura do Cabeçalho
            for (int i = 0; i < 15; i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                for (Cell cell : row) {
                    String txt = lerCell(cell).toUpperCase();

                    if (txt.contains("LISTA DE VIDROS") || txt.contains("LISTA DE MEDIDAS") || txt.contains("RELAÇÃO")) {
                        String num = txt.replaceAll("[^0-9]", "");
                        if (!num.isEmpty()) listaDetectada = "L" + num;
                    }
                }
            }

            // 2. Descobrir Índices das Colunas Dinamicamente
            Iterator<Row> it = sheet.iterator();
            int colEspec = -1, colLarg = -1, colAlt = -1, colQtd = -1, colPosicao = -1;
            boolean headerAchado = false;

            while (it.hasNext()) {
                Row row = it.next();

                if (!headerAchado) {
                    for (Cell cell : row) {
                        String txt = removerAcentos(lerCell(cell).toUpperCase());
                        if (txt.contains("ESPECIFICACAO") || txt.contains("DESCRIÇÃO")) colEspec =
                                cell.getColumnIndex();
                        if (txt.equals("LARGURA (MM)") || txt.contains("LARGURA")) colLarg = cell.getColumnIndex();
                        if (txt.equals("ALTURA (MM)") || txt.contains("ALTURA")) colAlt = cell.getColumnIndex();
                        if (txt.contains("QUANT") || txt.contains("QTD")) colQtd = cell.getColumnIndex();
                        if (txt.equals("TIPOLOGIA") || txt.equals("POSICAO") || txt.equals("CODIGO") || txt.equals("TIPO")) {
                            colPosicao = cell.getColumnIndex();
                        }
                    }
                    if (colEspec != -1 && colLarg != -1 && colQtd != -1) headerAchado = true;
                    continue;
                }

                // 3. Ler Dados
                try {
                    String espec = lerCell(row.getCell(colEspec));
                    if (espec.isEmpty() || espec.contains("Especificação")) continue;

                    int qtd = (int) lerNum(row.getCell(colQtd));
                    double larg = lerNum(row.getCell(colLarg));
                    double alt = lerNum(row.getCell(colAlt));

                    String posicao = (colPosicao != -1) ? lerCell(row.getCell(colPosicao)) : "";

                    if (qtd > 0 && larg > 0) {
                        // Chave única para agrupar
                        String chave = espec + "|" + larg + "x" + alt;

                        agrupamento.putIfAbsent(chave, new Stats());
                        Stats st = agrupamento.get(chave);

                        st.nomeEspec = espec;
                        st.largura = larg;
                        st.altura = alt;
                        st.pecas += qtd;
                        st.area += (larg * alt * qtd) / 1000000.0;

                        if (!posicao.isEmpty() && !posicao.equals("0")) {
                            st.listaPosicoes.add(posicao);
                        }
                    }
                } catch (Exception ignored) {}
            }

            // 4. Converter Mapa para Lista de Registros
            for (Stats st : agrupamento.values()) {
                String detalhePosicoes = String.join(", ", st.listaPosicoes);

                if (detalhePosicoes.length() > 100) {
                    detalhePosicoes = detalhePosicoes.substring(0, 97) + "...";
                }
                if (detalhePosicoes.isEmpty()) detalhePosicoes = "-";

                resultado.add(new RegistroLista(
                        dataHoje,
                        nomeObraManual.toUpperCase(),
                        listaDetectada,
                        st.nomeEspec,
                        st.largura,
                        st.altura,
                        st.pecas,
                        st.area,
                        detalhePosicoes,
                        "Importado"
                ));
            }
        } catch (Exception e) { e.printStackTrace(); }

        return resultado;
    }

    // --Auxiliares---
    static class Stats {
        String nomeEspec;
        double largura;
        double altura;
        int pecas = 0;
        double area = 0;
        Set<String> listaPosicoes = new LinkedHashSet<>();
    }

    private static String lerCell(Cell c) {
        if (c == null) return "";
        try {
            if(c.getCellType()==CellType.NUMERIC) return String.valueOf((int)c.getNumericCellValue());
            return c.getStringCellValue();
        } catch (Exception e) { return ""; }
    }

    private static double lerNum(Cell c) {
        if (c == null) return 0;
        try { return c.getNumericCellValue(); } catch (Exception e) { return 0; }
    }

    private static String removerAcentos(String str) {
        return Normalizer.normalize(str, Normalizer.Form.NFD).replaceAll("[^\\p{ASCII}]", "");
    }
}
