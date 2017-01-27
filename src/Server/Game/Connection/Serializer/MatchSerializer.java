package Server.Game.Connection.Serializer;

import Server.Game.Match;
import Server.Game.Player;
import com.google.gson.*;
import java.lang.reflect.Type;

/**
 * Created by fiore on 27/01/2017.
 */
public class MatchSerializer implements JsonSerializer<Match> {
    @Override
    public JsonElement serialize(Match src, Type typeOfSrc, JsonSerializationContext context) {
        final JsonObject jm = new JsonObject();

        jm.addProperty("Id", src.Id);
        jm.addProperty("Name", src.Name);
        jm.addProperty("GameMap", src.GameMap.name());
        jm.addProperty("IsStarted", src.isStarted());

        final JsonArray pa = new JsonArray();
        src.getPlayers().forEach((id, p) -> {
            if(p.isPlaying() || !src.isStarted())
                pa.add(context.serialize(p, Player.class));
        });

        jm.add("Players", pa);

        return jm;
    }
}
