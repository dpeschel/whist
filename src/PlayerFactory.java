import ch.aplu.jcardgame.*;

/**
 * player factory class. Create players according to
 * its configuration, hands, and the game they belongs to
 */
class PlayerFactory {
    private static PlayerFactory factory;

    /**
     * make this class a singleton.
     * @return return the only instance of the class
     */
    static PlayerFactory getInstance() {
        if (factory == null) {
            factory = new PlayerFactory();
        }
        return factory;
    }

    /**
     * player creation method. create play according to its configuration
     * @param whist to which game the player belongs
     * @param playerConfiguration include the player type and its position in the game
     * @param hands cards in players hand
     * @return return all of the 4 players in a array
     */
    Player[] createPlayers(Whist whist, String[] playerConfiguration, Hand[] hands) {
        Player[] players = new Player[Whist.nbPlayers];

        for (int i = 0; i < Whist.nbPlayers; i++) {
            String[] onePlayer = playerConfiguration[i].trim().split(",");
            onePlayer[0] = onePlayer[0].trim();
            onePlayer[1] = onePlayer[1].trim();
            initializePlayer(whist, onePlayer, hands, players);
        }
        return players;
    }

    private void initializePlayer(Whist whist, String[] onePlayer, Hand[] hands, Player[] players) {
        int position = Integer.parseInt(onePlayer[1]);
        switch (onePlayer[0]) {
            /* create human player */
            case "human":
                players[position] = new Player(hands[position], new HumanStrategy(hands[position]), whist, position);
                break;
            /* create smart player */
            case "smart":
                players[position] = new Player(hands[position], new SmartStrategy(hands[position], whist.getDeck()), whist, position);
                break;
            /* create legal player */
            case "legal":
                players[position] = new Player(hands[position], new LegalStrategy(), whist, position);
                break;
        }
    }
}
