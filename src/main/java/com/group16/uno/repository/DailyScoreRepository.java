package com.group16.uno.repository;

import com.group16.uno.model.DailyScore;
import com.group16.uno.dto.LeaderBoardDTO;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.sql.Date;

@Repository
public interface DailyScoreRepository extends JpaRepository<DailyScore, Long> {

    Optional<DailyScore> findByUserIdAndCreatedAt(String userId, Date createdAt);

    @Modifying
    @Transactional
    @Query("UPDATE DailyScore ds " +
            "SET ds.score = ds.score + :value " +
            "WHERE ds.user.id = :userId and ds.createdAt = :createdAt")
    void updateDailyScore(
            @Param("userId") String userId,
            @Param("createdAt") Date createdAt,
            @Param("increment") Integer value
    );

    @Query(value = "SELECT u.username, SUM(ds.score) as total_score " +
                   "FROM daily_score ds, user u " +
                   "WHERE ds.user_id = u.id and ds.createdAt >= CURDATE() - INTERVAL 7 DAY " +
                   "GROUP BY u.username " +
                   "ORDER BY total_score DESC", nativeQuery = true)
    List<LeaderBoardDTO> getWeeklyLeaderBoard();

    @Query(value = "SELECT u.username, SUM(ds.score) as total_score " +
                   "FROM daily_score ds, user u " +
                   "WHERE ds.user_id = u.id and ds.createdAt >= CURDATE() - INTERVAL 30 DAY " +
                   "GROUP BY u.username " +
                   "ORDER BY total_score DESC", nativeQuery = true)
    List<LeaderBoardDTO> getMonthlyLeaderBoard();

    @Query(value = "SELECT u.username, SUM(ds.score) as total_score " +
                   "FROM daily_score ds, user u " +
                   "WHERE ds.user_id = u.id " +
                   "GROUP BY u.username " +
                   "ORDER BY total_score DESC", nativeQuery = true)
    List<LeaderBoardDTO> getAllTimeLeaderBoard();

}
