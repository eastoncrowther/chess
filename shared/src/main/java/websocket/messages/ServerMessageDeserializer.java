package websocket.messages;

import com.google.gson.*;

import java.lang.reflect.Type;

public class ServerMessageDeserializer implements JsonDeserializer<ServerMessage> {

    @Override
    public ServerMessage deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        String messageType = jsonObject.get("commandType").getAsString();

        Gson gson = new Gson();

        return switch (messageType) {
            case "NOTIFICATION" -> gson.fromJson(jsonElement, NotificationMessage.class);
            case "LOAD_GAME" -> gson.fromJson(jsonElement, LoadGameMessage.class);
            case "ERROR" -> gson.fromJson(jsonElement, ErrorMessage.class);
            default -> throw new JsonParseException("Unknown command type: " + messageType);
        };
    }
}
