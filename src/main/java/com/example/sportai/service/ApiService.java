package com.example.sportai.service;

import com.example.sportai.dto.ChatResponse;
import com.example.sportai.dto.Req;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.resolver.DefaultAddressResolverGroup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.util.List;
import java.util.Map;

@Service
public class ApiService {

    private static final String EXERCISES = "push ups, bench press, incline dumbbell press, cable crossover, dips, pull ups, lat pulldown, bent-over row, seated cable row, dumbbell row, deadlift, romanian deadlift, back squat, leg press, lunges, bulgarian split squat, leg extension, leg curl, hip thrust, calf raise, shoulder press, lateral raise, rear delt fly, barbell curl, hammer curl, triceps pushdown, skull crushers, plank, hanging leg raise, russian twist";

    private final WebClient webClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ApiService(WebClient.Builder builder,
                      @Value("${groq.key.api}") String apiKey) {

        HttpClient httpClient = HttpClient.create()
                .resolver(DefaultAddressResolverGroup.INSTANCE);

        this.webClient = builder
                .baseUrl("https://api.groq.com")
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .build();
    }

    /**
     * Возвращает список дней программы.
     * Каждый элемент: { "День 1": { "push ups": ["3x12", "описание"], ... } }
     */
    public List<Map<String, Map<String, List<String>>>> generateWorkout(Req request) {
        String prompt = """
                Создай персональную программу тренировок.
                Для %s, %d лет, рост %d см, вес %d кг, цель: %s.
                Раньше занимался: %s, травмы: %s, готов заниматься %d дней в неделю.

                Верни программу с упражнениями, количеством подходов, повторений и временем отдыха.
                Можно использовать только упражнения из списка: %s

                Формат ответа строго json, без markdown, без пояснений:
                [
                  {"День 1": {"push ups": ["3x12", "Дополнительное описание, если не нужно — оставь пустым"], "bench press": ["4x11", ""]}},
                  {"День 2": {"pull ups": ["4x8", ""], "lat pulldown": ["3x10", ""]}},
                  {"День 3": {"back squat": ["4x8", ""], "romanian deadlift": ["3x10", ""]}}
                ]
                """.formatted(
                request.gender(), request.age(), request.height(), request.weight(),
                request.goal(), request.experience(), request.injuries(),
                request.dayOfWeek(), EXERCISES);

        Map<String, Object> body = Map.of(
                "model", "openai/gpt-oss-120b",
                "messages", List.of(Map.of("role", "user", "content", prompt)),
                "temperature", 0.2
        );

        String raw = webClient.post()
                .uri("/openai/v1/chat/completions")
                .bodyValue(body)
                .retrieve()
                .onStatus(HttpStatusCode::isError, resp ->
                        resp.bodyToMono(String.class)
                                .map(msg -> new IllegalStateException(
                                        "Groq вернул " + resp.statusCode() + ": " + msg)))
                .bodyToMono(String.class)
                .block();

        return parseProgram(raw);
    }

    private List<Map<String, Map<String, List<String>>>> parseProgram(String raw) {
        try {
            ChatResponse chat = objectMapper.readValue(raw, ChatResponse.class);
            String content = chat.choices().get(0).message().content();
            content = stripCodeFence(content);

            // временно — чтобы видеть, что реально вернула модель
            System.out.println("=== RAW CONTENT ===");
            System.out.println(content);
            System.out.println("===================");

            return objectMapper.readValue(
                    content,
                    new TypeReference<List<Map<String, Map<String, List<String>>>>>() {}
            );
        } catch (Exception e) {
            throw new IllegalStateException("Не удалось распарсить ответ Groq: " + raw, e);
        }
    }

    private String stripCodeFence(String content) {
        String trimmed = content.trim();
        if (trimmed.startsWith("```")) {
            int firstNewline = trimmed.indexOf('\n');
            if (firstNewline != -1) {
                trimmed = trimmed.substring(firstNewline + 1);
            }
            if (trimmed.endsWith("```")) {
                trimmed = trimmed.substring(0, trimmed.length() - 3);
            }
        }
        return trimmed.trim();
    }
}