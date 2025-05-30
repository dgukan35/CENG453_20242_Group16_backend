package com.group16.uno.service;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.stereotype.Service;

@Service
public class GameRoomService {

    private Map<String, GameRoom> gameRooms = new HashMap<>();
    private final Lock lock = new ReentrantLock();

    public String createGameRoom() {
        lock.lock();
        try {
            String roomId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            gameRooms.put(roomId, new GameRoom(roomId));
            return roomId;
        } finally {
            lock.unlock();
        }
    }

    public GameRoom getGameRoom(String roomId) {
        return gameRooms.get(roomId);
    }

    public void addPlayerToRoom(String roomId, String playerId) {
        lock.lock();
        try {
            GameRoom room = gameRooms.get(roomId);
            if (room != null && room.players.size() < 4) {
                room.addPlayer(playerId);
            }
        } finally {
            lock.unlock();
        }
    }

    public boolean removePlayerFromRoom(String roomId, String playerId) {
        lock.lock();
        try {
            GameRoom room = gameRooms.get(roomId);
            if (room != null) {
                boolean removed = room.removePlayer(playerId);
                if (room.players.isEmpty()) {
                    gameRooms.remove(roomId);
                }
                return removed;
            }
            return false;
        } finally {
            lock.unlock();
        }
    }

    // Additional methods for managing players and game state

    public static class GameRoom {
        private String roomId;
        private List<String> players = new ArrayList<>();
        private Map<String, List<Card>> playerHands = new HashMap<>();
        private List<Card> deck = new ArrayList<>();
        private List<Card> discardPile = new ArrayList<>();
        private int currentPlayerIndex = 0;
        private String currentColor;
        private boolean clockwise = true;
        private int drawStack = 0;
        private boolean gameStarted = false;

        public GameRoom(String roomId) {
            this.roomId = roomId;
            initializeDeck();
        }

        public void addPlayer(String playerId) {
            if (players.size() < 4 && !players.contains(playerId)) {
                players.add(playerId);
                playerHands.put(playerId, new ArrayList<>());
            }
        }

        public boolean removePlayer(String playerId) {
            playerHands.remove(playerId);
            return players.remove(playerId);
        }

        public void startGame() {
            if (players.size() >= 2 && !gameStarted) {
                gameStarted = true;
                shuffleDeck();
                
                // Deal 7 cards to each player
                for (String player : players) {
                    List<Card> hand = new ArrayList<>();
                    for (int i = 0; i < 7; i++) {
                        hand.add(drawCard());
                    }
                    playerHands.put(player, hand);
                }
                
                // Set first card (not Wild Draw Four)
                Card firstCard;
                do {
                    firstCard = drawCard();
                } while (firstCard.getValue().equals("Wild Draw Four"));
                
                discardPile.add(firstCard);
                currentColor = firstCard.getColor();
            }
        }

        public boolean playCard(String playerId, String cardValue, String cardColor, String chosenColor) {
            if (!gameStarted || !players.get(currentPlayerIndex).equals(playerId)) {
                return false;
            }
            
            List<Card> playerHand = playerHands.get(playerId);
            Card cardToPlay = null;
            
            // Find the card in player's hand
            for (Card card : playerHand) {
                if (card.getValue().equals(cardValue) && card.getColor().equals(cardColor)) {
                    cardToPlay = card;
                    break;
                }
            }
            
            if (cardToPlay == null || !isValidPlay(cardToPlay)) {
                return false;
            }
            
            // Remove card from player's hand
            playerHand.remove(cardToPlay);
            
            // Add to discard pile
            discardPile.add(cardToPlay);
            
            // Handle special cards
            handleSpecialCard(cardToPlay, chosenColor);
            
            // Move to next player
            moveToNextPlayer();
            
            return true;
        }

        private boolean isValidPlay(Card card) {
            if (discardPile.isEmpty()) return true;
            
            Card topCard = discardPile.get(discardPile.size() - 1);
            
            if (drawStack > 0) {
                if (topCard.getValue().equals("Draw Two")) {
                    return card.getValue().equals("Draw Two");
                }
                if (topCard.getValue().equals("Wild Draw Four")) {
                    return card.getValue().equals("Wild Draw Four");
                }
                return false;
            }
            
            return card.getValue().equals("Wild") || 
                   card.getValue().equals("Wild Draw Four") ||
                   card.getColor().equals(currentColor) || 
                   card.getValue().equals(topCard.getValue());
        }

        private void handleSpecialCard(Card card, String chosenColor) {
            String value = card.getValue();
            
            switch (value) {
                case "Skip":
                    moveToNextPlayer(); // Skip one more time
                    break;
                case "Reverse":
                    clockwise = !clockwise;
                    if (players.size() == 2) {
                        moveToNextPlayer(); // In 2-player game, reverse acts like skip
                    }
                    break;
                case "Draw Two":
                    drawStack += 2;
                    break;
                case "Wild":
                    currentColor = chosenColor;
                    break;
                case "Wild Draw Four":
                    currentColor = chosenColor;
                    drawStack = 4;
                    break;
                default:
                    currentColor = card.getColor();
            }
        }

        private void moveToNextPlayer() {
            if (clockwise) {
                currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
            } else {
                currentPlayerIndex = (currentPlayerIndex - 1 + players.size()) % players.size();
            }
        }

        private Card drawCard() {
            if (deck.isEmpty()) {
                // Reshuffle discard pile back into deck
                if (discardPile.size() > 1) {
                    Card topCard = discardPile.remove(discardPile.size() - 1);
                    deck.addAll(discardPile);
                    discardPile.clear();
                    discardPile.add(topCard);
                    shuffleDeck();
                }
            }
            return deck.isEmpty() ? null : deck.remove(0);
        }

        private void shuffleDeck() {
            Collections.shuffle(deck);
        }

        private void initializeDeck() {
            String[] colors = {"red", "yellow", "green", "blue"};
            String[] values = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "Skip", "Reverse", "Draw Two"};
            
            // Add numbered and action cards (2 of each except 0)
            for (String color : colors) {
                for (String value : values) {
                    deck.add(new Card(value, color));
                    if (!value.equals("0")) {
                        deck.add(new Card(value, color));
                    }
                }
            }
            
            // Add Wild cards
            for (int i = 0; i < 4; i++) {
                deck.add(new Card("Wild", "black"));
                deck.add(new Card("Wild Draw Four", "black"));
            }
            
            shuffleDeck();
        }

        // Getters
        public String getRoomId() { return roomId; }
        public List<String> getPlayers() { return new ArrayList<>(players); }
        public String getCurrentPlayer() { 
            return players.isEmpty() ? null : players.get(currentPlayerIndex); 
        }
        public Card getTopCard() { 
            return discardPile.isEmpty() ? null : discardPile.get(discardPile.size() - 1); 
        }
        public String getCurrentColor() { return currentColor; }
        public boolean isClockwise() { return clockwise; }
        public int getDrawStack() { return drawStack; }
        public boolean isGameStarted() { return gameStarted; }
        public int getPlayerHandSize(String playerId) {
            List<Card> hand = playerHands.get(playerId);
            return hand != null ? hand.size() : 0;
        }
        public List<Card> getPlayerHand(String playerId) {
            return playerHands.getOrDefault(playerId, new ArrayList<>());
        }
    }

    public static class Card {
        private String value;
        private String color;

        public Card(String value, String color) {
            this.value = value;
            this.color = color;
        }

        public String getValue() { return value; }
        public String getColor() { return color; }
        
        @Override
        public String toString() {
            return color + "_" + value;
        }
    }
} 