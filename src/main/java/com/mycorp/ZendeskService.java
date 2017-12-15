package com.mycorp;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.mycorp.support.CorreoElectronico;
import com.mycorp.support.DatosCliente;
import com.mycorp.support.MensajeriaService;
import com.mycorp.support.Poliza;
import com.mycorp.support.PolizaBasicoFromPolizaBuilder;
import com.mycorp.support.Ticket;
import com.mycorp.support.ValueCode;

import portalclientesweb.ejb.interfaces.PortalClientesWebEJBRemote;
import util.datos.PolizaBasico;
import util.datos.UsuarioAlta;

@Service
public class ZendeskService {

    /** LOG. */
    private static final Logger LOG = LoggerFactory.getLogger( ZendeskService.class );
	
    private SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
    
 
	// Datos para el servicio
    @Autowired
    private ClientesDAO cliente;
    
	// Datos para el servicio
    @Autowired
    private ZendeskServiceData data;


    /** The portalclientes web ejb remote. */
    @Autowired
    // @Qualifier("portalclientesWebEJB")
    private PortalClientesWebEJBRemote portalclientesWebEJBRemote;

    /** The rest template. */
    @Autowired
    @Qualifier("restTemplateUTF8")
    private RestTemplate restTemplate;

    @Autowired
    @Qualifier( "emailService" )
    MensajeriaService emailService;

     
    /**
     * Crea un ticket en Zendesk. Si se ha informado el nÂº de tarjeta, obtiene los datos asociados a dicha tarjeta de un servicio externo.
     * @param usuarioAlta
     * @param userAgent
     */
    public String altaTicketZendesk(UsuarioAlta usuarioAlta, String userAgent){

        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        
        StringBuilder datosUsuario = configureDatosUsuario(usuarioAlta, userAgent);
        

        cliente.getDatosBravo().append(Constantes.ESCAPED_LINE_SEPARATOR + "Datos recuperados de BRAVO:" + Constantes.ESCAPED_LINE_SEPARATOR + Constantes.ESCAPED_LINE_SEPARATOR);
        

        // Obtiene el idCliente de la tarjeta
        if(StringUtils.isNotBlank(usuarioAlta.getNumTarjeta())){       	
        	recuperarDatosTarjeta(usuarioAlta);
        }
        else if(StringUtils.isNotBlank(usuarioAlta.getNumPoliza())){      	
        	recuperarDatosPoliza(usuarioAlta.getNumPoliza(), usuarioAlta.getNumDocAcreditativo());    	
       }

        recuperarDatosCliente();

        obtenerTicket(usuarioAlta);

        datosUsuario.append(cliente.getDatosBravo());

        return datosUsuario.toString();
    }
    
    
    
    public void recuperarDatosTarjeta(UsuarioAlta usuarioAlta) {

        try{
            String urlToRead = data.getTARJETAS_GETDATOS() + usuarioAlta.getNumTarjeta();
            ResponseEntity<String> res = restTemplate.getForEntity( urlToRead, String.class);
            if(res.getStatusCode() == HttpStatus.OK){
                String dusuario = res.getBody();
                cliente.getClientName().append(dusuario);
                cliente.getDatosServicio().append("Datos recuperados del servicio de tarjeta:").append(Constantes.ESCAPED_LINE_SEPARATOR).append(MapperFactory.getMapper().writeValueAsString(dusuario));
            }
        }catch(Exception e)
        {
            LOG.error("Error al obtener los datos de la tarjeta", e);
        }    	
    }
    
    
    private void recuperarDatosPoliza(String numPoliza, String documentoAcreditativo) {
        try
        {
            Poliza poliza = new Poliza();
            poliza.setNumPoliza(Integer.valueOf(numPoliza));
            poliza.setNumColectivo(Integer.valueOf(documentoAcreditativo));
            poliza.setCompania(1);

            PolizaBasico polizaBasicoConsulta = new PolizaBasicoFromPolizaBuilder().withPoliza( poliza ).build();

            final util.datos.DetallePoliza detallePolizaResponse = portalclientesWebEJBRemote.recuperarDatosPoliza(polizaBasicoConsulta);

             cliente.getClientName().append(detallePolizaResponse.getTomador().getNombre()).
                        append(" ").
                        append(detallePolizaResponse.getTomador().getApellido1()).
                        append(" ").
                        append(detallePolizaResponse.getTomador().getApellido2());
             
            cliente.getDatosServicio().append("Datos recuperados del servicio de tarjeta:").append(Constantes.ESCAPED_LINE_SEPARATOR).append(MapperFactory.getMapper().writeValueAsString(detallePolizaResponse));
        }catch(Exception e)
        {
            LOG.error("Error al obtener los datos de la poliza", e);
        }    	
    }
    
    
    private void obtenerTicket(UsuarioAlta usuarioAlta) {
    
    	String ticket = String.format(data.getPETICION_ZENDESK(), cliente.getClientName().toString(), usuarioAlta.getEmail(), cliente.getDatosUsuario().toString()+cliente.getDatosBravo().toString()+
                parseJsonBravo(cliente.getDatosServicio()));
        ticket = ticket.replaceAll("["+Constantes.ESCAPED_LINE_SEPARATOR+"]", " ");

        try {
       	
        	Zendesk zendesk = new Builder(data.getURL_ZENDESK(), data.getTOKEN_ZENDESK()).build();
    			
            //Ticket
            Ticket petiZendesk = MapperFactory.getMapper().readValue(ticket, Ticket.class);
            zendesk.createTicket(petiZendesk);

        }catch(Exception e){
            LOG.error("Error al crear ticket ZENDESK", e);
            // Send email

            CorreoElectronico correo = new CorreoElectronico( Long.parseLong(data.getZENDESK_ERROR_MAIL_FUNCIONALIDAD()), "es" )
                    .addParam(cliente.getDatosUsuario().toString().replaceAll(Constantes.ESCAPE_ER+Constantes.ESCAPED_LINE_SEPARATOR, Constantes.HTML_BR))
                    .addParam(cliente.getDatosBravo().toString().replaceAll(Constantes.ESCAPE_ER+Constantes.ESCAPED_LINE_SEPARATOR, Constantes.HTML_BR));
            correo.setEmailA( data.getZENDESK_ERROR_DESTINATARIO() );
            try
            {
                emailService.enviar( correo );
            }catch(Exception ex){
                LOG.error("Error al enviar mail", ex);
            }

        }    	
    }
    
    
    private StringBuilder configureDatosUsuario(UsuarioAlta usuarioAlta, String userAgent) {    
    	// AÃ±ade los datos del formulario
        if(StringUtils.isNotBlank(usuarioAlta.getNumPoliza())){
            cliente.getDatosUsuario().append("NÂº de poliza/colectivo: ").append(usuarioAlta.getNumPoliza()).append("/").append(usuarioAlta.getNumDocAcreditativo()).append(Constantes.ESCAPED_LINE_SEPARATOR);
        }else{
        	cliente.getDatosUsuario().append("NÂº tarjeta Sanitas o Identificador: ").append(usuarioAlta.getNumTarjeta()).append(Constantes.ESCAPED_LINE_SEPARATOR);
        }
        cliente.getDatosUsuario().append("Tipo documento: ").append(usuarioAlta.getTipoDocAcreditativo()).append(Constantes.ESCAPED_LINE_SEPARATOR);
        cliente.getDatosUsuario().append("NÂº documento: ").append(usuarioAlta.getNumDocAcreditativo()).append(Constantes.ESCAPED_LINE_SEPARATOR);
        cliente.getDatosUsuario().append("Email personal: ").append(usuarioAlta.getEmail()).append(Constantes.ESCAPED_LINE_SEPARATOR);
        cliente.getDatosUsuario().append("NÂº mÃ³vil: ").append(usuarioAlta.getNumeroTelefono()).append(Constantes.ESCAPED_LINE_SEPARATOR);
        cliente.getDatosUsuario().append("User Agent: ").append(userAgent).append(Constantes.ESCAPED_LINE_SEPARATOR);  
        return cliente.getDatosUsuario();
    }
    
 
    private void recuperarDatosCliente() {
        try
        {
            // Obtenemos los datos del cliente
            DatosCliente response = restTemplate.getForObject("http://localhost:8080/test-endpoint", DatosCliente.class, cliente.getIdCliente());

            cliente.getDatosBravo().append("TelÃ©fono: ").append(response.getGenTGrupoTmk()).append(Constantes.ESCAPED_LINE_SEPARATOR);


            cliente.getDatosBravo().append("Feha de nacimiento: ").append(formatter.format(formatter.parse(response.getFechaNacimiento()))).append(Constantes.ESCAPED_LINE_SEPARATOR);

            List< ValueCode > tiposDocumentos = getTiposDocumentosRegistro();
            for(int i = 0; i < tiposDocumentos.size();i++)
            {
                if(tiposDocumentos.get(i).getCode().equals(response.getGenCTipoDocumento().toString()))
                {
                	cliente.getDatosBravo().append("Tipo de documento: ").append(tiposDocumentos.get(i).getValue()).append(Constantes.ESCAPED_LINE_SEPARATOR);
                }
            }
            cliente.getDatosBravo().append("NÃºmero documento: ").append(response.getNumeroDocAcred()).append(Constantes.ESCAPED_LINE_SEPARATOR);

            cliente.getDatosBravo().append("Tipo cliente: ");
            switch (response.getGenTTipoCliente()) {
            case 1:
            	cliente.getDatosBravo().append("POTENCIAL").append(Constantes.ESCAPED_LINE_SEPARATOR);
                break;
            case 2:
            	cliente.getDatosBravo().append("REAL").append(Constantes.ESCAPED_LINE_SEPARATOR);
                break;
            case 3:
            	cliente.getDatosBravo().append("PROSPECTO").append(Constantes.ESCAPED_LINE_SEPARATOR);
                break;
            }

            cliente.getDatosBravo().append("ID estado del cliente: ").append(response.getGenTStatus()).append(Constantes.ESCAPED_LINE_SEPARATOR);

            cliente.getDatosBravo().append("ID motivo de alta cliente: ").append(response.getIdMotivoAlta()).append(Constantes.ESCAPED_LINE_SEPARATOR);

            cliente.getDatosBravo().append("Registrado: ").append(response.getfInactivoWeb() == null ? "SÃ­" : "No").append(Constantes.ESCAPED_LINE_SEPARATOR + Constantes.ESCAPED_LINE_SEPARATOR);


        }catch(Exception e)
        {
            LOG.error("Error al obtener los datos en BRAVO del cliente", e);
        }    
    }
    
    

    public List< ValueCode > getTiposDocumentosRegistro() {
        return Arrays.asList( new ValueCode(), new ValueCode() ); // simulacion servicio externo
    }

    /**
     * MÃ©todo para parsear el JSON de respuesta de los servicios de tarjeta/pÃ³liza
     *
     * @param resBravo
     * @return
     */
    private String parseJsonBravo(StringBuilder resBravo)
    {
        return resBravo.toString().replaceAll("[\\[\\]\\{\\}\\\"\\r]", "").replaceAll(Constantes.ESCAPED_LINE_SEPARATOR, Constantes.ESCAPE_ER + Constantes.ESCAPED_LINE_SEPARATOR);
    }
}