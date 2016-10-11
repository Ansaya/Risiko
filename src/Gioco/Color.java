package Gioco;

/**
 * Created by fiore on 10/10/2016.
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
     * Restituisce un colore tra quelli disponibili da assegnare a un giocatore
     *
     * @return Color dalla sequenza
     * @throws IndexOutOfBoundsException Se si richiedono più di 6 colori
     */
    public static Color next() throws IndexOutOfBoundsException {
        if(index >= 6)
            throw new IndexOutOfBoundsException("I colori sono solo 6.");

        return Color.values()[index++];
    }

    public static void reset() {
        index = 0;
    }
}
