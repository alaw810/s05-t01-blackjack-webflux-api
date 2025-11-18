package cat.itacademy.s05.t01.blackjack.integration;

import cat.itacademy.s05.t01.blackjack.repository.mongo.GameReactiveRepository;
import cat.itacademy.s05.t01.blackjack.repository.mysql.PlayerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.test.StepVerifier;

@SpringBootTest
class DatabaseBootTest {

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private GameReactiveRepository gameRepository;

    @Test
    void contextLoads() {
        assert playerRepository != null;
        assert gameRepository != null;
    }

    @Test
    void testMySQLConnection() {
        StepVerifier.create(playerRepository.count())
                .expectNextMatches(count -> count >= 0)
                .verifyComplete();
    }

    @Test
    void testMongoConnection() {
        StepVerifier.create(gameRepository.count())
                .expectNextCount(1)
                .verifyComplete();
    }
}
