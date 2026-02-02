package com.ward.bancovidro.io;

import com.ward.bancovidro.modelo.RegistroLista;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.FileInputStream;
import java.text.Normalizer;
import java.text.SimpleDateFormat;
import java.util.*;

public class LeitorListaCorte {

    public static List<RegistroLista> lerArquivo(File arquivo) {
        List<RegistroLista> resultado = new ArrayList<>();
        Map<String, Stats> agrupamento = new HashMap<>();   // Agrupamento por Tipologia

        String dataHoje = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
        String obraDetectada = "Desconhecida";
        String listaDetectada = "S/N";

        try (FileInputStream fis = new FileInputStream(arquivo);
            Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet sheet = workbook.getSheetAt(0);

            // 1. Varredura do Cabeçalho
            for (int i = 0; i < 10; i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                for (Cell cell : row) {
                    String txt = lerCell(cell).toUpperCase();

                    if (txt.equals("OBRA:")) {
                        obraDetectada = txt.replace("OBRA:", "").replace("CLIENTE:", "").trim();
                        if (obraDetectada.contains("-")) obraDetectada =
                                obraDetectada.split("-")[0].trim() + " " + obraDetectada.split("-")[1].trim();
                    }
                    if (txt.contains("LISTA DE VIDROS") || txt.contains("LISTA DE MEDIDAS")) {
                        listaDetectada = txt.replaceAll("[^0-9]", "");
                        if (!listaDetectada.isEmpty()) listaDetectada = "Lista" + listaDetectada;
                        else listaDetectada = "Geral";
                    }
                }
            }

            // 2. Descobrir Índices das Colunas Dinamicamente
            Iterator<Row> it = sheet.iterator();
            int colEspec = -1;
            int colLarg = -1;
            int colAlt = -1;
            int colQtd = -1;
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

                    if (qtd > 0 && larg > 0) {  // Agrupa por especificação para somar m ao quadrado
                        agrupamento.putIfAbsent(espec, new Stats());
                        Stats st = agrupamento.get(espec);
                        st.pecas += qtd;
                        st.area += (larg * alt * qtd) / 1000000.0;
                    }
                } catch (Exception ignored) {}
            }

            // 4. Converter Mapa para Lista de Registros
            for (Map.Entry<String, Stats> entry : agrupamento.entrySet()) {
                resultado.add(new RegistroLista(
                        UUID.randomUUID().toString(),
                        dataHoje,
                        obraDetectada,
                        listaDetectada,
                        entry.getKey(),
                        entry.getValue().pecas,
                        entry.getValue().area
                ));
            }
        } catch (Exception e) { e.printStackTrace(); }

        return resultado;
    }

    // --Auxiliares---
    static class Stats { int pecas=0; double area=0; }

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
