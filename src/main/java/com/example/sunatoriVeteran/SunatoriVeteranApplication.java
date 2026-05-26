package com.example.sunatoriVeteran;

import com.example.sunatoriVeteran.model.Sanatorium;
import com.example.sunatoriVeteran.model.SanatoriumSpecialization;
import com.example.sunatoriVeteran.repository.SanatoriumRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableCaching
public class SunatoriVeteranApplication {

	@Bean
	public CacheManager cacheManager() {
		return new ConcurrentMapCacheManager("users");
	}

	public static void main(String[] args) {
		SpringApplication.run(SunatoriVeteranApplication.class, args);
	}

	@Bean
	CommandLineRunner initDatabase(SanatoriumRepository repository, JdbcTemplate jdbcTemplate) {
		return args -> {
			try {
				jdbcTemplate.execute("ALTER TABLE users ADD COLUMN is_banned BOOLEAN DEFAULT FALSE");
				System.out.println("Column is_banned successfully added.");
			} catch (Exception e) {
				System.out.println("Column is_banned skipped (already exists or error): " + e.getMessage());
			}
			try {
				jdbcTemplate.execute("ALTER TABLE bookings ADD COLUMN rejection_reason TEXT");
				System.out.println("Column rejection_reason successfully added.");
			} catch (Exception e) {
				System.out.println("Column rejection_reason skipped (already exists or error): " + e.getMessage());
			}

			try {
				jdbcTemplate.execute("DELETE FROM bookings");
				System.out.println("Старі бронювання видалено для оновлення санаторіїв.");
			} catch (Exception e) {
				System.out.println("Помилка видалення бронювань: " + e.getMessage());
			}

			try {
				jdbcTemplate.execute("DELETE FROM reviews");
				System.out.println("Старі відгуки видалено для оновлення санаторіїв.");
			} catch (Exception e) {
				System.out.println("Помилка видалення відгуків: " + e.getMessage());
			}

		};
	}
}