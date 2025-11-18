package cat.itacademy.s05.t01.blackjack.repository.mysql;

import cat.itacademy.s05.t01.blackjack.model.mysql.Player;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface PlayerRepository extends ReactiveCrudRepository<Player, Long> {

    Mono<Player> findByName(String name);
}
