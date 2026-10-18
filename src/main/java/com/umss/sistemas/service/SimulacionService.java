package com.umss.sistemas.service;

import com.umss.sistemas.model.Titulado;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class SimulacionService {

    /**
     * Simulación de Montecarlo: Proyecta la inserción laboral y demanda de posgrados
     * para N nuevos egresados simulados (ej. 1,000 iteraciones).
     */
    public Map<String, Object> ejecutarSimulacionMontecarlo(List<Titulado> historicos, int numIteraciones) {
        if (historicos.isEmpty()) return Collections.emptyMap();

        Random random = new Random();
        
        List<Integer> tiemposReales = new ArrayList<>();
        for (Titulado t : historicos) {
            tiemposReales.add(t.getAnosDesempleo() * 12);
        }

        double mediaMeses = tiemposReales.stream().mapToInt(Integer::intValue).average().orElse(6.0);
        double desvioMeses = Math.sqrt(tiemposReales.stream()
                .mapToDouble(i -> Math.pow(i - mediaMeses, 2)).average().orElse(2.0));

        List<Double> resultadosSimulacionDemora = new ArrayList<>();
        int altaDemandaPosgradoCount = 0;

        for (int i = 0; i < numIteraciones; i++) {
            double u1 = random.nextDouble();
            double u2 = random.nextDouble();
            double z = Math.sqrt(-2.0 * Math.log(u1)) * Math.cos(2.0 * Math.PI * u2);
            
            double tiempoSimulado = Math.max(0, mediaMeses + (z * desvioMeses));
            resultadosSimulacionDemora.add(tiempoSimulado);

            if (random.nextDouble() < 0.72) {
                altaDemandaPosgradoCount++;
            }
        }

        double mediaSimulada = resultadosSimulacionDemora.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

        Map<String, Object> resultadoSimulacion = new HashMap<>();
        resultadoSimulacion.put("numIteraciones", numIteraciones);
        resultadoSimulacion.put("mediaTiempoConsolidacionMeses", Math.round(mediaSimulada * 100.0) / 100.0);
        resultadoSimulacion.put("proyeccionDemandaPosgradosIA", altaDemandaPosgradoCount);
        resultadoSimulacion.put("porcentajeProyectadoIA", (altaDemandaPosgradoCount * 100.0) / numIteraciones);

        return resultadoSimulacion;
    }
}