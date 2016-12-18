package Client.Game.Connection.Serializer;

import Client.Game.Observables.ObservableUser;
import Game.Map.Army.Color;
import com.google.gson.*;
import java.lang.reflect.Type;

/**
 * Serializer/deserializer for observable user class
 */
public class ObservableUserSerializer implements JsonSerializer<ObservableUser>, JsonDeserializer<ObservableUser> {
    @Override
    public JsonElement serialize(ObservableUser src, Type typeOfSrc, JsonSerializationContext context) {

        JsonObject obj = new JsonObject();
        obj.addProperty("id", src.id.get());
        obj.addProperty("username", src.username.get());
        if(src.color != null)
            obj.addProperty("color", src.color.name());

        return obj;
    }

    @Override
    public ObservableUser deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();
        return new ObservableUser(obj.get("id").getAsInt(),
                                  obj.get("username").getAsString(),
                                  obj.has("color") ? Color.valueOf(obj.get("color").getAsString()) : null);
    }
}
