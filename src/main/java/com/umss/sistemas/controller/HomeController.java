package com.umss.sistemas.controller;

import com.umss.sistemas.model.Empleador;
import com.umss.sistemas.model.Titulado;
import com.umss.sistemas.model.TituladoEntity;
import com.umss.sistemas.repository.TituladoRepository;
import com.umss.sistemas.service.ExcelService;
import com.umss.sistemas.service.SimulacionService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController {

    @Autowired
    private ExcelService excelService;

    @Autowired
    private SimulacionService simulacionService;

    @Autowired(required = false)
    private TituladoRepository tituladoRepository;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @GetMapping("/nuevo-titulado")
    public String mostrarFormularioNuevo(Model model) {
        model.addAttribute("nuevoTitulado", new TituladoEntity());
        return "nuevo_titulado";
    }

    @PostMapping("/guardar-titulado")
    public String guardarTitulado(@ModelAttribute("nuevoTitulado") TituladoEntity titulado) {
        if (tituladoRepository != null) {
            tituladoRepository.save(titulado);
        }
        return "redirect:/";
    }

    @GetMapping("/exportar/csv")
    public void exportarCsvTitulados(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"reporte_estrategico_arcusur.csv\"");

        PrintWriter writer = response.getWriter();
        writer.println("ID,Correo,Edad,AnosExperiencia,AreaInteresPosgrado,FuenteFinanciamiento");

        if (tituladoRepository != null) {
            List<TituladoEntity> titulados = tituladoRepository.findAll();
            for (TituladoEntity t : titulados) {
                writer.printf("%d,%s,%d,%d,%s,%s\n",
                    t.getId() != null ? t.getId() : 0,
                    t.getCorreo() != null ? t.getCorreo() : "",
                    t.getEdad(),
                    t.getAnosVidaProfesional(),
                    t.getAreaInteresPosgrado() != null ? t.getAreaInteresPosgrado() : "",
                    t.getFuenteFinanciamiento() != null ? t.getFuenteFinanciamiento() : ""
                );
            }
        }
        writer.flush();
    }

    @PostMapping("/analizar")
    public String analizarDatos(@RequestParam("fileTitulados") MultipartFile fileTitulados,
                                @RequestParam("fileEmpleadores") MultipartFile fileEmpleadores,
                                Model model) {
        
        // 1. Procesamos los archivos Excel mediante el servicio
        List<Titulado> titulados = excelService.procesarTitulados(fileTitulados);
        List<Empleador> empleadores = excelService.procesarEmpleadores(fileEmpleadores);

        // NUEVO: Guardar también los titulados del Excel en la base de datos Neon para unificar el reporte
        if (tituladoRepository != null && titulados != null) {
            for (Titulado t : titulados) {
                TituladoEntity entity = new TituladoEntity();
                entity.setCorreo(t.getCorreo());
                entity.setEdad(t.getEdad());
                // Asegúrate de usar el método getter exacto que tiene tu clase Titulado (ej: getAnosVidaProfesional o getAnosExperiencia)
                entity.setAnosVidaProfesional(t.getAnosVidaProfesional());
                entity.setAreaInteresPosgrado(t.getAreaInteresPosgrado());
                entity.setFuenteFinanciamiento(t.getFuenteFinanciamiento());
                tituladoRepository.save(entity);
            }
        }

        // 2. Calculamos las métricas requeridas por las Historias de Usuario
        double mediaExperiencia = excelService.calcularMediaExperiencia(titulados);
        Map<String, Long> frecuenciasAreas = excelService.obtenerFrecuenciaAreas(titulados);

        // HU4: medias de competencias (escala Likert 1-5) leídas del Excel de empleadores.
        // Si no se detecta un grupo de columnas, el valor es null y la vista muestra "Sin datos".
        Map<String, Double> competencias = excelService.calcularCompetencias(fileEmpleadores);
        model.addAttribute("mediaCompetencias", competencias.get("competencias"));
        model.addAttribute("mediaValores", competencias.get("valores"));

        // 3. Ejecutamos la Simulación de Montecarlo (1,000 iteraciones estocásticas)
        Map<String, Object> resultadoSimulacion = simulacionService.ejecutarSimulacionMontecarlo(titulados, 1000);

        // 4. Cálculo bivariado de Chi-Cuadrado para HU3
        Map<String, Object> pruebaChi2 = excelService.calcularChiCuadradoFinanciamiento(titulados);
        model.addAttribute("pruebaChi2", pruebaChi2);

        // 5. Pasamos las métricas y simulación a la vista HTML
        model.addAttribute("totalTitulados", titulados.size());
        model.addAttribute("totalEmpleadores", empleadores.size());
        model.addAttribute("mediaExperiencia", mediaExperiencia);
        model.addAttribute("frecuenciasAreas", frecuenciasAreas);
        model.addAttribute("resultadoSimulacion", resultadoSimulacion);
        model.addAttribute("titulados", titulados);
        model.addAttribute("empleadores", empleadores);

        return "resultado";
    }
}