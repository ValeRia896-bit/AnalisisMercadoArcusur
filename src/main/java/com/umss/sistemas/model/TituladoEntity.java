package com.umss.sistemas.model;

import jakarta.persistence.*;

@Entity
@Table(name = "titulados")
public class TituladoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String correo;
    private int edad;
    private int anosVidaProfesional;
    private int anosDesempleo;
    private String programaPosgrado;
    private String areaInteresPosgrado;
    private String fuenteFinanciamiento;
    private String situacionLaboral;

    public TituladoEntity() {}

    public TituladoEntity(String correo, int edad, int anosVidaProfesional, int anosDesempleo, 
                         String programaPosgrado, String areaInteresPosgrado, 
                         String fuenteFinanciamiento, String situacionLaboral) {
        this.correo = correo;
        this.edad = edad;
        this.anosVidaProfesional = anosVidaProfesional;
        this.anosDesempleo = anosDesempleo;
        this.programaPosgrado = programaPosgrado;
        this.areaInteresPosgrado = areaInteresPosgrado;
        this.fuenteFinanciamiento = fuenteFinanciamiento;
        this.situacionLaboral = situacionLaboral;
    }

    // Getters
    public Long getId() { return id; }
    public String getCorreo() { return correo; }
    public int getEdad() { return edad; }
    public int getAnosVidaProfesional() { return anosVidaProfesional; }
    public int getAnosDesempleo() { return anosDesempleo; }
    public String getProgramaPosgrado() { return programaPosgrado; }
    public String getAreaInteresPosgrado() { return areaInteresPosgrado; }
    public String getFuenteFinanciamiento() { return fuenteFinanciamiento; }
    public String getSituacionLaboral() { return situacionLaboral; }

    // Setters (Añadidos para permitir la persistencia desde el controlador)
    public void setId(Long id) { this.id = id; }
    public void setCorreo(String correo) { this.correo = correo; }
    public void setEdad(int edad) { this.edad = edad; }
    public void setAnosVidaProfesional(int anosVidaProfesional) { this.anosVidaProfesional = anosVidaProfesional; }
    public void setAnosDesempleo(int anosDesempleo) { this.anosDesempleo = anosDesempleo; }
    public void setProgramaPosgrado(String programaPosgrado) { this.programaPosgrado = programaPosgrado; }
    public void setAreaInteresPosgrado(String areaInteresPosgrado) { this.areaInteresPosgrado = areaInteresPosgrado; }
    public void setFuenteFinanciamiento(String fuenteFinanciamiento) { this.fuenteFinanciamiento = fuenteFinanciamiento; }
    public void setSituacionLaboral(String situacionLaboral) { this.situacionLaboral = situacionLaboral; }
}