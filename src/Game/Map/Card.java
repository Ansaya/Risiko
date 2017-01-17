package Game.Map;

import javafx.scene.image.Image;
import java.util.ArrayList;

/**
 * Card object
 */
public class Card {
    public final String Name;

    public final Figure Figure;

    private transient Image image;

    /**
     * Get image relative to this card
     * @return
     */
    public Image getImage() {
        if(image == null)
            loadGraphic();

        return image;
    }

    private Maps mapName;

    private Card(String Name, Figure Figure, Maps MapName){
        this.Name = Name;
        this.Figure = Figure;
        this.image = null;
        this.mapName = MapName;
    }

    /**
     * Load card image from resources
     */
    public void loadGraphic() {
        try {
            this.image = new Image(Card.class.getResource(mapName.name() + "\\Cards\\" + Name.replaceAll(" ", "") + ".jpg").openStream());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Check if figure combination is valid
     *
     * @param Cards Three cards list
     * @return True if combination is valid, false if not
     */
    public static boolean isCombinationValid(ArrayList<Card> Cards) {
        if(Cards.size() != 3)
            return false;

        int infantry = 0, cavalry = 0, artillery = 0, jolly = 0;

        // Increment respective counter for each figure
        for (Card t: Cards) {
            switch (t.Figure){
                case Infantry:
                    infantry++;
                    break;
                case Cavalry:
                    cavalry++;
                    break;
                case Artillery:
                    artillery++;
                    break;
                case Jolly:
                    jolly++;
                    break;
                default:
                    break;
            }
        }

        // Check for valid combinations
        // Three same cards         Two same cards plus jolly       Three different cards
        return infantry == 3 || cavalry == 3 || artillery == 3 || (infantry == 1 && cavalry == 1 && artillery == 1) ||
                ((infantry == 2 || cavalry == 2 || artillery == 2) && jolly == 1);
    }

    @Override
    public String toString(){
        return Name;
    }
}
