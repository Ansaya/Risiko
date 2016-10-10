package Gioco;

/**
 * Created by fiore on 10/10/2016.
 */
public enum Continente {
    NordAmerica,
    SudAmerica,
    Europa,
    Africa,
    Asia,
    Australia;

    @Override
    public String toString() {
        String[] parti = this.name().split("(?=[A-Z])");
        String nome = parti[0];
        for (int i = parti.length; i > 0 ; i--) {
            nome += " " + parti;
        }

        return nome;
    }
}
