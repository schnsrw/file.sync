package in.lazygod.translate;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriUtils;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class LingvaTranslationService implements TranslationService {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public LingvaTranslationService(RestTemplateBuilder builder,
                                    @Value("${lingva.base-url:http://lingva:3000}") String baseUrl) {
        this.restTemplate = builder.build();
        this.baseUrl = baseUrl;
    }

    @Override
    public List<String> translateBatch(List<String> segments, String src, String tgt) {
        return segments.stream().map(s -> translate(s, src, tgt)).toList();
    }

    private String translate(String text, String src, String tgt) {
        String path = String.format("/api/v1/%s/%s/%s",
                (src == null || src.isBlank()) ? "auto" : src,
                tgt,
                UriUtils.encodePathSegment(text, StandardCharsets.UTF_8));
        TranslationResponse response = restTemplate.getForObject(baseUrl + path, TranslationResponse.class);
        return response != null ? response.translation : text;
    }

    public static class TranslationResponse {
        public String translation;
    }
}
