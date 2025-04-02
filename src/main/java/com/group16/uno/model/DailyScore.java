package com.group16.uno.model;

import jakarta.persistence.*;
import java.sql.Date;

@Entity
@Table(name = "daily_score", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "created_at"}))
public class DailyScore {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne
    @JoinColumn(name="user_id")
    private User user;

    @Column(nullable = false)
    private Date createdAt;

    @Column(nullable = false)
    private Integer score;

    public DailyScore() {}

    public DailyScore(User user, Date createdAt, Integer score) {
        this.user = user;
        this.createdAt = createdAt;
        this.score = score;
    }

    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    public User getUser() { return user; }

    public void setUser(User user) { this.user = user; }

    public Date geteDate() { return createdAt; }

    public void setDate(Date createdAt) { this.createdAt = createdAt; }
}
