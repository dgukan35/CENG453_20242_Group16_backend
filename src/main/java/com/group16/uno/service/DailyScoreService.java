package com.group16.uno.service;

import com.group16.uno.model.DailyScore;
import com.group16.uno.model.User;
import com.group16.uno.repository.DailyScoreRepository;
import com.group16.uno.dto.LeaderBoardDTO;

import com.group16.uno.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.List;
import java.sql.Date;

@Service
public class DailyScoreService {

    @Autowired
    private DailyScoreRepository dailyScoreRepository;

    @Autowired
    private UserRepository userRepository;

    public DailyScore createDailyScore(String userId, Date createdAt, BigDecimal score) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        DailyScore dailyScore = new DailyScore(user, createdAt, score);

        return dailyScoreRepository.save(dailyScore);
    }

    public Optional<DailyScore> getDailyScoreByUserIdAndCreatedAt(String userId, Date createdAt) {
        return dailyScoreRepository.findByUserIdAndCreatedAt(userId, createdAt);
    }

    public void updateDailyScore(String userId, Date createdAt, BigDecimal value) {
        dailyScoreRepository.updateDailyScore(userId, createdAt, value);
    }

    public List<LeaderBoardDTO> getWeeklyLeaderBoard() {
        return dailyScoreRepository.getWeeklyLeaderBoard();
    }

    public List<LeaderBoardDTO> getMonthlyLeaderBoard() {
        return dailyScoreRepository.getMonthlyLeaderBoard();
    }

    public List<LeaderBoardDTO> getAllTimeLeaderBoard() {
        return dailyScoreRepository.getAllTimeLeaderBoard();
    }

    public long geDailyScoreCount() {
        return dailyScoreRepository.count();
    }

}
