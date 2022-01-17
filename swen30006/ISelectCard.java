import ch.aplu.jcardgame.*;

/**
 * the interface defines what actions must be performed
 */
public interface ISelectCard {
    Card selectCard(int position, Hand hand, Whist whist);
    void responseToCardPlayed(Card cardPlayed);
}
