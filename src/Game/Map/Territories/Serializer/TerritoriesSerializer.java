package Game.Map.Territories.Serializer;

import Game.Map.RealWorldMap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;

/**
 * Created by fiore on 18/12/2016.
 */
public class TerritoriesSerializer implements JsonSerializer<RealWorldMap> {
    @Override
    public JsonElement serialize(RealWorldMap src, Type typeOfSrc, JsonSerializationContext context) {

        JsonObject json = new JsonObject();
        json.addProperty("Name", src.toString());
        json.addProperty("Card", src.toString());
        json.add



        return null;
    }
}
