package service;

// not sure about player color being a string... could be a ChessGame.TeamColor object as well
public record JoinRequest(String authToken, String playerColor, String gameID) {}
