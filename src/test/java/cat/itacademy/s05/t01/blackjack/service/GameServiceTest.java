package cat.itacademy.s05.t01.blackjack.service;

import cat.itacademy.s05.t01.blackjack.model.mongo.Game;
import cat.itacademy.s05.t01.blackjack.model.mysql.Player;
import cat.itacademy.s05.t01.blackjack.repository.mongo.GameReactiveRepository;
import cat.itacademy.s05.t01.blackjack.repository.mysql.PlayerRepository;
import cat.itacademy.s05.t01.blackjack.util.DeckFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class GameServiceTest {

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private GameReactiveRepository gameRepository;

    @InjectMocks
    private GameService gameService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldCreateNewPlayerIfNotExists() {
        String playerName = "John";

        when(playerRepository.findByName(playerName)).thenReturn(Mono.empty());
        when(playerRepository.save(any(Player.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        when(gameRepository.save(any(Game.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(gameService.createNewGame(playerName))
                .assertNext(response -> {
                    assert response.playerName().equals("John");
                })
                .verifyComplete();

        verify(playerRepository).save(any(Player.class));
    }

    @Test
    void shouldNotCreateNewPlayerIfExists() {
        String playerName = "Jane";
        Player existingPlayer = new Player(1L, "Jane", 0, 0, 0);

        when(playerRepository.findByName(playerName)).thenReturn(Mono.just(existingPlayer));
        when(gameRepository.save(any(Game.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(gameService.createNewGame(playerName))
                .assertNext(response -> {
                    assert response.playerName().equals("Jane");
                })
                .verifyComplete();

        verify(playerRepository, never()).save(any());
    }

    @Test
    void shouldInitializeDeckAndHands() {
        String playerName = "John";
        Player player = new Player(5L, "John", 0, 0, 0);

        when(playerRepository.findByName(playerName)).thenReturn(Mono.just(player));
        when(gameRepository.save(any(Game.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(gameService.createNewGame(playerName))
                .assertNext(response -> {
                    assert response.playerHand().size() == 2;
                    assert response.dealerHand().size() == 2;
                    assert response.deck().size() == 52 - 4;
                })
                .verifyComplete();
    }
}
