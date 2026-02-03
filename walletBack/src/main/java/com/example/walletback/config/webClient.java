@Configuration
public class WebClientConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        return builder.baseUrl(issuerUri).build();
    }
}
