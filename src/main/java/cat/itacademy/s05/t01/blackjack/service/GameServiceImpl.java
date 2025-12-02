package cat.itacademy.s05.t01.blackjack.service;

import cat.itacademy.s05.t01.blackjack.dto.*;
import cat.itacademy.s05.t01.blackjack.exception.NotFoundException;
import cat.itacademy.s05.t01.blackjack.model.mongo.Game;
import cat.itacademy.s05.t01.blackjack.model.mongo.GameStatus;
import cat.itacademy.s05.t01.blackjack.model.mongo.Move;
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
                            .status(GameStatus.IN_PROGRESS)
                            .build();

                    return gameRepository.save(game)
                            .map(savedGame -> toNewGameResponse(savedGame, player));
                });
    }

    @Override
    public Mono<GameDetailsResponse> getGame(String gameId) {
        return gameRepository.findById(gameId)
                .switchIfEmpty(Mono.error(new NotFoundException("Game not found")))
                .map(game -> {
                    int playerValue = BlackjackRules.calculateHandValue(game.getPlayerHand());
                    int dealerValue = getVisibleDealerValue(game);

                    return GameDetailsResponse.builder()
                            .gameId(game.getId())
                            .playerId(game.getPlayerId())
                            .playerHand(game.getPlayerHand())
                            .dealerHand(getVisibleDealerHand(game))
                            .status(game.getStatus() != null ? game.getStatus().name() : null)
                            .playerHandValue(playerValue)
                            .dealerHandValue(dealerValue)
                            .build();
                });
    }

    @Override
    public Mono<PlayResultDTO> playMove(String gameId, PlayRequestDTO request) {
        Move move = Move.from(request.move());

        return gameRepository.findById(gameId)
                .switchIfEmpty(Mono.error(new NotFoundException("Game not found")))
                .flatMap(game -> {

                    if (game.getStatus() != GameStatus.IN_PROGRESS) {
                        return Mono.error(new IllegalStateException("Game is already finished"));
                    }

                    return switch (move) {
                        case HIT -> handleHit(game);
                        case STAND -> handleStand(game);
                    };
                });
    }

    @Override
    public Mono<Void> deleteGame(String gameId) {
        return gameRepository.findById(gameId)
                .switchIfEmpty(Mono.error(new NotFoundException("Game not found")))
                .flatMap(game -> gameRepository.deleteById(gameId));
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
        int dealerValue = getVisibleDealerValue(game);

        return NewGameResponse.builder()
                .gameId(game.getId())
                .playerName(player.getName())
                .playerHand(game.getPlayerHand())
                .dealerHand(getVisibleDealerHand(game))
                .playerHandValue(playerValue)
                .dealerHandValue(dealerValue)
                .status(game.getStatus() != null ? game.getStatus().name() : null)
                .build();
    }

    private Mono<PlayResultDTO> handleHit(Game game) {
        List<String> deck = game.getDeck();
        List<String> playerHand = game.getPlayerHand();

        String card = deck.remove(0);
        playerHand.add(card);

        int playerValue = BlackjackRules.calculateHandValue(playerHand);

        if (playerValue > 21) {
            game.setStatus(GameStatus.PLAYER_BUST);
            return endGame(game);
        }

        return gameRepository.save(game)
                .map(this::toPlayResult);
    }

    private Mono<PlayResultDTO> handleStand(Game game) {
        List<String> deck = game.getDeck();
        List<String> dealerHand = game.getDealerHand();

        while (BlackjackRules.calculateHandValue(dealerHand) < 17 && !deck.isEmpty()) {
            dealerHand.add(deck.remove(0));
        }

        int dealerValue = BlackjackRules.calculateHandValue(dealerHand);
        int playerValue = BlackjackRules.calculateHandValue(game.getPlayerHand());

        if (dealerValue > 21) {
            game.setStatus(GameStatus.PLAYER_WIN);
        } else if (dealerValue > playerValue) {
            game.setStatus(GameStatus.PLAYER_LOSE);
        } else if (dealerValue < playerValue) {
            game.setStatus(GameStatus.PLAYER_WIN);
        } else {
            game.setStatus(GameStatus.TIE);
        }

        return endGame(game);
    }

    private Mono<PlayResultDTO> endGame(Game game) {
        return playerRepository.findById(game.getPlayerId())
                .flatMap(player -> {
                    player.setGamesPlayed(player.getGamesPlayed() + 1);

                    switch (game.getStatus()) {
                        case PLAYER_WIN -> player.setGamesWon(player.getGamesWon() + 1);
                        case PLAYER_LOSE, PLAYER_BUST -> player.setGamesLost(player.getGamesLost() + 1);
                        case TIE, DEALER_BUST, IN_PROGRESS -> {}
                    }

                    return playerRepository.save(player);
                })
                .flatMap(p -> gameRepository.save(game))
                .map(this::toPlayResult);
    }

    private PlayResultDTO toPlayResult(Game game) {
        int playerValue = BlackjackRules.calculateHandValue(game.getPlayerHand());
        int dealerValue = getVisibleDealerValue(game);

        return PlayResultDTO.builder()
                .gameId(game.getId())
                .status(game.getStatus() != null ? game.getStatus().name() : null)
                .playerHand(game.getPlayerHand())
                .dealerHand(getVisibleDealerHand(game))
                .playerValue(playerValue)
                .dealerValue(dealerValue)
                .message(toHumanMessage(game.getStatus()))
                .build();
    }

    private String toHumanMessage(GameStatus status) {
        return switch (status) {
            case PLAYER_WIN -> "Player wins!";
            case PLAYER_LOSE -> "Player loses!";
            case PLAYER_BUST -> "Player busts!";
            case TIE -> "It's a tie!";
            case IN_PROGRESS -> "Game in progress";
            case DEALER_BUST -> "Dealer busts!";
        };
    }

    private List<String> getVisibleDealerHand(Game game) {
        List<String> dealerHand = game.getDealerHand();
        if (dealerHand == null || dealerHand.isEmpty()) {
            return List.of();
        }

        if (game.getStatus() == GameStatus.IN_PROGRESS && dealerHand.size() > 1) {
            return List.of(dealerHand.get(0));
        }

        return dealerHand;
    }

    private int getVisibleDealerValue(Game game) {
        List<String> visibleDealerHand = getVisibleDealerHand(game);
        return BlackjackRules.calculateHandValue(visibleDealerHand);
    }

}
