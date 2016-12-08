package Game;

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

    public final javafx.scene.paint.Color hexColor;

    Color(String HexColor) {
        this.hexColor = javafx.scene.paint.Color.web(HexColor);
    }
}
