package support;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public abstract class HttpUtils {

    private static final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(3))
            .build();

    public static HttpResponse<String> send(final String path) { // 들어온 Path 를 기반으로 Local 에 요청을 보낸다.
        final var request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080" + path))
                .timeout(Duration.ofSeconds(3))
                .build();

        try {
            return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
