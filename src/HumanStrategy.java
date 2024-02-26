/* TO DO when to call setTouchEnabled(true) and when false? */
/* this code looks inefficient but it may actually be correct */

import ch.aplu.jcardgame.*;
import java.util.ArrayList;

/**
 * the human strategy class
 */
public class HumanStrategy implements ISelectCard {
    private Card selected;

    /**
     * The constructor of HumanStrategy class, add a listener to the hand
     *
     * @param hand cards on hand. will add a card listener to receive human command
     */
    HumanStrategy(Hand hand) {
        CardListener cardListener = new CardAdapter()  // Human Player plays card
        {
            public void leftDoubleClicked(Card card) {
                hand.setTouchEnabled(true);
                selected = card;
            }
        };
        hand.addCardListener(cardListener);
    }

    /**
     * the override method that represents how this strategy deal with given information, in this case do nothing
     *
     * @param oneCard card been selected to play
     */
    @Override
    public void responseToCardPlayed(Card oneCard) {
    }

    /**
     * player a card that has been chosen by human by double click the mouse
     *
     * @param position where the player seat
     * @param hand     the cards that player has on his hand
     * @param whist    the game the player is playing
     * @return the card has been selected to play in a trick
     */
    @Override
    public Card selectCard(int position, Hand hand, Whist whist) {
        selected = null;
        Boolean done = false;
        ArrayList<Card> list;

        hand.setTouchEnabled(true);

        whist.setStatusText("Player " + position + " double-click on card to lead.");
        while (done == false) {
            while (null == selected) Whist.delay(100);
            if (whist.getLead() == null) {
                // Leading; any card OK.
                done=true;
            } else {
                list = hand.getCardsWithSuit(whist.getLead());
                if (list.size() == 0 ||
                    selected.getSuit() == whist.getLead()) {
                    // No cards of suit, or following suit
                    done=true;
                } else {
                    // Revoking (Cards of suit and not following suit)
                    whist.setStatusText("You must follow suit. Player " + position + " double-click on card to lead.");
                }
            }
        }
        whist.setStatusText("");
        return selected;
    }
}
