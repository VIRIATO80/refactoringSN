package com.mycorp;


public class ClientesDAO {

   
	private StringBuilder datosServicio = new StringBuilder();
    
    private StringBuilder clientName = new StringBuilder();
    
    private StringBuilder datosUsuario = new StringBuilder();
    
    private StringBuilder datosBravo = new StringBuilder();
    
    private String idCliente;

    public ClientesDAO() {}
    
	public StringBuilder getDatosServicio() {
		return datosServicio;
	}

	public void setDatosServicio(StringBuilder datosServicio) {
		this.datosServicio = datosServicio;
	}

	public StringBuilder getClientName() {
		return clientName;
	}

	public void setClientName(StringBuilder clientName) {
		this.clientName = clientName;
	}

	public StringBuilder getDatosUsuario() {
		return datosUsuario;
	}

	public void setDatosUsuario(StringBuilder datosUsuario) {
		this.datosUsuario = datosUsuario;
	}

	public StringBuilder getDatosBravo() {
		return datosBravo;
	}

	public void setDatosBravo(StringBuilder datosBravo) {
		this.datosBravo = datosBravo;
	}

	public String getIdCliente() {
		return idCliente;
	}

	public void setIdCliente(String idCliente) {
		this.idCliente = idCliente;
	}
	
    
}
