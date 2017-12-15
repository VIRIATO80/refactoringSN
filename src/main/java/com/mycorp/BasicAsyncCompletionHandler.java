package com.mycorp;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.ning.http.client.ListenableFuture;
import com.ning.http.client.Response;

class BasicAsyncCompletionHandler<T> extends ZendeskAsyncCompletionHandler<T> {
    private final Class<T> clazz;
    private final String name;
    private final Class[] typeParams;
    public final Logger logger;
    public final ObjectMapper mapper;
    
    
    public BasicAsyncCompletionHandler(Class clazz, String name, Class... typeParams) {
        this.clazz = clazz;
        this.name = name;
        this.typeParams = typeParams;
        this.logger = LoggerFactory.getLogger(Zendesk.class);
        this.mapper = createMapper();
    }

    @Override
    public T onCompleted(Response response) throws Exception {
        logResponse(response);
        if (isStatus2xx(response)) {
            if (typeParams.length > 0) {
                JavaType type = mapper.getTypeFactory().constructParametricType(clazz, typeParams);
                return mapper.convertValue(mapper.readTree(response.getResponseBodyAsStream()).get(name), type);
            }
            return mapper.convertValue(mapper.readTree(response.getResponseBodyAsStream()).get(name), clazz);
        } else if (isRateLimitResponse(response)) {
            throw new ZendeskException(response.toString());
        }
        if (response.getStatusCode() == 404) {
            return null;
        }
        throw new ZendeskException(response.toString());
    }
    
    
    private void logResponse(Response response) throws IOException {
        if (logger.isDebugEnabled()) {
            logger.debug("Response HTTP/{} {}\n{}", response.getStatusCode(), response.getStatusText(),
                    response.getResponseBody());
        }
        if (logger.isTraceEnabled()) {
            logger.trace("Response headers {}", response.getHeaders());
        }
    }

    private boolean isRateLimitResponse(Response response) {
        return response.getStatusCode() == 429;
    }
    
    private boolean isStatus2xx(Response response) {
        return response.getStatusCode() / 100 == 2;
    }
    


    protected <T> ZendeskAsyncCompletionHandler<T> handle(final Class<T> clazz, final String name, final Class... typeParams) {
        return new BasicAsyncCompletionHandler<T>(clazz, name, typeParams);
    }



    //////////////////////////////////////////////////////////////////////
    // Static helper methods
    //////////////////////////////////////////////////////////////////////

    public static <T> T complete(ListenableFuture<T> future) {
        try {
            return future.get();
        } catch (InterruptedException e) {
            throw new ZendeskException(e.getMessage(), e);
        } catch (ExecutionException e) {
            if (e.getCause() instanceof ZendeskException) {
                throw (ZendeskException) e.getCause();
            }
            throw new ZendeskException(e.getMessage(), e);
        }
    }
    
    
    public static ObjectMapper createMapper() {
        ObjectMapper mapper = MapperFactory.getMapper();
        mapper.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
        mapper.enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper;
    }
    
    public byte[] json(Object object) {
        try {
            return mapper.writeValueAsBytes(object);
        } catch (JsonProcessingException e) {
            throw new ZendeskException(e.getMessage(), e);
        }
    }
    
}