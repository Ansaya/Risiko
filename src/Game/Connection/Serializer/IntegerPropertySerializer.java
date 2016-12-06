package Game.Connection.Serializer;

import com.google.gson.*;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import java.lang.reflect.Type;

/**
 * Created by fiore on 06/12/2016.
 */
public class IntegerPropertySerializer implements JsonSerializer<IntegerProperty>, JsonDeserializer<IntegerProperty> {

    @Override
    public JsonElement serialize(IntegerProperty src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.get());
    }

    @Override
    public IntegerProperty deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {

        return new SimpleIntegerProperty(json.getAsInt());
    }
}
