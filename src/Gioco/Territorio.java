package Gioco;

import java.util.ArrayList;

import static Gioco.Continente.*;

/**
 * Created by fiore on 10/10/2016.
 */
public enum Territorio {
    Alaska (NordAmerica),
    Alberta (NordAmerica),
    AmericaCentrale (NordAmerica),
    StatiUnitiOrientali (NordAmerica),
    Groenlandia (NordAmerica),
    TerritoriDelNordOvest (NordAmerica),
    Ontario (NordAmerica),
    Quebec (NordAmerica),
    StatiUnitiOccidentali (NordAmerica),
    Argentina(SudAmerica),
    Brasile(SudAmerica),
    Peru(SudAmerica),
    Venezuela(SudAmerica),
    GranBretagna(Europa),
    Islanda(Europa),
    EuropaDelNord(Europa),
    Scandinavia(Europa),
    EuropaDelSud(Europa),
    Ukraina(Europa),
    EuropaOccidentale(Europa),
    Congo(Africa),
    AfricaOrientale(Africa),
    Egitto(Africa),
    Madagascar(Africa),
    NordAfrica(Africa),
    SudAfrica(Africa),
    Afghanistan(Asia),
    Cina(Asia),
    India(Asia),
    Irkutsk(Asia),
    Giappone(Asia),
    Kamchatka(Asia),
    MedioOriente(Asia),
    Mongolia(Asia),
    Siam(Asia),
    Siberia(Asia),
    Urali(Asia),
    Yakutsk(Asia),
    AustraliaOrientale(Australia),
    Indonesia(Australia),
    NuovaGuinea(Australia),
    AustraliaOccidentale(Australia);

    static {
        // Nord America
        Alaska.Init(Alberta, TerritoriDelNordOvest, Kamchatka);
        Alberta.Init(Alaska, StatiUnitiOrientali, TerritoriDelNordOvest, Ontario, StatiUnitiOccidentali);
        AmericaCentrale.Init(StatiUnitiOrientali, StatiUnitiOccidentali, Venezuela);
        StatiUnitiOrientali.Init(AmericaCentrale, Ontario, Quebec, StatiUnitiOccidentali);
        Groenlandia.Init(TerritoriDelNordOvest, Ontario, Quebec, Islanda);
        TerritoriDelNordOvest.Init(Alaska, Alberta, Ontario, Groenlandia);
        Ontario.Init(Alberta, StatiUnitiOrientali, Groenlandia, TerritoriDelNordOvest, Quebec, StatiUnitiOccidentali);
        Quebec.Init(StatiUnitiOrientali, Ontario, TerritoriDelNordOvest, Groenlandia);
        StatiUnitiOccidentali.Init(Alberta, AmericaCentrale, StatiUnitiOrientali, Ontario);

        // Sud America
        Argentina.Init(Brasile, Peru);
        Brasile.Init(Argentina, Peru, Venezuela, NordAfrica);
        Peru.Init(Argentina, Brasile, Venezuela);
        Venezuela.Init(Brasile, Peru, AmericaCentrale);

        // Europa
        GranBretagna.Init(Islanda, EuropaDelNord, Scandinavia, EuropaOccidentale);
        Islanda.Init(GranBretagna, Scandinavia, Groenlandia);
        EuropaDelNord.Init(GranBretagna, Scandinavia, EuropaDelSud, Ukraina, EuropaOccidentale);
        Scandinavia.Init(GranBretagna, Islanda, EuropaDelNord, Ukraina);
        EuropaDelSud.Init(EuropaDelNord, Ukraina, EuropaOccidentale, NordAfrica, Egitto, MedioOriente);
        Ukraina.Init(EuropaDelNord, Scandinavia, EuropaDelSud, Afghanistan, Urali, MedioOriente);
        EuropaOccidentale.Init(GranBretagna, EuropaDelNord, EuropaDelSud, NordAfrica);

        // Afirca
        Congo.Init(AfricaOrientale, NordAfrica, SudAfrica);
        AfricaOrientale.Init(Congo, Egitto, Madagascar, SudAfrica, MedioOriente);
        Egitto.Init(Congo, AfricaOrientale, NordAfrica, EuropaDelSud, MedioOriente);
        Madagascar.Init(AfricaOrientale, SudAfrica);
        NordAfrica.Init(Congo, AfricaOrientale, Egitto, EuropaDelSud, EuropaOccidentale, Brasile);
        SudAfrica.Init(Congo, AfricaOrientale, Madagascar);

        // Asia
        Afghanistan.Init(Cina, India, MedioOriente, Urali, Ukraina);
        Cina.Init(Afghanistan, India, Mongolia, Siam, Siberia, Urali);
        India.Init(Afghanistan, Cina, MedioOriente, Mongolia);
        Irkutsk.Init(Kamchatka, Mongolia, Siberia, Yakutsk);
        Giappone.Init(Kamchatka, Mongolia);
        Kamchatka.Init(Irkutsk, Giappone, Mongolia, Yakutsk, Alaska);
        MedioOriente.Init(Afghanistan, India, AfricaOrientale, Egitto, EuropaDelSud, Ukraina);
        Mongolia.Init(Cina, Irkutsk, Giappone, Kamchatka, Siberia);
        Siam.Init(Cina, India, Indonesia);
        Siberia.Init(Irkutsk, Mongolia, Urali, Yakutsk);
        Urali.Init(Afghanistan, Cina, Siberia, Ukraina);
        Yakutsk.Init(Irkutsk, Kamchatka, Siberia);

        // Australia
        AustraliaOrientale.Init(NuovaGuinea, AustraliaOccidentale);
        Indonesia.Init(NuovaGuinea, AustraliaOccidentale, Siam);
        NuovaGuinea.Init(AustraliaOrientale, Indonesia, AustraliaOccidentale);
        AustraliaOccidentale.Init(AustraliaOrientale, Indonesia, NuovaGuinea);
    }


    private Continente continente;

    public Continente getContinente() { return continente; }

    private ArrayList<Territorio> confinanti = new ArrayList<>();

    Territorio(Continente Continente) {
        this.continente = Continente;
    }

    private void Init(Territorio... Territori) {
        int i = 0;
        while (Territori[i] != null)
            confinanti.add(Territori[i++]);
    }

    public boolean confinaCon(Territorio Territorio) {
        return this.confinanti.contains(Territorio);
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
