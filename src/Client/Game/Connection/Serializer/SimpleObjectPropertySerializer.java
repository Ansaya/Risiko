package Client.Game.Connection.Serializer;

import com.google.gson.*;
import javafx.beans.property.SimpleObjectProperty;
import java.lang.reflect.Type;

/**
 * Created by fiore on 22/01/2017.
 */
public class SimpleObjectPropertySerializer<T> implements JsonSerializer<SimpleObjectProperty<T>>, JsonDeserializer<SimpleObjectProperty<T>> {

    private final Type typeOfObject;

    public SimpleObjectPropertySerializer(Type TypeOfObject) {
        typeOfObject = TypeOfObject;
    }

    @Override
    public SimpleObjectProperty deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return new SimpleObjectProperty<T>(context.deserialize(json,typeOfObject));
    }

    @Override
    public JsonElement serialize(SimpleObjectProperty src, Type typeOfSrc, JsonSerializationContext context) {
        return context.serialize(src.get(), typeOfObject);
    }
}
