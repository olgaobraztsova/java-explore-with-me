import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.lang.Nullable;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class BaseClient {
    protected final RestTemplate restTemplate;

    protected ResponseEntity<Object> get(String path, @Nullable Map<String, Object> params) {
        return makeAndSendRequest(HttpMethod.GET, path, params, null);
    }

    protected <T> ResponseEntity<Object> post(String path, @Nullable Map<String, Object> params, T body) {
        return makeAndSendRequest(HttpMethod.POST, path, params, body);
    }

    protected <T> ResponseEntity<Object> post(String path, T body) {
        return post(path, null, body);
    }

    private <T> ResponseEntity<Object> makeAndSendRequest(
            HttpMethod method, String path, @Nullable Map<String, Object> parameters, @Nullable T body) {

        HttpEntity<T> requestEntity = new HttpEntity<>(body, defaultHeaders());
        ResponseEntity<Object> statsServiceResponse;

        try {
            if (parameters != null) {
                statsServiceResponse = restTemplate.exchange(path, method, requestEntity, Object.class, parameters);
            } else {
                statsServiceResponse = restTemplate.exchange(path, method, requestEntity, Object.class);
            }
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsByteArray());
        }
        return prepareStatsResponse(statsServiceResponse);
    }

    private HttpHeaders defaultHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return headers;
    }

    private static ResponseEntity<Object> prepareStatsResponse(ResponseEntity<Object> response) {
        if (response.getStatusCode().is2xxSuccessful()) {
            return response;
        }
        ResponseEntity.BodyBuilder responseBuilder = ResponseEntity.status(response.getStatusCode());
        if (response.hasBody()) {
            return responseBuilder.body(response.getBody());
        }
        return responseBuilder.build();
    }
}
