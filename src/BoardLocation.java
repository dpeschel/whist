import ch.aplu.jgamegrid.Location;
/**
 * Helper class that defines the coordinates of the game board
 */
public class BoardLocation {
    public final Location[] handLocations = {
            new Location(350, 625),
            new Location(75, 350),
            new Location(350, 75),
            new Location(625, 350)
    };
    public final Location[] scoreLocations = {
            new Location(575, 675),
            new Location(25, 575),
            new Location(575, 25),
            new Location(650, 575)
    };
    public final Location trickLocation = new Location(350, 350);
    public final Location textLocation = new Location(350, 450);
    public Location hideLocation = new Location(-500, -500);
    public Location trumpsActorLocation = new Location(50, 50);
    public final int handWidth = 400;
    public final int trickWidth = 40;
}
