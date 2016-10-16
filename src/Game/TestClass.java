package Game;

import Game.Map.Territories;
import Game.Map.Territory;

import java.util.ArrayList;

/**
 * Created by fiore on 16/10/2016.
 */
public class TestClass {

    private int integer;

    public void setInteger(int i) { this.integer = i; }

    public int getInteger() { return this.integer; }

    private ArrayList<Territory> territories = new ArrayList<>();

    public ArrayList<Territory> getTerritories() { return this.territories; }
}
