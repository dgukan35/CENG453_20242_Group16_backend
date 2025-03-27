package com.group16.uno.service;
import com.group16.uno.model.User;
import com.group16.uno.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User createUser(String username, String email, String plainPassword) {
        String hashedPassword = passwordEncoder.encode(plainPassword);
        User user = new User(username, email, hashedPassword);
        return userRepository.save(user);
    }

    public Optional<User> getUserById(String Id) {
        return userRepository.findById(Id);
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}