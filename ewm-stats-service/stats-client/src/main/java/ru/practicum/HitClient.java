package ru.practicum;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class HitClient extends BaseClient {
    private final ObjectMapper objectMapper;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    public HitClient(@Value("${STATS_SERVER_URL}") String serverUrl, RestTemplateBuilder builder,
                     ObjectMapper objectMapper) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
        this.objectMapper = objectMapper;
    }

    public ResponseEntity<Object> addHit(HitInputDto hitInputDto) {
        return post("/hit", hitInputDto);
    }

    public List<HitOutputDto> getHitStats(LocalDateTime start, LocalDateTime end, List<String> uris,
                                          boolean unique) {
        StringBuilder builder = new StringBuilder();
        for (String uri : uris) {
            builder.append(uri);
            builder.append(",");
        }
        Map<String, Object> parameters = Map.of(
                "start", start.format(formatter),
                "end", end.format(formatter),
                "uris", builder.toString(),
                "unique", unique
        );
        ResponseEntity<Object> response = get("/stats?start={start}&end={end}&uris={uris}&unique={unique}",
                parameters);
        return objectMapper.convertValue(response.getBody(), new TypeReference<List<HitOutputDto>>() {
        });
    }
}