import ch.aplu.jcardgame.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * smart strategy play the game in smart way. Increase the ability to win
 */
public class SmartStrategy implements ISelectCard {
    private HashMap<Enum, ArrayList<Card>> remainingCards = new HashMap<>();
    private ArrayList<Card> trickSoFar = new ArrayList<>();

    /**
     * smart strategy constructor.
     *
     * @param myHand cards on hand
     * @param deck   the deck of the game
     */
    SmartStrategy(Hand myHand, Deck deck) {
        allCards(myHand, deck);
    }

    /**
     * list all cards in a HashMap, then remove cards on hand,
     * the remaining represents cards on other players' hand
     *
     * @param myHand cards on my hand
     * @param deck   the deck of the game
     */
    private void allCards(Hand myHand, Deck deck) {
        // clear HashMap first
        remainingCards.clear();
        // list all cards in a HashMap
        remainingCards.put(Whist.Suit.SPADES, new ArrayList<>(Arrays.asList(deck.cards[0])));
        remainingCards.put(Whist.Suit.HEARTS, new ArrayList<>(Arrays.asList(deck.cards[1])));
        remainingCards.put(Whist.Suit.DIAMONDS, new ArrayList<>(Arrays.asList(deck.cards[2])));
        remainingCards.put(Whist.Suit.CLUBS, new ArrayList<>(Arrays.asList(deck.cards[3])));
        // remove cards on hand
        for (Card oneCard : myHand.getCardList()) {
            remainingCards.get(oneCard.getSuit()).remove(oneCard);
        }
    }

    /**
     * the override method that represents how this strategy deal with given information,
     * in this case remember the cards, and calculate what cards left
     *
     * @param oneCard card been selected to play
     */
    @Override
    public void responseToCardPlayed(Card oneCard) {
        if (trickSoFar.size() < 4) {
            trickSoFar.add(oneCard);
        }
        if (trickSoFar.size() == 4) {
            remainingCards.get(oneCard.getSuit()).removeAll(trickSoFar);
            trickSoFar.clear();
        }
    }

    /**
     * find  the smallest card out of all cards on hand
     * @param hand cards on hand
     * @return smallest card out of all cards on hand
     */
    private Card smallestCardOnHand(Hand hand){
        Card smallestCard = hand.getCardList().get(0);
        for(Card card : hand.getCardList()){
            if(Whist.rankGreater(smallestCard, card)) {
                smallestCard = card;
            }
        }
        return smallestCard;
    }

    /**
     * compare a single Card to all cards in trick
     *
     * @param card card to be compared
     * @return true if selected card is greater than all cards in trick, false otherwise
     */
    private boolean trickCardCompare(Card card, Enum trump) {
        Enum cardSuit = card.getSuit();
        for (Card trickCard : trickSoFar) {
            //if trick card is not trump
            if(trickCard.getSuit() != trump){
                if (!Whist.rankGreater(card, trickCard) && cardSuit != trump) {
                    return false;
                }
            }else{
                // trick card is trump and compare card is not, return false
                if(cardSuit != trump){
                    return false;
                    //both trick and compare card are trump card, compare their id
                }else{
                    if (!Whist.rankGreater(card, trickCard)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * find the smallest winning card of a certain suit, or null.
     *
     * @param remainingCards the collection of all of other players' card on their hand
     * @param oneSuitCards   one suit of cards of all cards on this hand
     * @return smallest winning card or null
     */
    private Card smallestWinningCard(HashMap<Enum, ArrayList<Card>> remainingCards, ArrayList<Card> oneSuitCards) {
        Card winningCard = null;
        //if the chosen Suit list is empty, return null
        if (oneSuitCards.size() == 0) {
            return null;
        }
        //get the suit value of teh chosen suit, a.k.a. the key of HashMap here
        Enum key = oneSuitCards.get(0).getSuit();
        //if no cards on other players hand, return the smallest card in chosen suit
        if (remainingCards.get(key).size() == 0) {
            return oneSuitCards.get(oneSuitCards.size() - 1);
        }
        // As both ArrayList is sorted, so only compare the first card
        // then find the smallest winning card through looping, or find null
        for (Card oneSuitCard : oneSuitCards) {
            if (Whist.rankGreater(oneSuitCard, remainingCards.get(key).get(0))) {
                winningCard = oneSuitCard;
            } else {
                break;
            }
        }
        return winningCard;
    }

    /**
     * play the 100% winning card or smallest card on hand, to maximum the chance to win
     * the algorithm is based on the playing order of the player in a trick
     *
     * @param position where the player seat
     * @param hand     the cards that player has on his hand
     * @param whist    the game the player is playing
     * @return the card has been selected to play in a trick
     */
    @Override
    public Card selectCard(int position, Hand hand, Whist whist) {
        Card winningCard = null;
        ArrayList<Card> leadCardsList = hand.getCardsWithSuit(whist.getLead());
        ArrayList<Card> trumpCardsList = hand.getCardsWithSuit(whist.getTrumps());

        whist.setStatusText("Player " + position + " thinking...");
        Whist.delay(whist.thinkingTime);
        //smart player leads the trick, play card based on player's order(position) in a trick
        if (trickSoFar.size() == 0) {
            //loop through all suit to find the first card that has almost 100% winning chance
            //however trump card played by other player is not considered
            for (Whist.Suit oneSuit : Whist.Suit.values()) {
                winningCard = smallestWinningCard(remainingCards, hand.getCardsWithSuit(oneSuit));
                if (winningCard != null) {
                    break;
                }
            }
            //play winning card if available, otherwise play the smallest card of random suit.
            if (winningCard != null) {
                return winningCard;
            } else {
                return smallestCardOnHand(hand);
            }
            //smart player follows the trick and is the last one to play a card
        } else if (trickSoFar.size() == 3) {
            //check if any cards in lead suit. If yes, compare them to current trick to find
            //the smallest winning card. start from the smallest cards of lead suit on hand
            if (leadCardsList.size() > 0) {
                for (int i = (leadCardsList.size() - 1); i >= 0; i--) {
                    if (trickCardCompare(leadCardsList.get(i), whist.getTrumps())) {
                        return leadCardsList.get(i);
                    }
                }
                // no cards on hand is greater than cards in trick, return the smallest cards of lead
                return leadCardsList.get(leadCardsList.size() - 1);
            } else {
                if (trumpCardsList.size() > 0) {
                    //return the smallest winning trump card
                    for (int i = (trumpCardsList.size() - 1); i >= 0; i--) {
                        if (trickCardCompare(trumpCardsList.get(i), whist.getTrumps())) {
                            return trumpCardsList.get(i);
                        }
                    }
                    return smallestCardOnHand(hand);
                }
                //if no lead and trump card, select smallest card of other suit
                return smallestCardOnHand(hand);
            }
            //player is in the middle of playing a card in a trick
        } else {
            //if has lead cards, return the smallest winning card if there is any
            if (leadCardsList.size() > 0) {
                Card card = smallestWinningCard(remainingCards, leadCardsList);
                if (card != null) {
                    return card;
                }
                //no winning card of lead suit. Return the smallest card of lead suit
                return leadCardsList.get(leadCardsList.size() - 1);
            } else {
                //if no lead card, play the smallest trump card
                if (trumpCardsList.size() > 0) {
                    //return the smallest trump card
                    return trumpCardsList.get(trumpCardsList.size() - 1);
                }
                // if no lead and trump card, select smallest card of other suit
                return smallestCardOnHand(hand);
            }
        }
    }
}