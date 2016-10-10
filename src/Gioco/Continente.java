package Gioco;

import java.util.ArrayList;

import static Gioco.Territorio.*;

/**
 * Created by fiore on 10/10/2016.
 */
public enum Continente {
    NordAmerica(Alaska, Alberta, AmericaCentrale, StatiUnitiOccidentali, Groenlandia, TerritoriDelNordOvest, Ontario, Quebec, StatiUnitiOrientali),
    SudAmerica(Argentina, Brasile, Peru, Venezuela),
    Europa(GranBretagna, Islanda, EuropaDelNord, Scandinavia, EuropaDelSud, Ukraina, EuropaOccidentale),
    Africa(Congo, AfricaOrientale, Egitto, Madagascar, NordAfrica, SudAfrica),
    Asia(Afghanistan, Cina, India, Irkutsk, Giappone, Kamchatka, MedioOriente, Mongolia, Siam, Siberia, Urali, Yakutsk),
    Australia(AustraliaOrientale, Indonesia, NuovaGuinea, AustraliaOccidentale);

    private ArrayList<Territorio> territori = new ArrayList<>();

    Continente(Territorio... Territori) {
        for (Territorio t: Territori
             ) {
            this.territori.add(t);
        }
    }

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
