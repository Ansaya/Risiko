package Game.Sounds;

import javafx.scene.media.AudioClip;
import java.util.prefs.Preferences;

/**
 * Application sounds
 */
public enum Sounds {
    Button("button.mp3"),
    Match("match.mp3"),
    Chat("chat.mp3"),
    Victory("victory.mp3"),
    CardTris("cardTris.mp3"),
    Battle01("battle01.mp3"),
    Battle02("battle02.mp3"),
    Battle03("battle03.mp3"),
    Battle10("battle10.mp3"),
    Battle11("battle11.mp3"),
    Battle12("battle12.mp3"),
    Battle20("battle20.mp3"),
    Battle21("battle21.mp3"),
    Battle30("battle30.mp3");

    private final AudioClip sound;

    private static final Preferences prefs = Preferences.userNodeForPackage(Client.Main.class);

    private static double volume = prefs.getDouble("volume", 80.0);

    Sounds(String fileName) {
        sound = new AudioClip(Sounds.class.getResource(fileName).toExternalForm());
    }

    public static double getVolume() {
        return volume;
    }

    public static void setVolume(double newVolume) {
        volume = newVolume;
        prefs.putDouble("volume", newVolume);
    }

    public void play() {
        sound.setVolume(volume);
        sound.play();
    }

    /**
     * Select correct sound to play for battle result
     *
     * @param LostAtk Armies lost from attack
     * @param LostDef Armies lost from defense
     * @return Sound to play
     */
    public static Sounds battleSoundSelector(int LostAtk, int LostDef) {
        switch ("" + LostDef + "" + LostAtk){
            case "01":
                return Sounds.Battle01;
            case "02":
                return Sounds.Battle02;
            case "03":
                return Sounds.Battle03;
            case "10":
                return Sounds.Battle10;
            case "11":
                return Sounds.Battle11;
            case "12":
                return Sounds.Battle12;
            case "20":
                return Sounds.Battle20;
            case "21":
                return Sounds.Battle21;
            case "30":
                return Sounds.Battle30;
            default:
                return Sounds.Battle21;
        }
    }
}
