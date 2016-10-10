package Gioco;

import java.util.ArrayList;

import static Gioco.Territorio.*;

/**
 * Lista dei contineti della mappa mondiale
 */
public enum Continente {
    NordAmerica(Alaska, Alberta, AmericaCentrale, StatiUnitiOccidentali, Groenlandia, TerritoriDelNordOvest, Ontario, Quebec, StatiUnitiOrientali),
    SudAmerica(Argentina, Brasile, Peru, Venezuela),
    Europa(GranBretagna, Islanda, EuropaSettentrionale, Scandinavia, EuropaMeridionale, Ukraina, EuropaOccidentale),
    Africa(Congo, AfricaOrientale, Egitto, Madagascar, NordAfrica, SudAfrica),
    Asia(Afghanistan, Cina, India, Cita, Giappone, Kamchatka, MedioOriente, Mongolia, Siam, Siberia, Urali, Jacuzia),
    Australia(AustraliaOrientale, Indonesia, NuovaGuinea, AustraliaOccidentale);

    /**
     * Territori appartenenti al continente
     */
    private ArrayList<Territorio> territori = new ArrayList<>();

    Continente(Territorio... Territori) {
        for (Territorio t: Territori
             ) {
            this.territori.add(t);
        }
    }

    /**
     * Controlla se i territori passati comprendono interi continenti per assegnare armate bonus al giocatore
     *
     * @param Territori Territori del giocatore
     * @return int Numero di armate bonus disponibili
     */
    public static int armateBonus(ArrayList<Territorio> Territori) {

        // Lista dei continenti completi presenti nell'array territori
        ArrayList<Continente> controllati = continentiControllati(Territori);

        int bonus = 0;

        // Se sono presenti tutti i territori di un continente assegno le armate bonus
        for (Continente c: controllati
             ) {
            switch (c){
                case NordAmerica:
                    bonus += 5;
                    break;
                case SudAmerica:
                    bonus += 2;
                    break;
                case Europa:
                    bonus += 5;
                    break;
                case Africa:
                    bonus += 3;
                    break;
                case Asia:
                    bonus += 7;
                    break;
                case Australia:
                    bonus += 2;
                    break;
                default:
                    break;
            }
        }



        return bonus;
    }

    /**
     * Controlla se nei territori passati sono presenti interi continenti
     *
     * @param Territori Territori del giocatore
     * @return Lista dei continenti completi presenti nella lista
     */
    public static ArrayList<Continente> continentiControllati(ArrayList<Territorio> Territori) {
        int nordAmerica = 0, sudAmerica = 0, europa = 0, africa = 0, asia = 0, australia = 0;

        // Incremento il contatore corrispondente alla nazione di ogni territorio
        for (Territorio t:Territori
                ) {
            switch (t.continente){
                case NordAmerica:
                    nordAmerica++;
                    break;
                case SudAmerica:
                    sudAmerica++;
                    break;
                case Europa:
                    europa++;
                    break;
                case Africa:
                    africa++;
                    break;
                case Asia:
                    asia++;
                    break;
                case Australia:
                    australia++;
                    break;
                default:
                    break;
            }
        }

        ArrayList<Continente> controllati = new ArrayList<>();

        // Se sono presenti tutti i territori di un continente assegno le armate bonus
        if(nordAmerica == 9)
            controllati.add(NordAmerica);
        if (sudAmerica == 4)
            controllati.add(SudAmerica);
        if (europa == 7)
            controllati.add(Europa);
        if (africa == 6)
            controllati.add(Africa);
        if (asia == 12)
            controllati.add(Asia);
        if (australia == 4)
            controllati.add(Australia);

        return controllati;
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
