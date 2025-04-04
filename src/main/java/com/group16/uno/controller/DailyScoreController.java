package com.group16.uno.controller;

import com.group16.uno.dto.LeaderBoardDTO;
import com.group16.uno.model.DailyScore;
import com.group16.uno.service.DailyScoreService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.Parameter;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RestController
@Tag(name = "DailyScore Controller")
public class DailyScoreController {

    @Autowired
    private DailyScoreService dailyScoreService;

    @Operation(summary = "Update the daily score")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Daily score successfully updated"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "500", description = "Error updating daily score: 'error message'")
    })
    @PutMapping("/daily-score")
    public ResponseEntity<String> updateDailyScore(
            @Parameter(description = "UserId of the user", required = true) @RequestParam String userId,
            @Parameter(description = "Score user got from the game", required = true) @RequestParam BigDecimal score) {
        try {
            Date today = new Date(System.currentTimeMillis());
            Optional<DailyScore> dailyScore = dailyScoreService.getDailyScoreByUserIdAndCreatedAt(userId, today);

            if (dailyScore.isEmpty()) {
                dailyScoreService.createDailyScore(userId, today, score);
            } else {
                dailyScoreService.updateDailyScore(userId, today, score);
            }

            return ResponseEntity.ok("Daily score successfully updated");
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating daily score: " + e.getMessage());
        }
    }

    @Operation(summary = "Get weekly leaderboard")
    @ApiResponses( value = {
            @ApiResponse(responseCode = "200", description = "Weekly leaderboard retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User scores for the last week not found")
    })
    @GetMapping("/weekly-leaderboard")
    public ResponseEntity<List<LeaderBoardDTO>> getWeeklyLeaderboard() {
        List<LeaderBoardDTO> leaderboard = dailyScoreService.getWeeklyLeaderBoard();

        if (!leaderboard.isEmpty()) {
            return ResponseEntity.ok(leaderboard);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyList());
        }
    }

    @Operation(summary = "Get monthly leaderboard")
    @ApiResponses( value = {
            @ApiResponse(responseCode = "200", description = "Monthly leaderboard retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User scores for the last month not found")
    })
    @GetMapping("/monthly-leaderboard")
    public ResponseEntity<List<LeaderBoardDTO>> getMonthlyLeaderboard() {
        List<LeaderBoardDTO> leaderboard = dailyScoreService.getMonthlyLeaderBoard();

        if (!leaderboard.isEmpty()) {
            return ResponseEntity.ok(leaderboard);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyList());
        }
    }


    @Operation(summary = "Get all time leaderboard")
    @ApiResponses( value = {
            @ApiResponse(responseCode = "200", description = "All time leaderboard retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User scores not found")
    })
    @GetMapping("/all-time-leaderboard")
    public ResponseEntity<List<LeaderBoardDTO>> getAllTimeLeaderboard() {
        List<LeaderBoardDTO> leaderboard = dailyScoreService.getAllTimeLeaderBoard();

        if (!leaderboard.isEmpty()) {
            return ResponseEntity.ok(leaderboard);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Collections.emptyList());
        }
    }

}
