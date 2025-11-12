package cat.itacademy.s05.t01.blackjack.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BlackjackRulesTest {

    @Test
    void handValue_ShouldCountFaceCardsAs10() {
        List<String> hand = List.of("KH", "QD");
        assertThat(BlackjackRules.calculateHandValue(hand)).isEqualTo(20);
    }

    @Test
    void handValue_ShouldCountAceAs11WhenPossible() {
        List<String> hand = List.of("AH", "9D");
        assertThat(BlackjackRules.calculateHandValue(hand)).isEqualTo(20);
    }

    @Test
    void handValue_ShouldCountAceAs1When11WouldBust() {
        List<String> hand = List.of("AH", "9D", "8C");
        assertThat(BlackjackRules.calculateHandValue(hand)).isEqualTo(18);
    }

    @Test
    void isBlackjack_ShouldReturnTrueForAceAndTenValue() {
        List<String> hand = List.of("AH", "KD");
        assertThat(BlackjackRules.isBlackjack(hand)).isTrue();
    }
}

