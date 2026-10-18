package com.umss.sistemas.service;

import com.umss.sistemas.model.Empleador;
import com.umss.sistemas.model.Titulado;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.text.Normalizer;
import java.util.*;

@Service
public class ExcelService {

    public List<Titulado> procesarTitulados(MultipartFile file) {
        List<Titulado> lista = new ArrayList<>();
        try (InputStream is = file.getInputStream();
                Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    String id = getCellValue(row, 0);
                    String correo = getCellValue(row, 3);
                    int edad = parseSafelyInt(getCellValue(row, 8));
                    int anosProf = parseSafelyInt(getCellValue(row, 43)); // Columna de años profesionales
                    int anosDesempleo = parseSafelyInt(getCellValue(row, 46));
                    String programa = getCellValue(row, 17);
                    String areaInteres = getCellValue(row, 32);
                    String financiamiento = getCellValue(row, 23);
                    String situacion = getCellValue(row, 49);

                    lista.add(new Titulado(id, correo, edad, programa, areaInteres,
                            financiamiento, anosProf, anosDesempleo, situacion));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }

    public List<Empleador> procesarEmpleadores(MultipartFile file) {
        List<Empleador> lista = new ArrayList<>();
        try (InputStream is = file.getInputStream();
                Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    String empresa = getCellValue(row, 2);
                    String tipoOrg = getCellValue(row, 3);
                    String tamano = getCellValue(row, 4);
                    String rubro = getCellValue(row, 5);
                    boolean contrato = getCellValue(row, 6).equalsIgnoreCase("SI");
                    String nivelDemanda = getCellValue(row, 7);

                    lista.add(new Empleador(empresa, tipoOrg, tamano, rubro, contrato, nivelDemanda));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }

    // --- CÁLCULOS ESTADÍSTICOS (HU1, HU2, HU3, HU4) ---

    public double calcularMediaExperiencia(List<Titulado> titulados) {
        if (titulados.isEmpty())
            return 0.0;
        int suma = 0;
        for (Titulado t : titulados) {
            suma += t.getAnosVidaProfesional();
        }
        return (double) suma / titulados.size();
    }

    public Map<String, Long> obtenerFrecuenciaAreas(List<Titulado> titulados) {
        Map<String, Long> frecuencias = new HashMap<>();
        for (Titulado t : titulados) {
            String area = t.getAreaInteresPosgrado();
            if (area != null && !area.isEmpty()) {
                frecuencias.put(area, frecuencias.getOrDefault(area, 0L) + 1);
            }
        }
        return frecuencias;
    }

    /**
     * HU4: media (escala 1-5) de dos afirmaciones de la encuesta de empleadores:
     *  - "competencias": la afirmación sobre competencias laborales demostradas por los titulados
     *    (comunicación efectiva, resolución de problemas, pensamiento crítico, trabajo en equipo, TIC...).
     *  - "valores": la afirmación sobre valores y actitudes (ética, responsabilidad social, inclusión...).
     *
     * Las columnas se localizan por el texto del encabezado. Las respuestas pueden ser números (1-5)
     * o texto de una escala de acuerdo ("Totalmente de acuerdo", "De acuerdo", ...) o de frecuencia
     * ("Siempre", "Casi siempre", ...). Si no se puede leer un grupo, su clave no existe en el mapa
     * (la vista mostrará "Sin datos").
     */
    public Map<String, Double> calcularCompetencias(MultipartFile file) {
        Map<String, Double> resultado = new HashMap<>();
        try (InputStream is = file.getInputStream();
                Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            Row header = sheet.getRow(0);
            if (header == null)
                return resultado;

            DataFormatter formatter = new DataFormatter();
            double sumaComp = 0, sumaVal = 0;
            int nComp = 0, nVal = 0;

            for (int c = 0; c < header.getLastCellNum(); c++) {
                String titulo = normalizar(getCellValue(header, c));
                boolean esComp = titulo.contains("competencias laborales");
                boolean esVal = titulo.contains("valores y actitudes");
                if (!esComp && !esVal)
                    continue;

                double suma = 0;
                int leidas = 0;
                Set<String> noReconocidas = new LinkedHashSet<>();
                for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                    Row row = sheet.getRow(r);
                    if (row == null)
                        continue;
                    Cell cell = row.getCell(c);
                    if (cell == null)
                        continue;
                    String texto = formatter.formatCellValue(cell).trim();
                    if (texto.isEmpty())
                        continue;
                    Double v = leerLikert(cell, texto);
                    if (v != null) {
                        suma += v;
                        leidas++;
                    } else if (noReconocidas.size() < 5) {
                        noReconocidas.add(texto);
                    }
                }

                System.out.println("[COMPETENCIAS] COL " + c + " (" + (esComp ? "competencias" : "valores")
                        + "): " + leidas + " respuestas leídas"
                        + (noReconocidas.isEmpty() ? "" : " | NO reconocidas: " + noReconocidas));

                if (esComp) {
                    sumaComp += suma;
                    nComp += leidas;
                } else {
                    sumaVal += suma;
                    nVal += leidas;
                }
            }

            if (nComp > 0)
                resultado.put("competencias", sumaComp / nComp);
            if (nVal > 0)
                resultado.put("valores", sumaVal / nVal);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultado;
    }

    // --- Utilidades ---

    private String normalizar(String texto) {
        if (texto == null)
            return "";
        String sinTildes = Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return sinTildes.toLowerCase().trim();
    }

    // Convierte una respuesta a un valor 1-5 (número, o texto de escala de acuerdo/frecuencia)
    private Double leerLikert(Cell cell, String texto) {
        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                double v = cell.getNumericCellValue();
                return (v >= 1 && v <= 5) ? Double.valueOf(v) : null;
            }
        } catch (Exception ignored) {
        }

        String s = normalizar(texto);
        if (s.isEmpty())
            return null;

        // "4", "4 - De acuerdo", etc.
        if (s.charAt(0) >= '1' && s.charAt(0) <= '5' && (s.length() == 1 || !Character.isDigit(s.charAt(1)))) {
            return Double.valueOf(s.charAt(0) - '0');
        }

        // Escala de acuerdo (el orden importa: de lo más específico a lo más general)
        if (s.contains("totalmente en desacuerdo") || s.contains("muy en desacuerdo")) return 1.0;
        if (s.contains("totalmente de acuerdo") || s.contains("muy de acuerdo")) return 5.0;
        if (s.contains("ni de acuerdo") || s.contains("neutral") || s.contains("indiferente")) return 3.0;
        if (s.contains("en desacuerdo")) return 2.0;
        if (s.contains("de acuerdo")) return 4.0;

        // Escala de frecuencia
        if (s.contains("casi nunca")) return 2.0;
        if (s.contains("nunca")) return 1.0;
        if (s.contains("a veces") || s.contains("algunas veces")) return 3.0;
        if (s.contains("casi siempre")) return 4.0;
        if (s.contains("siempre")) return 5.0;

        return null;
    }

    private String getCellValue(Row row, int cellIndex) {
        Cell cell = row.getCell(cellIndex);
        if (cell == null)
            return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                return String.valueOf((int) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
        }
    }

    private int parseSafelyInt(String val) {
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public Map<String, Object> calcularChiCuadradoFinanciamiento(List<Titulado> titulados) {
        Map<String, Map<String, Long>> tablaContingencia = new java.util.HashMap<>();
        java.util.Set<String> fuentes = new java.util.HashSet<>();

        // 1. Construir Tabla de Contingencia (Área vs Fuente de Financiamiento)
        for (Titulado t : titulados) {
            String area = t.getAreaInteresPosgrado() != null ? t.getAreaInteresPosgrado() : "Sin Especificar";
            String fuente = t.getFuenteFinanciamiento() != null ? t.getFuenteFinanciamiento() : "Recursos Propios";

            fuentes.add(fuente);
            tablaContingencia.putIfAbsent(area, new java.util.HashMap<>());
            Map<String, Long> fila = tablaContingencia.get(area);
            fila.put(fuente, fila.getOrDefault(fuente, 0L) + 1L);
        }

        // 2. Cálculo simplificado del Estadístico Chi-Cuadrado (χ²)
        double chiCuadrado = 0.0;
        long totalGeneral = titulados.size();

        if (totalGeneral > 0) {
            for (String area : tablaContingencia.keySet()) {
                long totalFila = tablaContingencia.get(area).values().stream().mapToLong(Long::longValue).sum();
                for (String fuente : fuentes) {
                    long observada = tablaContingencia.get(area).getOrDefault(fuente, 0L);

                    // Calcular Frecuencia Esperada: (Total Fila * Total Columna) / Total General
                    long totalColumna = titulados.stream()
                            .filter(t -> fuente.equals(t.getFuenteFinanciamiento()))
                            .count();

                    double esperada = (double) (totalFila * totalColumna) / totalGeneral;
                    if (esperada > 0) {
                        chiCuadrado += Math.pow(observada - esperada, 2) / esperada;
                    }
                }
            }
        }

        int gradosLibertad = Math.max(1, (tablaContingencia.size() - 1) * (Math.max(1, fuentes.size() - 1)));
        boolean esDependiente = chiCuadrado > 9.488; // Valor crítico de referencia para α = 0.05

        Map<String, Object> resultado = new java.util.HashMap<>();
        resultado.put("tablaContingencia", tablaContingencia);
        resultado.put("fuentes", fuentes);
        resultado.put("chiCuadrado", Math.round(chiCuadrado * 100.0) / 100.0);
        resultado.put("gradosLibertad", gradosLibertad);
        resultado.put("esDependiente", esDependiente);

        return resultado;
    }
}