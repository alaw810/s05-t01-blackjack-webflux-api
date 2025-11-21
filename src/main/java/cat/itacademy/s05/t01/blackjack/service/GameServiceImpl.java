package cat.itacademy.s05.t01.blackjack.service;

import cat.itacademy.s05.t01.blackjack.dto.*;
import cat.itacademy.s05.t01.blackjack.model.mongo.Game;
import cat.itacademy.s05.t01.blackjack.model.mysql.Player;
import cat.itacademy.s05.t01.blackjack.repository.mongo.GameReactiveRepository;
import cat.itacademy.s05.t01.blackjack.repository.mysql.PlayerRepository;
import cat.itacademy.s05.t01.blackjack.util.BlackjackRules;
import cat.itacademy.s05.t01.blackjack.util.DeckFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
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
                    List<String> deck = DeckFactory.createShuffledDeck();

                    List<String> playerHand = new ArrayList<>();
                    List<String> dealerHand = new ArrayList<>();
                    dealInitialCards(deck, playerHand, dealerHand);

                    Game game = Game.builder()
                            .playerId(player.getId())
                            .playerHand(playerHand)
                            .dealerHand(dealerHand)
                            .deck(deck)
                            .status("IN_PROGRESS")
                            .build();

                    return gameRepository.save(game)
                            .map(savedGame -> toNewGameResponse(savedGame, player));
                });
    }

    @Override
    public Mono<GameDetailsResponse> getGame(String gameId) {
        return gameRepository.findById(gameId)
                .switchIfEmpty(Mono.error(
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Game not found"
                        )
                ))
                .map(game -> {
                    int playerValue = BlackjackRules.calculateHandValue(game.getPlayerHand());
                    int dealerValue = BlackjackRules.calculateHandValue(game.getDealerHand());
                    int deckSize = game.getDeck() != null ? game.getDeck().size() : 0;

                    return GameDetailsResponse.builder()
                            .gameId(game.getId())
                            .playerId(game.getPlayerId())
                            .playerHand(game.getPlayerHand())
                            .dealerHand(game.getDealerHand())
                            .status(game.getStatus())
                            .playerHandValue(playerValue)
                            .dealerHandValue(dealerValue)
                            .remainingDeckSize(deckSize)
                            .build();
                });
    }

    @Override
    public Mono<PlayResultDTO> playMove(String gameId, PlayRequestDTO request) {
        String move = request.move() != null ? request.move().trim().toUpperCase() : "";

        if (!List.of("HIT", "STAND", "DOUBLE").contains(move)) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid move"));
        }

        return gameRepository.findById(gameId)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found")))
                .flatMap(game -> {

                    if (!"IN_PROGRESS".equals(game.getStatus())) {
                        return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Game is already finished"));
                    }

                    return switch (move) {
                        case "HIT" -> handleHit(gameId, game);
                        case "STAND" -> handleStand(gameId, game);
                        case "DOUBLE" -> handleDouble(gameId, game);
                        default -> Mono.error(new IllegalStateException("Unknown move"));
                    };
                });
    }

    @Override
    public Mono<Void> deleteGame(String gameId) {
        return Mono.empty();
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

    private Mono<PlayResultDTO> handleHit(String gameId, Game game) {
        List<String> deck = game.getDeck();
        List<String> playerHand = game.getPlayerHand();

        String card = deck.remove(0);
        playerHand.add(card);

        int playerValue = BlackjackRules.calculateHandValue(playerHand);

        if (playerValue > 21) {
            game.setStatus("PLAYER_BUST");
            return endGameAndUpdateStats(game)
                    .map(savedGame -> toPlayResult(savedGame));
        }

        return gameRepository.save(game)
                .map(this::toPlayResult);
    }

    private Mono<PlayResultDTO> handleStand(String gameId, Game game) {
        List<String> deck = game.getDeck();
        List<String> dealerHand = game.getDealerHand();

        while (BlackjackRules.calculateHandValue(dealerHand) < 17 && !deck.isEmpty()) {
            dealerHand.add(deck.remove(0));
        }

        int dealerValue = BlackjackRules.calculateHandValue(dealerHand);
        int playerValue = BlackjackRules.calculateHandValue(game.getPlayerHand());

        if (dealerValue > 21) {
            game.setStatus("DEALER_BUST");
        } else if (dealerValue > playerValue) {
            game.setStatus("PLAYER_LOSE");
        } else if (dealerValue < playerValue) {
            game.setStatus("PLAYER_WIN");
        } else {
            game.setStatus("TIE");
        }

        return endGameAndUpdateStats(game)
                .map(this::toPlayResult);
    }

    private Mono<PlayResultDTO> handleDouble(String gameId, Game game) {
        List<String> deck = game.getDeck();
        List<String> playerHand = game.getPlayerHand();

        String card = deck.remove(0);
        playerHand.add(card);

        int playerValue = BlackjackRules.calculateHandValue(playerHand);

        if (playerValue > 21) {
            game.setStatus("PLAYER_BUST");
            return endGameAndUpdateStats(game)
                    .map(this::toPlayResult);
        }
        return handleStand(gameId, game);
    }

    private Mono<Game> endGameAndUpdateStats(Game game) {
        return playerRepository.findById(game.getPlayerId())
                .flatMap(player -> {

                    player.setGamesPlayed(player.getGamesPlayed() + 1);

                    switch (game.getStatus()) {
                        case "PLAYER_WIN" -> player.setGamesWon(player.getGamesWon() + 1);
                        case "PLAYER_LOSE", "PLAYER_BUST" -> player.setGamesLost(player.getGamesLost() + 1);
                    }

                    return playerRepository.save(player);
                })
                .flatMap(p -> gameRepository.save(game));
    }

    private PlayResultDTO toPlayResult(Game game) {
        int playerValue = BlackjackRules.calculateHandValue(game.getPlayerHand());
        int dealerValue = BlackjackRules.calculateHandValue(game.getDealerHand());

        return PlayResultDTO.builder()
                .gameId(game.getId())
                .status(game.getStatus())
                .playerHand(game.getPlayerHand())
                .dealerHand(game.getDealerHand())
                .playerValue(playerValue)
                .dealerValue(dealerValue)
                .remainingDeckSize(game.getDeck().size())
                .message(game.getStatus())
                .build();
    }
}
