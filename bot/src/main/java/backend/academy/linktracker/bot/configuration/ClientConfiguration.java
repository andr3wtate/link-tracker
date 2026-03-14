package backend.academy.linktracker.bot.configuration;

import backend.academy.linktracker.bot.client.BotClient;
import backend.academy.linktracker.commondto.ApiError;
import backend.academy.linktracker.commondto.ClientException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import tools.jackson.databind.ObjectMapper;

@Configuration
public class ClientConfiguration {
    @Value("${app.client.base-url-for-scrapper}")
    private String baseUrl;

    @Bean
    public RestClient restClient(ObjectMapper objectMapper) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .defaultStatusHandler(HttpStatusCode::is4xxClientError, (request, response) -> {
                    ApiError apiError = objectMapper.readValue(response.getBody(), ApiError.class);
                    throw new ClientException(apiError);
                })
                .build();
    }

    @Bean
    public BotClient botClient(RestClient restClient) {
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(RestClientAdapter.create(restClient))
                .build();
        return factory.createClient(BotClient.class);
    }
}
