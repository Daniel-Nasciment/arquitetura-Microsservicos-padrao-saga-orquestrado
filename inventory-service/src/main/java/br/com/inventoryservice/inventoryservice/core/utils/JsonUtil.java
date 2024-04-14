package br.com.inventoryservice.inventoryservice.core.utils;

import br.com.inventoryservice.inventoryservice.core.dto.Event;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class JsonUtil {

    private final ObjectMapper objectMapper;

    public String toJson(Object object) throws JsonProcessingException {
        return objectMapper.writeValueAsString(object);
    }

    public Event toEvent(String json) throws JsonProcessingException {
        return objectMapper.readValue(json, Event.class);
    }

}
