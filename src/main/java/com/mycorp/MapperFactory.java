package com.mycorp;

import com.fasterxml.jackson.databind.ObjectMapper;

public class MapperFactory {

    private static ObjectMapper mapper;
	
	private MapperFactory() {}
	
    public static ObjectMapper getMapper() {
    	if(mapper == null) {
    		mapper = new ObjectMapper();
    	}
        return mapper;
    }
	
	
}
