package com.group16.uno.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.sql.Date;

@Entity
@Table(name = "daily_score", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "created_at"}))
public class DailyScore {
    @Setter
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Setter
    @Getter
    @ManyToOne
    @JoinColumn(name="user_id")
    private User user;

    @Column(nullable = false)
    private Date createdAt;

    @Setter
    @Getter
    @Column(nullable = false)
    private Integer score;

    public DailyScore() {}

    public DailyScore(User user, Date createdAt, Integer score) {
        this.user = user;
        this.createdAt = createdAt;
        this.score = score;
    }

    public Date geteDate() { return createdAt; }

    public void setDate(Date createdAt) { this.createdAt = createdAt; }
}
