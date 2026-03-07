package backend.academy.linktracker.bot.configuration;

import backend.academy.linktracker.bot.client.BotClient;
import backend.academy.linktracker.bot.client.BotClientException;
import backend.academy.linktracker.commondto.ApiError;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import tools.jackson.databind.ObjectMapper;

@Configuration
public class ClientConfiguration {
    @Bean
    public RestClient restClient(ObjectMapper objectMapper) {
        return RestClient.builder()
            .defaultStatusHandler(HttpStatusCode::is4xxClientError,
                (request, response) -> {
                    ApiError apiError = objectMapper.readValue(response.getBody(), ApiError.class);
                    throw new BotClientException(apiError);
                })
            .build();
    }

    @Bean
    public BotClient botClient(RestClient restClient) {
        HttpServiceProxyFactory factory = HttpServiceProxyFactory
            .builderFor(RestClientAdapter.create(restClient))
            .build();
        return factory.createClient(BotClient.class);
    }
}
