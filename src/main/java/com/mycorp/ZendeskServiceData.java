package com.mycorp;

import org.springframework.beans.factory.annotation.Value;

public class ZendeskServiceData {
	@Value("#{envPC['zendesk.ticket']}")
	private String PETICION_ZENDESK;
	@Value("#{envPC['zendesk.token']}")
	private String TOKEN_ZENDESK;
	@Value("#{envPC['zendesk.url']}")
	private String URL_ZENDESK;
	@Value("#{envPC['zendesk.user']}")
	private String ZENDESK_USER;
	@Value("#{envPC['tarjetas.getDatos']}")
	private String TARJETAS_GETDATOS;
	@Value("#{envPC['cliente.getDatos']}")
	private String CLIENTE_GETDATOS;
	@Value("#{envPC['zendesk.error.mail.funcionalidad']}")
	private String ZENDESK_ERROR_MAIL_FUNCIONALIDAD;
	@Value("#{envPC['zendesk.error.destinatario']}")
	private String ZENDESK_ERROR_DESTINATARIO;



	public ZendeskServiceData() {}
	
	public ZendeskServiceData(String pETICION_ZENDESK, String tOKEN_ZENDESK, String uRL_ZENDESK, String zENDESK_USER,
			String tARJETAS_GETDATOS, String cLIENTE_GETDATOS, String zENDESK_ERROR_MAIL_FUNCIONALIDAD,
			String zENDESK_ERROR_DESTINATARIO) {
		PETICION_ZENDESK = pETICION_ZENDESK;
		TOKEN_ZENDESK = tOKEN_ZENDESK;
		URL_ZENDESK = uRL_ZENDESK;
		ZENDESK_USER = zENDESK_USER;
		TARJETAS_GETDATOS = tARJETAS_GETDATOS;
		CLIENTE_GETDATOS = cLIENTE_GETDATOS;
		ZENDESK_ERROR_MAIL_FUNCIONALIDAD = zENDESK_ERROR_MAIL_FUNCIONALIDAD;
		ZENDESK_ERROR_DESTINATARIO = zENDESK_ERROR_DESTINATARIO;
	}
	
	public String getPETICION_ZENDESK() {
		return PETICION_ZENDESK;
	}

	public void setPETICION_ZENDESK(String pETICION_ZENDESK) {
		PETICION_ZENDESK = pETICION_ZENDESK;
	}

	public String getTOKEN_ZENDESK() {
		return TOKEN_ZENDESK;
	}

	public void setTOKEN_ZENDESK(String tOKEN_ZENDESK) {
		TOKEN_ZENDESK = tOKEN_ZENDESK;
	}

	public String getURL_ZENDESK() {
		return URL_ZENDESK;
	}

	public void setURL_ZENDESK(String uRL_ZENDESK) {
		URL_ZENDESK = uRL_ZENDESK;
	}

	public String getZENDESK_USER() {
		return ZENDESK_USER;
	}

	public void setZENDESK_USER(String zENDESK_USER) {
		ZENDESK_USER = zENDESK_USER;
	}

	public String getTARJETAS_GETDATOS() {
		return TARJETAS_GETDATOS;
	}

	public void setTARJETAS_GETDATOS(String tARJETAS_GETDATOS) {
		TARJETAS_GETDATOS = tARJETAS_GETDATOS;
	}

	public String getCLIENTE_GETDATOS() {
		return CLIENTE_GETDATOS;
	}

	public void setCLIENTE_GETDATOS(String cLIENTE_GETDATOS) {
		CLIENTE_GETDATOS = cLIENTE_GETDATOS;
	}

	public String getZENDESK_ERROR_MAIL_FUNCIONALIDAD() {
		return ZENDESK_ERROR_MAIL_FUNCIONALIDAD;
	}

	public void setZENDESK_ERROR_MAIL_FUNCIONALIDAD(String zENDESK_ERROR_MAIL_FUNCIONALIDAD) {
		ZENDESK_ERROR_MAIL_FUNCIONALIDAD = zENDESK_ERROR_MAIL_FUNCIONALIDAD;
	}

	public String getZENDESK_ERROR_DESTINATARIO() {
		return ZENDESK_ERROR_DESTINATARIO;
	}

	public void setZENDESK_ERROR_DESTINATARIO(String zENDESK_ERROR_DESTINATARIO) {
		ZENDESK_ERROR_DESTINATARIO = zENDESK_ERROR_DESTINATARIO;
	}
	
	
}