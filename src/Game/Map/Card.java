package Game.Map;

import java.util.ArrayList;

/**
 * Created by fiore on 20/12/2016.
 */
public class Card {
    public final String Name;

    public final Figure Figure;

    private String image;

    public String getImage() { return image; }

    private String mapName;

    public Card(String Name, Figure Figure, String MapName){
        this.Name = Name;
        this.Figure = Figure;
        this.mapName = MapName;
    }

    public void loadGraphic() {
        this.image = Card.class.getResource(mapName + "/Cards/" + Name.replaceAll(" ", "") + ".jpg").toExternalForm();
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
