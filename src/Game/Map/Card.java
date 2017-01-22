package Game.Map;

import javafx.scene.image.Image;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Card object
 */
public class Card {
    public final String Id;

    public transient String Name;

    public final Figure Figure;

    private transient Image image;

    /**
     * Get image relative to this card
     *
     * @param Locale Language to load
     * @return Card image
     */
    public Image getImage(Locale Locale) {
        if(image == null)
            loadGraphic(Locale);

        return image;
    }

    private Maps mapId;

    private Card(String Id, Figure Figure, Maps mapId){
        this.Id = Id;
        this.Name = "";
        this.Figure = Figure;
        this.image = null;
        this.mapId = mapId;
    }

    /**
     * Load card image from resources
     */
    public void loadGraphic(Locale Locale) {
        try {
            this.image = new Image(Card.class.getResource(mapId.name() + "\\Cards\\" + Id + ".jpg").openStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        Name = ResourceBundle.getBundle(mapId + ".Resources", Locale).getString(Id);
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
