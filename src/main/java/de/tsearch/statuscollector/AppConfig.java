package de.tsearch.statuscollector;

import de.tsearch.tclient.Config;
import de.tsearch.tclient.StreamClient;
import de.tsearch.tclient.TClientInstance;
import de.tsearch.tclient.WebhookClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
    @Bean
    public TClientInstance tClientInstance(@Value("${twitch.clientid}") String clientId,
                                           @Value("${twitch.clientsecret}") String clientSecret) {
        return new TClientInstance(Config.ConfigBuilder.newInstance()
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .build());
    }

    @Bean
    public WebhookClient webhookClient(TClientInstance clientInstance) {
        return new WebhookClient(clientInstance);
    }

    @Bean
    public StreamClient streamClient(TClientInstance clientInstance) {
        return new StreamClient(clientInstance);
    }
}
