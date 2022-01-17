import ch.aplu.jcardgame.*;

import java.util.ArrayList;

/**
 * the legal strategy that can be user by player. play the card legally.
 */

public class LegalStrategy implements ISelectCard {
    /**
     * the override method that represents how this strategy deal with given information, in this case do nothing
     *
     * @param oneCard card has been selected to play
     */
    @Override
    public void responseToCardPlayed(Card oneCard) {
    }

    /**
     * randomly select a legal card to play
     *
     * @param position where the player seat
     * @param hand     the cards that player has on his hand
     * @param whist    the game the player is playing
     * @return the card has been selected to play in a trick
     */
    @Override
    public Card selectCard(int position, Hand hand, Whist whist) {
        ArrayList<Card> list;

        whist.setStatusText("Player " + position + " thinking...");
        Whist.delay(whist.thinkingTime);
        // when legal player follows
        if (whist.getLead() != null) {
            list = hand.getCardsWithSuit(whist.getLead());
            if (list.size() > 0) {
                return whist.random.randomCard(list);
            } else {
                return whist.random.randomCard(hand);
            }
            // when legal player leads
        } else {
            return whist.random.randomCard(hand);
        }
    }
}