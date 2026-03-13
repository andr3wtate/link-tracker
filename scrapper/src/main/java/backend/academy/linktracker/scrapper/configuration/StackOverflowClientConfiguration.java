package backend.academy.linktracker.scrapper.configuration;

import backend.academy.linktracker.scrapper.client.GitHubClient;
import backend.academy.linktracker.scrapper.client.StackOverflowClient;
import backend.academy.linktracker.scrapper.properties.GithubProperties;
import backend.academy.linktracker.scrapper.properties.StackoverflowProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class StackOverflowClientConfiguration {
    @Bean
    public RestClient getStackOverflowRestClient(@Validated StackoverflowProperties properties) {
        return RestClient.builder()
            .baseUrl("https://api.stackexchange.com/2.3")
            .defaultHeader("Accept", "application/json")
            .build();
    }

    @Bean
    public StackOverflowClient stackOverflowClient(@Qualifier("getStackOverflowRestClient") RestClient restClient) {
        HttpServiceProxyFactory factory = HttpServiceProxyFactory
            .builderFor(RestClientAdapter.create(restClient))
            .build();
        return factory.createClient(StackOverflowClient.class);
    }
}
