import ch.aplu.jcardgame.*;

/**
 * Player class that represents players in the game , and its behaviours
 */
public class Player implements IObserver {
    private Whist whist;
    private int position;
    private Hand hand;
    private ISelectCard selection;

    /**
     * Player constructor.
     *
     * @param hand      hands on hand
     * @param selection card selection strategy adopted
     * @param whist     the game the player is playing
     * @param position  the position where the player sit
     */
    Player(Hand hand, ISelectCard selection, Whist whist, int position) {
        this.hand = hand;
        this.whist = whist;
        this.position = position;
        this.selection = selection;
    }

    /**
     * player use the strategy  available to play a card
     *
     * @return the card will be played
     */
    Card playerSelectCard() {
        return this.selection.selectCard(position, hand, whist);
    }

    /**
     * player receive notice of the game, and use the strategy to deal with it
     *
     * @param oneCard card been selected to play
     */
    @Override
    public void update(Card oneCard) {
        selection.responseToCardPlayed(oneCard);
    }

    Hand getHand() {
        return hand;
    }
}
