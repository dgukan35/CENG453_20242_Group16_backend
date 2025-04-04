package com.group16.uno.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class LeaderBoardDTO {
    private String username;
    private BigDecimal totalScore;


    public LeaderBoardDTO(String username, BigDecimal totalScore) {
        this.username = username;
        this.totalScore = totalScore;
    }


}
