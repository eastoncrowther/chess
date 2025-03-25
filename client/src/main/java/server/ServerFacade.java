package server;

import com.google.gson.Gson;
import requestresult.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

public class ServerFacade {
    private final String serverUrl;

    public ServerFacade (String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public void clear () throws Exception {
        var path = "/db";
        this.makeRequest("DELETE", path, null, null, null);

    }

    public RegisterResult register (RegisterRequest registerRequest) throws Exception{
        var path = "/user";
        return this.makeRequest("POST", path, registerRequest, RegisterResult.class, null);
    }

    public LoginResult login (LoginRequest loginRequest) throws Exception {
        var path = "/session";
        return this.makeRequest("POST", path, loginRequest, LoginResult.class, null);
    }

    public void logout (String authToken) throws Exception {
        var path = "/session";
        this.makeRequest("DELETE", path, null, null, authToken);

    }

    public ListResult list (String authToken) throws Exception {
        var path = "/game";
        return this.makeRequest("GET", path, null, ListResult.class, authToken);
    }

    public CreateResult createGame (CreateRequest createRequest, String authToken) throws Exception {
        var path = "/game";
        return this.makeRequest("POST", path, createRequest, CreateResult.class, authToken);
    }

    public void join (JoinRequest joinRequest, String authToken) throws Exception {
        var path = "/game";
        this.makeRequest("PUT", path, joinRequest, null, authToken);
    }


    private <T> T makeRequest(String method, String path, Object request, Class<T> responseClass, String authToken) throws Exception {
        URL url = (new URI(serverUrl + path)).toURL();
        HttpURLConnection http = (HttpURLConnection) url.openConnection();
        http.setRequestMethod(method);
        http.setDoOutput(true);

        if (authToken != null) {
            http.setRequestProperty("Authorization", authToken);
        }

        writeBody(request, http);
        http.connect();
        throwIfNotSuccessful(http);
        return readBody(http, responseClass);
    }

    private static void writeBody(Object request, HttpURLConnection http) throws IOException {
        if (request != null) {
            http.addRequestProperty("Content-Type", "application/json");
            String reqData = new Gson().toJson(request);
            try (OutputStream reqBody = http.getOutputStream()) {
                reqBody.write(reqData.getBytes());
            }
        }
    }

    private static <T> T readBody(HttpURLConnection http, Class<T> responseClass) throws IOException {
        T response = null;
        if (http.getContentLength() < 0) {
            try (InputStream respBody = http.getInputStream()) {
                InputStreamReader reader = new InputStreamReader(respBody);
                if (responseClass != null) {
                    response = new Gson().fromJson(reader, responseClass);
                }
            }
        }
        return response;
    }

    private void throwIfNotSuccessful (HttpURLConnection http) throws Exception {
        int status = http.getResponseCode();
        if (status / 100 != 2) {
            try (InputStream respErr = http.getErrorStream()) {
                if (respErr != null) {
                    throw new Exception("other failure: " + status);
                }
            }
        }
    }
}
