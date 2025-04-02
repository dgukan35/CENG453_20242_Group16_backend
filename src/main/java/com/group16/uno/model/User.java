package com.group16.uno.model;
import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "users", uniqueConstraints = {@UniqueConstraint(columnNames = "username")})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String hashed_password;

    @Column(nullable = false)
    private String email;

    @OneToMany
    @JoinColumn(name="user_id")
    private List<DailyScore> dailyScores;


    public User() {}

    public User(String username, String password, String email) {
        this.username = username;
        this.hashed_password = password;
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return hashed_password;
    }

    public void setPassword(String hashed_password) {
        this.hashed_password = hashed_password;
    }

    public String getEmail() {return email;}

    public void setEmail(String email) {this.email = email;}
}