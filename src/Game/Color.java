package Game;

/**
 * Armies color list
 */
public enum Color {
    ROSSO,
    GIALLO,
    BLU,
    VERDE,
    NERO,
    ROSA;

    private static int index = 0;

    /**
     * Get a color to assign to a player
     *
     * @return Color from sequence
     * @throws IndexOutOfBoundsException If more than six colors are requested
     */
    public static Color next() throws IndexOutOfBoundsException {
        if(index >= 6)
            throw new IndexOutOfBoundsException("Only six colors here.");

        return Color.values()[index++];
    }

    public static void reset() {
        index = 0;
    }
}
