package com.umss.sistemas.model;

public class Empleador {
    private String nombreEmpresa;
    private String tipoOrganizacion;
    private String tamanoOrganizacion;
    private String rubroSector;
    private boolean contratoUltimos5Anos;
    private String nivelFormacionDemandado;

    public Empleador(String nombreEmpresa, String tipoOrganizacion, String tamanoOrganizacion, 
                     String rubroSector, boolean contratoUltimos5Anos, String nivelFormacionDemandado) {
        this.nombreEmpresa = nombreEmpresa;
        this.tipoOrganizacion = tipoOrganizacion;
        this.tamanoOrganizacion = tamanoOrganizacion;
        this.rubroSector = rubroSector;
        this.contratoUltimos5Anos = contratoUltimos5Anos;
        this.nivelFormacionDemandado = nivelFormacionDemandado;
    }

    public String getNombreEmpresa() { return nombreEmpresa; }
    public String getTipoOrganizacion() { return tipoOrganizacion; }
    public String getTamanoOrganizacion() { return tamanoOrganizacion; }
    public String getRubroSector() { return rubroSector; }
    public boolean isContratoUltimos5Anos() { return contratoUltimos5Anos; }
    public String getNivelFormacionDemandado() { return nivelFormacionDemandado; }
}