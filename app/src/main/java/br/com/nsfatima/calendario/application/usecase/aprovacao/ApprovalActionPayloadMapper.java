package br.com.nsfatima.calendario.application.usecase.aprovacao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class ApprovalActionPayloadMapper {

    private final ObjectMapper objectMapper;

    public ApprovalActionPayloadMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String toJson(Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Unable to serialize approval action payload", ex);
        }
    }

    public Map<String, Object> toMap(String payloadJson) {
        if (payloadJson == null || payloadJson.isBlank()) {
            return Map.of();
        }
        try {
            return objectMapper.readValue(payloadJson, objectMapper.getTypeFactory()
                    .constructMapType(Map.class, String.class, Object.class));
        } catch (JsonProcessingException ex) {
            throw new IllegalArgumentException("Unable to deserialize approval action payload", ex);
        }
    }
}
