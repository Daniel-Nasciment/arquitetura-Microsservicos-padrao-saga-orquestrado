package br.com.orderservice.orderservice.core.utils;

import br.com.orderservice.orderservice.core.document.Event;
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
