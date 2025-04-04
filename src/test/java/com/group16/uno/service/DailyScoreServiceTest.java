package com.group16.uno.service;

import com.group16.uno.model.DailyScore;
import com.group16.uno.model.User;
import com.group16.uno.repository.DailyScoreRepository;
import com.group16.uno.repository.UserRepository;
import com.group16.uno.dto.LeaderBoardDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.Optional;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class DailyScoreServiceTest {

    @Mock
    private DailyScoreRepository dailyScoreRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private DailyScoreService dailyScoreService;

    private User user;
    private DailyScore dailyScore;
    private Date createdAt;

    @BeforeEach
    void setup() {
        // Initialize test data
        user = new User( "johndoe", "john@example.com", "hashedPassword");
        user.setId("userId");
        createdAt = Date.valueOf("2025-04-04");
        dailyScore = new DailyScore(user, createdAt, BigDecimal.valueOf(100));
    }

    @Test
    void createDailyScore_shouldSaveDailyScore() {
        // Arrange
        when(userRepository.findById("userId")).thenReturn(Optional.of(user));
        when(dailyScoreRepository.save(any(DailyScore.class))).thenReturn(dailyScore);

        // Act
        DailyScore result = dailyScoreService.createDailyScore("userId", createdAt, BigDecimal.valueOf(100));

        // Assert
        assertNotNull(result);
        assertEquals("userId", result.getUser().getId());
        assertEquals(100, result.getScore());
        verify(userRepository).findById("userId");
        verify(dailyScoreRepository).save(any(DailyScore.class));
    }

    @Test
    void createDailyScore_userNotFound_shouldThrowException() {
        // Arrange
        when(userRepository.findById("invalidUserId")).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            dailyScoreService.createDailyScore("invalidUserId", createdAt, BigDecimal.valueOf(100));
        });
        assertEquals("User not found with id: invalidUserId", exception.getMessage());
        verify(userRepository).findById("invalidUserId");
    }

    @Test
    void getDailyScoreByUserIdAndCreatedAt_shouldReturnDailyScore() {
        // Arrange
        when(dailyScoreRepository.findByUserIdAndCreatedAt("userId", createdAt))
                .thenReturn(Optional.of(dailyScore));

        // Act
        Optional<DailyScore> result = dailyScoreService.getDailyScoreByUserIdAndCreatedAt("userId", createdAt);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(100, result.get().getScore());
        verify(dailyScoreRepository).findByUserIdAndCreatedAt("userId", createdAt);
    }

    @Test
    void updateDailyScore_shouldUpdateScore() {
        // Arrange
        doNothing().when(dailyScoreRepository).updateDailyScore("userId", createdAt, BigDecimal.valueOf(200));

        // Act
        dailyScoreService.updateDailyScore("userId", createdAt, BigDecimal.valueOf(200));

        // Assert
        verify(dailyScoreRepository).updateDailyScore("userId", createdAt, BigDecimal.valueOf(200));
    }

    @Test
    void getWeeklyLeaderBoard_shouldReturnLeaderBoard() {
        // Arrange
        List<LeaderBoardDTO> leaderBoard = List.of(new LeaderBoardDTO("userId", BigDecimal.valueOf(100)));
        when(dailyScoreRepository.getWeeklyLeaderBoard()).thenReturn(leaderBoard);

        // Act
        List<LeaderBoardDTO> result = dailyScoreService.getWeeklyLeaderBoard();

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(100, result.get(0).getTotalScore());
        verify(dailyScoreRepository).getWeeklyLeaderBoard();
    }

    @Test
    void getMonthlyLeaderBoard_shouldReturnLeaderBoard() {
        // Arrange
        List<LeaderBoardDTO> leaderBoard = List.of(new LeaderBoardDTO("userId", BigDecimal.valueOf(100)));
        when(dailyScoreRepository.getMonthlyLeaderBoard()).thenReturn(leaderBoard);

        // Act
        List<LeaderBoardDTO> result = dailyScoreService.getMonthlyLeaderBoard();

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(100, result.get(0).getTotalScore());
        verify(dailyScoreRepository).getMonthlyLeaderBoard();
    }

    @Test
    void getAllTimeLeaderBoard_shouldReturnLeaderBoard() {
        // Arrange
        List<LeaderBoardDTO> leaderBoard = List.of(new LeaderBoardDTO("userId", BigDecimal.valueOf(100)));
        when(dailyScoreRepository.getAllTimeLeaderBoard()).thenReturn(leaderBoard);

        // Act
        List<LeaderBoardDTO> result = dailyScoreService.getAllTimeLeaderBoard();

        // Assert
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(100, result.get(0).getTotalScore());
        verify(dailyScoreRepository).getAllTimeLeaderBoard();
    }
}

