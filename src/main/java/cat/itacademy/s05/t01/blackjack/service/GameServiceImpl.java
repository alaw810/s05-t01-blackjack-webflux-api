package cat.itacademy.s05.t01.blackjack.service;

import cat.itacademy.s05.t01.blackjack.dto.NewGameRequest;
import cat.itacademy.s05.t01.blackjack.dto.NewGameResponse;
import cat.itacademy.s05.t01.blackjack.model.mongo.Game;
import cat.itacademy.s05.t01.blackjack.model.mysql.Player;
import cat.itacademy.s05.t01.blackjack.repository.mongo.GameReactiveRepository;
import cat.itacademy.s05.t01.blackjack.repository.mysql.PlayerRepository;
import cat.itacademy.s05.t01.blackjack.util.BlackjackRules;
import cat.itacademy.s05.t01.blackjack.util.DeckFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;

@Service
public class GameServiceImpl implements GameService {

    private final PlayerRepository playerRepository;
    private final GameReactiveRepository gameRepository;

    public GameServiceImpl(PlayerRepository playerRepository,
                           GameReactiveRepository gameRepository) {
        this.playerRepository = playerRepository;
        this.gameRepository = gameRepository;
    }

    @Override
    public Mono<NewGameResponse> createNewGame(NewGameRequest request) {
        String playerName = request.playerName() != null
                ? request.playerName().trim()
                : "";

        if (playerName.isBlank()) {
            return Mono.error(new IllegalArgumentException("Player name must not be empty"));
        }

        return findOrCreatePlayer(playerName)
                .flatMap(player -> {
                    // 1) Crear mazo barajado
                    List<String> deck = DeckFactory.createShuffledDeck();

                    // 2) Repartir 2 cartas a jugador y dealer
                    List<String> playerHand = new ArrayList<>();
                    List<String> dealerHand = new ArrayList<>();
                    dealInitialCards(deck, playerHand, dealerHand);

                    // 3) Crear entidad Game
                    Game game = Game.builder()
                            .playerId(player.getId())
                            .playerHand(playerHand)
                            .dealerHand(dealerHand)
                            .deck(deck)
                            .status("IN_PROGRESS")
                            .build();

                    // 4) Guardar en Mongo
                    return gameRepository.save(game)
                            .map(savedGame -> toNewGameResponse(savedGame, player));
                });
    }

    private Mono<Player> findOrCreatePlayer(String playerName) {
        return playerRepository.findByName(playerName)
                .switchIfEmpty(
                        Mono.defer(() ->
                                playerRepository.save(
                                        Player.builder()
                                                .name(playerName)
                                                .gamesPlayed(0)
                                                .gamesWon(0)
                                                .gamesLost(0)
                                                .build()
                                )
                        )
                );
    }

    private void dealInitialCards(List<String> deck,
                                  List<String> playerHand,
                                  List<String> dealerHand) {
        // Orden típico: jugador, dealer, jugador, dealer
        playerHand.add(deck.remove(0));
        dealerHand.add(deck.remove(0));
        playerHand.add(deck.remove(0));
        dealerHand.add(deck.remove(0));
    }

    private NewGameResponse toNewGameResponse(Game game, Player player) {
        int playerValue = BlackjackRules.calculateHandValue(game.getPlayerHand());
        int dealerValue = BlackjackRules.calculateHandValue(game.getDealerHand());

        return NewGameResponse.builder()
                .gameId(game.getId())
                .playerName(player.getName())
                .playerHand(game.getPlayerHand())
                .dealerHand(game.getDealerHand())
                .playerHandValue(playerValue)
                .dealerHandValue(dealerValue)
                .remainingDeckSize(game.getDeck() != null ? game.getDeck().size() : 0)
                .status(game.getStatus())
                .build();
    }
}
