package service;
import model.GameData;

public record CreateRequest(String authToken, GameData game) {}
