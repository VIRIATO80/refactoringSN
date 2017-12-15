package com.mycorp;



import java.util.Collections;
import java.util.regex.Pattern;

import com.mycorp.support.Ticket;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.ListenableFuture;
import com.ning.http.client.Realm;
import com.ning.http.client.Request;
import com.ning.http.client.RequestBuilder;
import com.ning.http.client.uri.Uri;

public class Zendesk extends BasicAsyncCompletionHandler {

	private static final String JSON = "application/json; charset=UTF-8";
    private final Builder builder;
    private Realm realm;
    private final String oauthToken;

    private static final Pattern RESTRICTED_PATTERN = Pattern.compile("%2B", Pattern.LITERAL);

    
    
    public Zendesk(Builder builder) {
    	super(Zendesk.class, "Zendesk");
        this.oauthToken = null;
        this.builder = builder;
        init();

    }

    
    private void init() {
       
    	if(builder.getClient() == null) {
        	builder.setClient(new AsyncHttpClient());
        }       
    	if(builder.getUrl().endsWith("/")) {
    		builder.setUrl(builder.getUrl()+"api/v2");
    	}else {
    		builder.setUrl(builder.getUrl()+"/api/v2");    		
    	}
        if (builder.getUsername() != null) {
            this.realm = new Realm.RealmBuilder()
                    .setScheme(Realm.AuthScheme.BASIC)
                    .setPrincipal(builder.getUsername())
                    .setPassword(builder.getPassword())
                    .setUsePreemptiveAuth(true)
                    .build();
        } else {
            if (builder.getPassword() != null) {
                throw new IllegalStateException("Cannot specify token or password without specifying username");
            }
            this.realm = null;
        }
    }

    
    public Ticket createTicket(Ticket ticket) {
        return complete(submit(req("POST", cnst("/tickets.json"),
                        JSON, json(Collections.singletonMap("ticket", ticket))),
                handle(Ticket.class, "ticket")));
    }



    private Request req(String method, Uri template, String contentType, byte[] body) {
        RequestBuilder builder = new RequestBuilder(method);
        if (realm != null) {
            builder.setRealm(realm);
        } else {
            builder.addHeader("Authorization", "Bearer " + oauthToken);
        }
        builder.setUrl(RESTRICTED_PATTERN.matcher(template.toString()).replaceAll("+")); //replace out %2B with + due to API restriction
        builder.addHeader("Content-type", contentType);
        builder.setBody(body);
        return builder.build();
    }

    private <T> ListenableFuture<T> submit(Request request, ZendeskAsyncCompletionHandler<T> handler) {
        if (logger.isDebugEnabled()) {
            if (request.getStringData() != null) {
                logger.debug("Request {} {}\n{}", request.getMethod(), request.getUrl(), request.getStringData());
            } else if (request.getByteData() != null) {
                logger.debug("Request {} {} {} {} bytes", request.getMethod(), request.getUrl(),
                        request.getHeaders().getFirstValue("Content-type"), request.getByteData().length);
            } else {
                logger.debug("Request {} {}", request.getMethod(), request.getUrl());
            }
        }
        return builder.getClient().executeRequest(request, handler);
    }
    
    

    private Uri cnst(String template) {
        return Uri.create(builder.getUrl() + template);
    }



}