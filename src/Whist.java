import ch.aplu.jcardgame.*;
import ch.aplu.jgamegrid.*;

import java.awt.Color;
import java.awt.Font;
import java.util.*;
import java.io.FileReader;
import java.io.IOException;

/**
 * the facade object that receive the configuration and command of user, then run the game
 */

@SuppressWarnings("serial")
public class Whist extends CardGame {
    /**
     * The suit of card
     */
    public enum Suit {
        SPADES, HEARTS, DIAMONDS, CLUBS
    }

    /**
     * The rank of card
     * Reverse order of rank importance (see rankGreater() below), Order of cards is tied to card images
     */
    public enum Rank {
        ACE, KING, QUEEN, JACK, TEN, NINE, EIGHT, SEVEN, SIX, FIVE, FOUR, THREE, TWO
    }

    /**
     * Compare two cards
     *
     * @param card1 card one
     * @param card2 card two
     * @return True if card 1 > card 2, other wise false
     * Warning: Reverse rank order of cards. e.g. ACE is "0", KING is "1" ... 2 is "12"
     */
    public static boolean rankGreater(Card card1, Card card2) {
        return card1.getRankId() < card2.getRankId();
    }

    Suit getTrumps() {
        return trumps;
    }

    Deck getDeck() {
        return deck;
    }

    Suit getLead() {
        return lead;
    }

    /**
     * Update method that notify listeners the change in the game
     *
     * @param cardPlayed the card has been played
     */
    private void update(Card cardPlayed) {
        for (IObserver listener : listeners) {
            listener.update(cardPlayed);
        }
    }

    /**
     * add listener to the list
     *
     * @param player the object that want to observing the game
     */
    private void addListener(IObserver player) {
        listeners.add(player);
    }

    /**
     * remove listener from the list
     *
     * @param player the listener on the list
     */
    private void removeListener(IObserver player) {
        listeners.remove(player);
    }

    final int thinkingTime = 2000;
    static final int nbPlayers = 4;
    private final String version = "1.0";
    private int seed;
    private final String[] playerConfiguration;
    private final int nbStartCards;
    private final int winningScore;
    private boolean enforceRules;
    private Actor trumpsActor;
    private Player[] players;
    private Suit trumps;
    private Suit lead;
    private ArrayList<IObserver> listeners = new ArrayList<>();
    private BoardLocation boardLocation = new BoardLocation();
    private final Deck deck = new Deck(Suit.values(), Rank.values(), "cover");
    private final String[] trumpImage = {"bigspade.gif", "bigheart.gif", "bigdiamond.gif", "bigclub.gif"};
    private Actor[] scoreActors = {null, null, null, null};
    private int[] scores = new int[nbPlayers];
    Font bigFont = new Font("Serif", Font.BOLD, 36);
    public final RandomSelection random = new RandomSelection(seed);


    /**
     * initialise players' score, and display them in the game board
     */
    private void initScore() {
        for (int i = 0; i < nbPlayers; i++) {
            scores[i] = 0;
            scoreActors[i] = new TextActor("0", Color.WHITE, bgColor, bigFont);
            addActor(scoreActors[i], boardLocation.scoreLocations[i]);
        }
    }

    /**
     * update player's score
     *
     * @param player the winning player of a trick
     */
    private void updateScore(int player) {
        removeActor(scoreActors[player]);
        scoreActors[player] = new TextActor(String.valueOf(scores[player]), Color.WHITE, bgColor, bigFont);
        addActor(scoreActors[player], boardLocation.scoreLocations[player]);
    }

    /**
     * initialise the game, randomly select the trump, initialise hands and players,
     * then display all of them in the game board
     */
    private void initRound() {
        // Select and display trump suit
        trumps = random.randomEnum(Suit.class);
        trumpsActor = new Actor("sprites/" + trumpImage[trumps.ordinal()]);
        addActor(trumpsActor, boardLocation.trumpsActorLocation);
        // shuffle cards. Last element of hands is leftover cards; these are ignored
        Hand[] hands = deck.dealingOut(nbPlayers, nbStartCards);
        for (int i = 0; i < nbPlayers; i++) {
            hands[i].sort(Hand.SortType.SUITPRIORITY, true);
        }
        // create players and assign specific strategy to each player through factory
        players = PlayerFactory.getInstance().createPlayers(this, playerConfiguration, hands);
        // every player observing whist game, but players respond differently
        listeners.clear();
        for (Player onePlayer : players) {
            addListener(onePlayer);
        }
        // graphics
        RowLayout[] layouts = new RowLayout[nbPlayers];
        for (int i = 0; i < nbPlayers; i++) {
            layouts[i] = new RowLayout(boardLocation.handLocations[i], boardLocation.handWidth);
            layouts[i].setRotationAngle(90 * i);
            players[i].getHand().setView(this, layouts[i]);
            players[i].getHand().setTargetArea(new TargetArea(boardLocation.trickLocation));
            players[i].getHand().draw();
        }
        // visually hide the cards in a hand (make them face down)
       for (int i = 1; i < nbPlayers; i++)
            hands[i].setVerso(true);
        // End graphics
    }

    /**
     * Play the game till there is a winner.
     *
     * @return return the winner of the game
     */
    private Optional<Integer> playRound() {
        Hand trick;
        int winner;
        Card winningCard;
        // randomly select player to lead for this round
        int nextPlayer = random.getRandom().nextInt(nbPlayers);
        //looping through all cards
        for (int i = 0; i < nbStartCards; i++) {
            trick = new Hand(deck);
            Card selected;
            lead = null;
            // player select a card to play, then the game update the changes
            selected = players[nextPlayer].playerSelectCard();
            update(selected);
            // Lead with selected card
            trick.setView(this, new RowLayout(boardLocation.trickLocation,
                    (trick.getNumberOfCards() + 2) * boardLocation.trickWidth));
            trick.draw();
            selected.setVerso(false);
            // No restrictions on the card being lead
            lead = (Suit) selected.getSuit();
            // transfer to trick (includes graphic effect)
            selected.transfer(trick, true);
            winner = nextPlayer;
            winningCard = selected;

            //rest players follows the lead through looping
            for (int j = 1; j < nbPlayers; j++) {
                // From last back to first
                if (++nextPlayer >= nbPlayers) nextPlayer = 0;
                // player select a card to play, then the game notify the changes to listeners
                selected = players[nextPlayer].playerSelectCard();
                update(selected);
                // Follow with selected card
                trick.setView(this, new RowLayout(boardLocation.trickLocation,
                        (trick.getNumberOfCards() + 2) * boardLocation.trickWidth));
                trick.draw();
                // In case it is upside down
                selected.setVerso(false);
                // Check: Following card must follow suit if possible
                if (selected.getSuit() != lead && players[nextPlayer].getHand().getNumberOfCardsWithSuit(lead) > 0) {
                    // Rule violation
                    String violation = "Follow rule broken by player " + nextPlayer + " attempting to play " + selected;
                    System.out.println(violation);
                    if (enforceRules)
                        try {
                            throw (new BrokeRuleException(violation));
                        } catch (BrokeRuleException e) {
                            e.printStackTrace();
                            System.out.println("A cheating player spoiled the game!");
                            System.exit(0);
                        }
                }
                // End Check
                selected.transfer(trick, true); // transfer to trick (includes graphic effect)
                System.out.println("winning: suit = " + winningCard.getSuit() + ", rank = " + winningCard.getRankId());
                System.out.println(" played: suit = " + selected.getSuit() + ", rank = " + selected.getRankId());
                if ( // beat current winner with higher card
                        (selected.getSuit() == winningCard.getSuit() && rankGreater(selected, winningCard)) ||
                                // trumped when non-trump was winning
                                (selected.getSuit() == trumps && winningCard.getSuit() != trumps)) {
                    System.out.println("NEW WINNER");
                    winner = nextPlayer;
                    winningCard = selected;
                }
                // End Follow
            }
            delay(600);
            trick.setView(this, new RowLayout(boardLocation.hideLocation, 0));
            trick.draw();
            nextPlayer = winner;
            setStatusText("Player " + nextPlayer + " wins trick.");
            scores[nextPlayer]++;
            updateScore(nextPlayer);
            if (winningScore == scores[nextPlayer]) return Optional.of(nextPlayer);
        }
        removeActor(trumpsActor);
        return Optional.empty();
    }

    /**
     * Constructor of the game, but also the starter that run the game
     *
     * @param seed                the seed of random object
     * @param playerConfiguration the type and seat location of the players
     * @param nbStartCards        number of cards on hand to play each round
     * @param winningScore        number of scores to win the game
     * @param enforceRules        if breaking the rules is acceptable
     */
    Whist(int seed, String[] playerConfiguration, int nbStartCards, int winningScore, boolean enforceRules) {
        //initialisation
        super(700, 700, 30);
        this.seed = seed;
        this.nbStartCards = nbStartCards;
        this.winningScore = winningScore;
        this.enforceRules = enforceRules;
        this.playerConfiguration = playerConfiguration;
        //run whist gun
        setTitle("Whist (V" + version + ") Constructed for UofM SWEN30006 with JGameGrid (www.aplu.ch)");
        setStatusText("Initializing...");
        initScore();
        Optional<Integer> winner;
        do {
            initRound();
            winner = playRound();
        } while (!winner.isPresent());
        addActor(new Actor("sprites/gameover.gif"), boardLocation.textLocation);
        setStatusText("Game over. Winner is player: " + winner.get());
        refresh();
    }

    /**
     * Main method. input game configurations and starts the game
     */
    public static void main(String[] args) throws IOException {
        //System.out.println("Working Directory = " + System.getProperty("user.dir"));
        /* read the property file, and pass to the game constructor */
        Properties WhistProperties = new Properties();
        FileReader inStream = null;
        try {
            inStream = new FileReader("whist.properties");
            WhistProperties.load(inStream);
        } finally {
            if (inStream != null) {
                inStream.close();
            }
        }
        int seed = Integer.parseInt(WhistProperties.getProperty("seed"));
        int nbStartCards = Integer.parseInt(WhistProperties.getProperty("nbStartCards"));
        int winningScore = Integer.parseInt(WhistProperties.getProperty("winningScore"));
        boolean legalPlay = Boolean.parseBoolean(WhistProperties.getProperty("legalPlay"));
        String[] playerConfiguration = WhistProperties.getProperty("players").trim().split(";");

        new Whist(seed, playerConfiguration, nbStartCards, winningScore, legalPlay);
    }

}
