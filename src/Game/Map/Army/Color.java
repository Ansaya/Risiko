package Game.Map.Army;

import javafx.scene.image.Image;

/**
 * Armies color list
 */
public enum Color {
    RED("#cc0000"),
    YELLOW("#f2b700"),
    BLUE("#0000b3"),
    GREEN("#196b19"),
    BLACK("#2f2f2f"),
    PURPLE("#711b71");

    public transient final javafx.scene.paint.Color hexColor;

    public transient final Image armyImg;

    Color(String HexColor) {
        this.hexColor = javafx.scene.paint.Color.web(HexColor);
        this.armyImg = new Image(Color.class.getResourceAsStream(this.name().toLowerCase() + ".png"),
                40.0,
                40.0,
                true,
                true);
    }
}
