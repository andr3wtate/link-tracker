package backend.academy.linktracker.scrapper.configuration;

import backend.academy.linktracker.scrapper.client.GitHubClient;
import backend.academy.linktracker.scrapper.properties.GithubProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
public class GitHubClientConfiguration {
    @Bean
    public RestClient getGitHubRestClient(@Validated GithubProperties properties) {
        return RestClient.builder()
            .baseUrl("https://api.github.com")
            .defaultHeader("Accept", "application/vnd.github.v3+json")
            .defaultHeader("Authorization", "Bearer " + properties.getToken())
            .build();
    }

    @Bean
    public GitHubClient gitHubClient(@Qualifier("getGitHubRestClient") RestClient restClient) {
        HttpServiceProxyFactory factory = HttpServiceProxyFactory
            .builderFor(RestClientAdapter.create(restClient))
            .build();
        return factory.createClient(GitHubClient.class);
    }
}
