package com.group16.uno;

import com.group16.uno.model.DailyScore;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import com.group16.uno.service.UserService;
import com.group16.uno.service.DailyScoreService;
import com.group16.uno.model.User;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserService userService;
    private final DailyScoreService dailyScoreService;
    long millisInADay = 24L * 60 * 60 * 1000;

    public DataSeeder(UserService userService, DailyScoreService dailyScoreService) {
        this.userService = userService;
        this.dailyScoreService = dailyScoreService;
    }

    @Override
    public void run(String... args) throws RuntimeException {
        if (userService.getUserCount() == 0) {
            for (int i = 0; i < 50; i++) {
                userService.createUser(
                        "User " + i,
                        "user" + i + "@example.com",
                        "password" + i
                );
            }

            System.out.println("Seeded 50 users into the database.");
        }

        if (dailyScoreService.geDailyScoreCount() == 0) {
            for (int i = 0; i < 10000; i++) {
                int random1 = ThreadLocalRandom.current().nextInt(0, 50);
                int random2 = ThreadLocalRandom.current().nextInt(0, 100);
                BigDecimal random3 = BigDecimal.valueOf(ThreadLocalRandom.current().nextInt(-100, 100));

                Date date = new Date(System.currentTimeMillis() - millisInADay*random2);
                User user = userService.getUserByUsername("User " + random1).
                        orElseThrow(() -> new RuntimeException("User not found"));

                Optional<DailyScore> dailyScore = dailyScoreService
                        .getDailyScoreByUserIdAndCreatedAt(user.getId(), date);

                if (dailyScore.isPresent()) {
                    dailyScoreService.updateDailyScore(user.getId(), date, random3);
                }
                else{
                    dailyScoreService.createDailyScore(user.getId(), date, random3);
                }
            }

            System.out.println("Seeded 10000 daily scores into the database.");
        }
    }
}
