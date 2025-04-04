package com.group16.uno.dto;

import java.math.BigDecimal;

public class LeaderBoardDTO {
    private String username;
    private BigDecimal totalScore;


    public LeaderBoardDTO(String username, BigDecimal totalScore) {
        this.username = username;
        this.totalScore = totalScore;
    }


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public BigDecimal getTotalScore() {
        return totalScore;
    }

    public void setTotalScore(BigDecimal totalScore) {
        this.totalScore = totalScore;
    }
}
