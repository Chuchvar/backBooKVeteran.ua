package com.example.sunatoriVeteran.repository;

import com.example.sunatoriVeteran.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findBySanatoriumIdOrderByCreatedAtDesc(Long sanatoriumId);

    @Query("SELECT COALESCE(AVG(r.rating), 0) FROM Review r WHERE r.sanatorium.id = :sanatoriumId")
    Double findAverageRatingBySanatoriumId(@Param("sanatoriumId") Long sanatoriumId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.sanatorium.id = :sanatoriumId")
    Integer countBySanatoriumId(@Param("sanatoriumId") Long sanatoriumId);

    @Query("SELECT r.sanatorium.id, AVG(r.rating), COUNT(r) FROM Review r GROUP BY r.sanatorium.id")
    List<Object[]> findAllAverageRatings();
}
