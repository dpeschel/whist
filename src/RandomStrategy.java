import ch.aplu.jcardgame.*;

/**
 * The random strategy class. play card randomly
 */
public class RandomStrategy implements ISelectCard {
    /**
     * the override method that represents how this strategy
     * deal with given information, in this case do nothing
     *
     * @param oneCard card has been selected to play
     */
    @Override
    public void responseToCardPlayed(Card oneCard) {
    }

    /**
     * randomly select a card to play
     *
     * @param position where the player seat
     * @param hand     the cards that player has on his hand
     * @param whist    the game the player is playing
     * @return the card has been selected to play in a trick
     */
    @Override
    public Card selectCard(int position, Hand hand, Whist whist) {
        whist.setStatusText("Player " + position + " thinking...");
        Whist.delay(whist.thinkingTime);
        return whist.random.randomCard(hand);
    }
}

