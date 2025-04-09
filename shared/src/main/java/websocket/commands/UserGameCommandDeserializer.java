package websocket.commands;

import com.google.gson.*;

import java.lang.reflect.Type;

public class UserGameCommandDeserializer implements JsonDeserializer<UserGameCommand> {

    @Override
    public UserGameCommand deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        String commandType = jsonObject.get("commandType").getAsString();

        Gson gson = new Gson();

        return switch (commandType) {
            case "CONNECT" -> gson.fromJson(jsonElement, Connect.class);
            case "MAKE_MOVE" -> gson.fromJson(jsonElement, MakeMove.class);
            default -> throw new JsonParseException("Unknown command type: " + commandType);
        };
    }
}
