package com.umss.sistemas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MiAplicacion {
    public static void main(String[] args) {
        SpringApplication.run(MiAplicacion.class, args);
        System.out.println("==================================================");
        System.out.println("¡SERVIDOR WEB DE ANÁLISIS DE MERCADO INICIADO!");
        System.out.println("Abre en tu navegador: http://localhost:8080");
        System.out.println("==================================================");
    }
}