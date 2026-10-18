package com.umss.sistemas.model;

public class Titulado {
    private String id;
    private String correo;
    private int edad;
    private String programaPosgrado;
    private String areaInteresPosgrado;
    private String fuenteFinanciamiento;
    private int anosVidaProfesional;
    private int anosDesempleo;
    private String situacionLaboral;

    public Titulado(String id, String correo, int edad, String programaPosgrado, 
                    String areaInteresPosgrado, String fuenteFinanciamiento, 
                    int anosVidaProfesional, int anosDesempleo, String situacionLaboral) {
        this.id = id;
        this.correo = correo;
        this.edad = edad;
        this.programaPosgrado = programaPosgrado;
        this.areaInteresPosgrado = areaInteresPosgrado;
        this.fuenteFinanciamiento = fuenteFinanciamiento;
        this.anosVidaProfesional = anosVidaProfesional;
        this.anosDesempleo = anosDesempleo;
        this.situacionLaboral = situacionLaboral;
    }

    public String getId() { return id; }
    public String getCorreo() { return correo; }
    public int getEdad() { return edad; }
    public String getProgramaPosgrado() { return programaPosgrado; }
    public String getAreaInteresPosgrado() { return areaInteresPosgrado; }
    public String getFuenteFinanciamiento() { return fuenteFinanciamiento; }
    public int getAnosVidaProfesional() { return anosVidaProfesional; }
    public int getAnosDesempleo() { return anosDesempleo; }
    public String getSituacionLaboral() { return situacionLaboral; }
}