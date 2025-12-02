package cat.itacademy.s05.t01.blackjack.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI blackjackApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Blackjack API")
                        .description("Reactive Blackjack Game API for IT Academy Bootcamp.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Adrià Lorente")
                                .email("adria810@gmail.com")));
    }
}
