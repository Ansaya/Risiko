package Game.Connection.Serializer;

import com.google.gson.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import java.lang.reflect.Type;

/**
 * Created by fiore on 06/12/2016.
 */
public class StringPropertySerializer implements JsonSerializer<StringProperty>, JsonDeserializer<StringProperty> {
    @Override
    public JsonElement serialize(StringProperty src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(src.get());
    }

    @Override
    public StringProperty deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return new SimpleStringProperty(json.getAsString());
    }
}
