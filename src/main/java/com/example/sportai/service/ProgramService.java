//package com.example.sportai.service;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.core.type.TypeReference;
//import org.springframework.stereotype.Service;
//import org.springframework.web.client.RestTemplate;
//
//import java.util.Map;
//
//@Service
//public class ProgramService {
//
//    private final ObjectMapper objectMapper = new ObjectMapper();
//    private final RestTemplate restTemplate = new RestTemplate();
//
//    public Map<String, String> getDay1() throws Exception {
//        String url = "https://ваш-сервер/chat/completions";
//
//        // тело запроса к LLM
//        Object request = Map.of(
//                "model", "openai/gpt-oss-120b",
//                "messages", new Object[]{ /* ... */ }
//        );
//
//        String raw = restTemplate.postForObject(url, request, String.class);
//
//        JsonNode root = objectMapper.readTree(raw);
//
//        // достаём content, который сам является JSON-строкой
//        String content = root.at("/choices/0/message/content").asText();
//
//        // парсим внутренний JSON
//        JsonNode days = objectMapper.readTree(content);
//
//        // ищем "День 1"
//        for (JsonNode day : days) {
//            if (day.has("День 1")) {
//                return objectMapper.convertValue(
//                        day.get("День 1"),
//                        new TypeReference<Map<String, String>>() {}
//                );
//            }
//        }
//
//        throw new RuntimeException("День 1 не найден");
//    }
//}