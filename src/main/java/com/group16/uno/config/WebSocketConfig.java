package com.group16.uno.config;

import com.group16.uno.config.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.messaging.simp.stomp.StompHeaders;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue"); // for broadcasting to clients and individual messages
        config.setApplicationDestinationPrefixes("/app"); // for receiving from clients
        config.setUserDestinationPrefix("/user"); // for user-specific messages
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/uno-websocket").setAllowedOriginPatterns("*").withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                
                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    System.out.println("WebSocket CONNECT request received");
                    

                    String jwt = null;
                    

                    final String authHeader = accessor.getFirstNativeHeader("Authorization");
                    System.out.println("Native Authorization header: " + (authHeader != null ? "Present" : "NULL"));
                    
                    if (authHeader != null && authHeader.startsWith("Bearer ")) {
                        jwt = authHeader.substring(7);
                        System.out.println("JWT found in native Authorization header");
                    } else {

                        StompHeaders stompHeaders = StompHeaders.readOnlyStompHeaders(accessor.toNativeHeaderMap());
                        String stompAuthHeader = stompHeaders.getFirst("Authorization");
                        System.out.println("STOMP Authorization header: " + (stompAuthHeader != null ? "Present" : "NULL"));
                        
                        if (stompAuthHeader != null && stompAuthHeader.startsWith("Bearer ")) {
                            jwt = stompAuthHeader.substring(7);
                            System.out.println("JWT found in STOMP Authorization header");
                        }
                    }

                    if (jwt == null || jwt.trim().isEmpty()) {
                        System.out.println("No JWT token found in any headers");
                        throw new RuntimeException("No JWT token found");
                    }
                    
                    System.out.println("Extracted JWT: " + jwt.substring(0, Math.min(20, jwt.length())) + "...");
                    
                    final String username = jwtService.extractUsername(jwt);
                    System.out.println("Extracted username: " + username);

                    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                        System.out.println("User details loaded for: " + userDetails.getUsername());
                        
                        if (jwtService.isTokenValid(jwt, userDetails)) {
                            UsernamePasswordAuthenticationToken token =
                                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                            accessor.setUser(token);
                            SecurityContextHolder.getContext().setAuthentication(token);
                            System.out.println("WebSocket JWT authentication successful for user: " + username);
                        } else {
                            System.out.println("JWT token is invalid for user: " + username);
                            throw new RuntimeException("Invalid JWT token");
                        }
                    } else {
                        System.out.println("Invalid username (" + username + ") or already authenticated");
                        throw new RuntimeException("Invalid username or already authenticated");
                    }
                }
                
                return message;
            }
        });
    }
} 