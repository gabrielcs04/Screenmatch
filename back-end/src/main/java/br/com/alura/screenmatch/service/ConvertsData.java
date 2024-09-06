package br.com.alura.screenmatch.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ConvertsData implements IConvertsData {
    private ObjectMapper mapper = new ObjectMapper();

    @Override
    public <T> T getData(String json, Class<T> category) {
        try {
            return mapper.readValue(json, category);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
