package com.group16.uno.controller;

import com.group16.uno.service.GameRoomService;
import com.group16.uno.service.GameStateService;
import com.group16.uno.dto.CreateRoomRequest;
import com.group16.uno.dto.JoinRoomRequest;
import com.group16.uno.dto.GameRoomResponse;
import com.group16.uno.dto.CardDataDTO;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.Map;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/api/game")
@Tag(name = "UNO Game Room Management", description = "REST API for managing UNO multiplayer game rooms")
public class GameWebSocketController {

    private final GameRoomService gameRoomService;
    private final GameStateService gameStateService;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public GameWebSocketController(GameRoomService gameRoomService, GameStateService gameStateService, SimpMessagingTemplate messagingTemplate) {
        this.gameRoomService = gameRoomService;
        this.gameStateService = gameStateService;
        this.messagingTemplate = messagingTemplate;
    }

    // REST Endpoints for room management
    @Operation(
        summary = "Create a new UNO game room",
        description = "Creates a new multiplayer UNO game room and adds the creator as the first player",
        tags = {"Room Management"}
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Room created successfully",
            content = @Content(mediaType = "application/json", 
            schema = @Schema(implementation = GameRoomResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request - Player name is required",
            content = @Content(mediaType = "application/json", 
            schema = @Schema(implementation = GameRoomResponse.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(mediaType = "application/json", 
            schema = @Schema(implementation = GameRoomResponse.class)))
    })
    @PostMapping("/create-room")
    public ResponseEntity<GameRoomResponse> createRoom(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Player information for room creation",
                required = true,
                content = @Content(schema = @Schema(implementation = CreateRoomRequest.class))
            )
            @RequestBody CreateRoomRequest request) {
        try {
            String playerName = request.getPlayerName();
            
            if (playerName == null || playerName.trim().isEmpty()) {
                GameRoomResponse errorResponse = new GameRoomResponse(false, "Player name is required");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            String gameId = gameRoomService.createGameRoom();
            gameRoomService.addPlayerToRoom(gameId, playerName);
            
            // Get the created room to populate response with complete information
            GameRoomService.GameRoom gameRoom = gameRoomService.getGameRoom(gameId);
            
            GameRoomResponse response = new GameRoomResponse(true, "Room created successfully");
            response.setRoomId(gameId);
            response.setPlayerCount(gameRoom.getPlayers().size());
            response.setPlayers(gameRoom.getPlayers());
            response.setGameStarted(gameRoom.isGameStarted());
            response.setCurrentPlayer(gameRoom.getCurrentPlayer());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            GameRoomResponse errorResponse = new GameRoomResponse(false, "Failed to create room: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @Operation(
        summary = "Join an existing UNO game room",
        description = "Allows a player to join an existing UNO game room using the room ID",
        tags = {"Room Management"}
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully joined room",
            content = @Content(mediaType = "application/json", 
            schema = @Schema(implementation = GameRoomResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request - Room ID or player name missing/invalid",
            content = @Content(mediaType = "application/json", 
            schema = @Schema(implementation = GameRoomResponse.class))),
        @ApiResponse(responseCode = "404", description = "Room not found",
            content = @Content(mediaType = "application/json", 
            schema = @Schema(implementation = GameRoomResponse.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(mediaType = "application/json", 
            schema = @Schema(implementation = GameRoomResponse.class)))
    })
    @PostMapping("/join-room")
    public ResponseEntity<GameRoomResponse> joinRoom(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Room and player information for joining",
                required = true,
                content = @Content(schema = @Schema(implementation = JoinRoomRequest.class))
            )
            @RequestBody JoinRoomRequest request) {
        try {
            String roomId = request.getRoomId();
            String playerName = request.getPlayerName();
            
            if (roomId == null || roomId.trim().isEmpty()) {
                GameRoomResponse errorResponse = new GameRoomResponse(false, "Room ID is required");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            if (playerName == null || playerName.trim().isEmpty()) {
                GameRoomResponse errorResponse = new GameRoomResponse(false, "Player name is required");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            GameRoomService.GameRoom gameRoom = gameRoomService.getGameRoom(roomId);
            
            if (gameRoom == null) {
                GameRoomResponse errorResponse = new GameRoomResponse(false, "Room not found");
                return ResponseEntity.notFound().build();
            }
            
            if (gameRoom.getPlayers().size() >= 4) {
                GameRoomResponse errorResponse = new GameRoomResponse(false, "Room is full (maximum 4 players)");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            if (gameRoom.getPlayers().contains(playerName)) {
                GameRoomResponse errorResponse = new GameRoomResponse(false, "Player already in room");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            gameRoomService.addPlayerToRoom(roomId, playerName);
            
            // Notify other players via WebSocket
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "PLAYER_JOINED");
            notification.put("player", playerName);
            notification.put("playerCount", gameRoom.getPlayers().size());
            notification.put("message", playerName + " joined the room");
            
            messagingTemplate.convertAndSend("/topic/room/" + roomId, notification);
            
            GameRoomResponse response = new GameRoomResponse(true, "Successfully joined room");
            response.setRoomId(roomId);
            response.setPlayerCount(gameRoom.getPlayers().size());
            response.setPlayers(gameRoom.getPlayers());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            GameRoomResponse errorResponse = new GameRoomResponse(false, "Failed to join room: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @Operation(
        summary = "Get UNO game room status",
        description = "Retrieves current status and information about a specific UNO game room",
        tags = {"Room Management"}
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Room status retrieved successfully",
            content = @Content(mediaType = "application/json", 
            schema = @Schema(implementation = GameRoomResponse.class))),
        @ApiResponse(responseCode = "404", description = "Room not found",
            content = @Content(mediaType = "application/json", 
            schema = @Schema(implementation = GameRoomResponse.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(mediaType = "application/json", 
            schema = @Schema(implementation = GameRoomResponse.class)))
    })
    @GetMapping("/room/{roomId}/status")
    public ResponseEntity<GameRoomResponse> getRoomStatus(
            @Parameter(description = "Room ID to check status for", example = "ABC12345", required = true)
            @PathVariable String roomId) {
        try {
            GameRoomService.GameRoom gameRoom = gameRoomService.getGameRoom(roomId);
            
            if (gameRoom == null) {
                GameRoomResponse errorResponse = new GameRoomResponse(false, "Room not found");
                return ResponseEntity.notFound().build();
            }
            
            GameRoomResponse response = new GameRoomResponse(true, "Room status retrieved successfully");
            response.setRoomId(roomId);
            response.setPlayerCount(gameRoom.getPlayers().size());
            response.setPlayers(gameRoom.getPlayers());
            response.setGameStarted(gameRoom.isGameStarted());
            response.setCurrentPlayer(gameRoom.getCurrentPlayer());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            GameRoomResponse errorResponse = new GameRoomResponse(false, "Failed to get room status: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @MessageMapping("/playCard") // e.g., /app/playCard
    public void playCard(Map<String, String> cardMessage) {
        String gameId = cardMessage.get("gameId");
        String card = cardMessage.get("card");
        String color = cardMessage.get("color");
        String playerName = cardMessage.get("player");
        String chosenColor = cardMessage.get("chosenColor");
        
        try {
            GameStateService.MultiplayerGameState gameState = gameStateService.getGame(gameId);
            
            if (gameState != null && gameState.isGameStarted()) {
                // Parse card value and color from card string (e.g., "red_7")
                String[] cardParts = card.split("_");
                String cardColor = cardParts.length > 1 ? cardParts[0] : color;
                String cardValue = cardParts.length > 1 ? cardParts[1] : card;
                
                // Create CardDataDTO for backend validation
                CardDataDTO cardToPlay = new CardDataDTO(cardValue, cardColor);
                
                // Validate if player can play this card
                if (!gameState.getCurrentPlayerName().equals(playerName)) {
                    throw new IllegalStateException("Not your turn");
                }
                
                if (!gameState.canPlayCard(cardToPlay, playerName)) {
                    throw new IllegalStateException("Invalid card play");
                }
                
                // Play the card through backend logic
                gameState.playCard(playerName, cardToPlay, chosenColor);
                
                // Broadcast successful card play to all players
                Map<String, Object> response = new HashMap<>();
                response.put("type", "CARD_PLAYED");
                response.put("player", playerName);
                response.put("card", card);
                response.put("gameId", gameId);
                response.put("currentPlayer", gameState.getCurrentPlayerName());
                response.put("topCard", gameState.getTopCard().toString());
                response.put("currentColor", gameState.getCurrentColor());
                response.put("clockwise", gameState.isClockwise());
                response.put("drawStack", gameState.getDrawStack());
                response.put("direction", gameState.isClockwise() ? 1 : -1);
                
                // Check for winner
                if (gameState.isGameOver()) {
                    response.put("winner", gameState.getWinner());
                    response.put("type", "GAME_OVER");
                }
                
                // Send updated game state to each player with their specific hand
                for (String player : gameState.getPlayerOrder()) {
                    Map<String, Object> playerResponse = new HashMap<>(response);
                    playerResponse.put("playerHand", gameState.getPlayerHand(player));
                    
                    // Calculate hand sizes for all players
                    Map<String, Integer> handSizes = new HashMap<>();
                    for (String p : gameState.getPlayerOrder()) {
                        handSizes.put(p, gameState.getPlayerHand(p).size());
                    }
                    playerResponse.put("handSizes", handSizes);
                    
                    messagingTemplate.convertAndSendToUser(player, "/queue/gameState", playerResponse);
                }
                
                // Broadcast to room
                messagingTemplate.convertAndSend("/topic/game/" + gameId, response);
                
            }
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("type", "INVALID_MOVE");
            errorResponse.put("message", "Failed to play card: " + e.getMessage());
            
            messagingTemplate.convertAndSendToUser(playerName, "/queue/errors", errorResponse);
        }
    }

    @MessageMapping("/drawCard") // e.g., /app/drawCard
    public void drawCard(Map<String, String> drawMessage) {
        String gameId = drawMessage.get("gameId");
        String playerName = drawMessage.get("player");
        String drawCount = drawMessage.get("drawCount");
        
        try {
            GameStateService.MultiplayerGameState gameState = gameStateService.getGame(gameId);
            
            if (gameState != null && gameState.isGameStarted()) {
                // Validate it's player's turn
                if (!gameState.getCurrentPlayerName().equals(playerName)) {
                    throw new IllegalStateException("Not your turn");
                }
                
                int cardsToTraw = drawCount != null ? Integer.parseInt(drawCount) : 1;
                
                // Handle forced draws (draw stack)
                if (gameState.getDrawStack() > 0) {
                    gameState.handleForcedDraw(playerName);
                    cardsToTraw = gameState.getDrawStack();
                } else {
                    // Normal draw
                    gameState.drawCards(playerName, cardsToTraw);
                    gameState.moveToNextPlayer();
                }
                
                // Broadcast draw action to all players
                Map<String, Object> response = new HashMap<>();
                response.put("type", "CARDS_DRAWN");
                response.put("player", playerName);
                response.put("drawCount", cardsToTraw);
                response.put("gameId", gameId);
                response.put("currentPlayer", gameState.getCurrentPlayerName());
                response.put("drawStack", gameState.getDrawStack());
                
                // Send updated game state to each player
                for (String player : gameState.getPlayerOrder()) {
                    Map<String, Object> playerResponse = new HashMap<>(response);
                    playerResponse.put("playerHand", gameState.getPlayerHand(player));
                    
                    // Calculate hand sizes for all players
                    Map<String, Integer> handSizes = new HashMap<>();
                    for (String p : gameState.getPlayerOrder()) {
                        handSizes.put(p, gameState.getPlayerHand(p).size());
                    }
                    playerResponse.put("handSizes", handSizes);
                    
                    messagingTemplate.convertAndSendToUser(player, "/queue/gameState", playerResponse);
                }
                
                // Broadcast to room
                messagingTemplate.convertAndSend("/topic/game/" + gameId, response);
            }
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("type", "ERROR");
            errorResponse.put("message", "Failed to draw card: " + e.getMessage());
            
            messagingTemplate.convertAndSendToUser(playerName, "/queue/errors", errorResponse);
        }
    }

    @MessageMapping("/test") // e.g., /app/test  
    @SendTo("/topic/test")
    public String testMessage(String message) {
        return "Echo: " + message;
    }

    @MessageMapping("/startGame") // e.g., /app/startGame
    public void startGame(Map<String, String> startMessage) {
        String gameId = startMessage.get("gameId");
        String playerName = startMessage.get("player");
        
        try {
            GameRoomService.GameRoom gameRoom = gameRoomService.getGameRoom(gameId);
            
            if (gameRoom != null && gameRoom.getPlayers().size() >= 2) {
                // Create backend game state
                GameStateService.MultiplayerGameState gameState = gameStateService.createGame(gameId);
                gameState.startGame(gameRoom.getPlayers());
                
                // Mark room as started
                gameRoom.startGame();
                
                // Notify all players that game has started
                Map<String, Object> response = new HashMap<>();
                response.put("type", "GAME_STARTED");
                response.put("gameId", gameId);
                response.put("currentPlayer", gameState.getCurrentPlayerName());
                response.put("topCard", gameState.getTopCard().toString());
                response.put("currentColor", gameState.getCurrentColor());
                response.put("players", gameState.getPlayerOrder());
                response.put("direction", gameState.isClockwise() ? 1 : -1);
                response.put("message", "Game started!");
                
                // Send game state to each player with their specific hand
                for (String player : gameState.getPlayerOrder()) {
                    Map<String, Object> playerResponse = new HashMap<>(response);
                    playerResponse.put("playerHand", gameState.getPlayerHand(player));
                    playerResponse.put("playerIndex", gameState.getPlayerOrder().indexOf(player));
                    
                    // Calculate hand sizes for all players
                    Map<String, Integer> handSizes = new HashMap<>();
                    for (String p : gameState.getPlayerOrder()) {
                        handSizes.put(p, gameState.getPlayerHand(p).size());
                    }
                    playerResponse.put("handSizes", handSizes);
                    
                    messagingTemplate.convertAndSendToUser(player, "/queue/gameState", playerResponse);
                }
                
                // Also broadcast to room
                messagingTemplate.convertAndSend("/topic/game/" + gameId, response);
            }
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("type", "ERROR");
            errorResponse.put("message", "Failed to start game: " + e.getMessage());
            
            messagingTemplate.convertAndSendToUser(playerName, "/queue/errors", errorResponse);
        }
    }

    private Map<String, Integer> getHandSizes(GameRoomService.GameRoom gameRoom) {
        Map<String, Integer> handSizes = new HashMap<>();
        for (String player : gameRoom.getPlayers()) {
            handSizes.put(player, gameRoom.getPlayerHandSize(player));
        }
        return handSizes;
    }

    // WebSocket Message Handlers for real-time gameplay
    @MessageMapping("/join") // e.g., /app/join - for WebSocket subscription
    public void joinGameWebSocket(Map<String, String> joinMessage) {
        String gameId = joinMessage.get("gameId");
        String playerName = joinMessage.get("player");
        
        try {
            GameRoomService.GameRoom gameRoom = gameRoomService.getGameRoom(gameId);
            
            if (gameRoom != null) {
                // Notify all players in the room about the WebSocket connection
                Map<String, Object> response = new HashMap<>();
                response.put("type", "PLAYER_CONNECTED");
                response.put("player", playerName);
                response.put("gameId", gameId);
                response.put("message", playerName + " connected to game");
                
                messagingTemplate.convertAndSend("/topic/game/" + gameId, response);
            }
        } catch (Exception e) {
            // Send error message back to the player
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("type", "ERROR");
            errorResponse.put("message", "Failed to join game via WebSocket: " + e.getMessage());
            
            messagingTemplate.convertAndSendToUser(playerName, "/queue/errors", errorResponse);
        }
    }
} 