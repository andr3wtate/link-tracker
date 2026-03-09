package backend.academy.linktracker.scrapper.configuration;

import backend.academy.linktracker.commondto.ApiError;
import backend.academy.linktracker.commondto.ClientException;
import backend.academy.linktracker.scrapper.client.ScrapperClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import tools.jackson.databind.ObjectMapper;

@Configuration
public class ScrapperClientConfiguration {
    @Value("${app.client.base-url-for-bot}")
    String baseUrl;

    @Bean
    public RestClient getScrapperClient(ObjectMapper objectMapper) {
        return RestClient.builder()
            .baseUrl(baseUrl)
            .defaultStatusHandler(HttpStatusCode::is4xxClientError,
                (request, response) -> {
                    ApiError apiError = objectMapper.readValue(response.getBody(), ApiError.class);
                    throw new ClientException(apiError);
                })
            .build();
    }

    @Bean
    public ScrapperClient scrapperClient(@Qualifier("getScrapperClient") RestClient restClient) {
        HttpServiceProxyFactory factory = HttpServiceProxyFactory
            .builderFor(RestClientAdapter.create(restClient))
            .build();
        return factory.createClient(ScrapperClient.class);
    }
}
