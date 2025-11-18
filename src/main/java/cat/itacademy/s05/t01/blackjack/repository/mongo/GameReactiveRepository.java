package cat.itacademy.s05.t01.blackjack.repository.mongo;

import cat.itacademy.s05.t01.blackjack.model.mongo.Game;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface GameReactiveRepository extends ReactiveMongoRepository<Game, String> {
}
