import ch.aplu.jcardgame.Card;

/**
 * the interface define who are the observers, and their behaviour
 */
public interface IObserver {
    void update(Card selected);
}
