package Gioco;

/**
 * Created by fiore on 10/10/2016.
 */
public enum Colore {
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
     * @param restart indica se è necessario resettare il contatore di assegnazione
     * @return Colore dalla sequenza
     * @throws IndexOutOfBoundsException Se si richiedono più di 6 colori
     */
    public Colore next(boolean restart) throws IndexOutOfBoundsException {
        if(restart)
            index = 0;

        if(index >= 6)
            throw new IndexOutOfBoundsException("I colori sono solo 6.");

        return Colore.values()[index++];
    }
}
