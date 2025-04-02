package websocket.commands;

import com.google.gson.*;

import java.lang.reflect.Type;

public class UserGameCommandDeserializer implements JsonDeserializer<UserGameCommand> {
    @Override
    public UserGameCommand deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        String commandType = jsonObject.get("commandType").getAsString();

        switch (commandType) {
            case "CONNECT":
                return new Gson().fromJson(jsonElement, Connect.class);
            default:
                throw new JsonParseException("Unknown command type: " + commandType);

        }
    }
}
