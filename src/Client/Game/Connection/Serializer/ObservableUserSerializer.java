package Client.Game.Connection.Serializer;

import Client.Game.Player;
import Game.Map.Army.Color;
import com.google.gson.*;
import java.lang.reflect.Type;

/**
 * Serializer/deserializer for observable user class
 */
public class ObservableUserSerializer implements JsonSerializer<Player>, JsonDeserializer<Player> {
    @Override
    public JsonElement serialize(Player src, Type typeOfSrc, JsonSerializationContext context) {
        final JsonObject obj = new JsonObject();
        obj.addProperty("id", src.Id.get());
        obj.addProperty("username", src.Username.get());
        if(src.Color != null)
            obj.addProperty("color", src.Color.name());

        return obj;
    }

    @Override
    public Player deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();
        return new Player(obj.get("id").getAsInt(),
                                  obj.get("username").getAsString(),
                                  obj.has("color") ? Color.valueOf(obj.get("color").getAsString()) : null);
    }
}
