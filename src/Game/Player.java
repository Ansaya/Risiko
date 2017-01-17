package Game;

import Game.Map.Army.Color;

/**
 * Player generic
 */
public interface Player {
    int getId();

    String getUsername();

    Color getColor();
}
