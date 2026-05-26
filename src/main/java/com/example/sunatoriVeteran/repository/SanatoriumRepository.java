package com.example.sunatoriVeteran.repository;

import com.example.sunatoriVeteran.model.Sanatorium;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SanatoriumRepository extends JpaRepository<Sanatorium, Long> {

    @Query("SELECT DISTINCT s FROM Sanatorium s LEFT JOIN s.specializations spec WHERE " +
           "(:region IS NULL OR :region = '' OR s.region = :region) AND " +
           "(:profile IS NULL OR :profile = '' OR LOWER(s.medicalProfile) LIKE LOWER(CONCAT('%', :profile, '%'))) AND " +
           "(:specialization IS NULL OR :specialization = '' OR CAST(spec AS string) = :specialization) AND " +
           "(:search IS NULL OR :search = '' OR LOWER(s.name) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:minPrice IS NULL OR s.standardPackagePrice >= :minPrice) AND " +
           "(:maxPrice IS NULL OR s.standardPackagePrice <= :maxPrice)")
    Page<Sanatorium> findByFilterPaged(
            @Param("region") String region,
            @Param("profile") String profile,
            @Param("specialization") String specialization,
            @Param("search") String search,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            Pageable pageable);

    @Query("SELECT DISTINCT s FROM Sanatorium s LEFT JOIN s.specializations spec WHERE " +
           "(:region IS NULL OR :region = '' OR s.region = :region) AND " +
           "(:profile IS NULL OR :profile = '' OR LOWER(s.medicalProfile) LIKE LOWER(CONCAT('%', :profile, '%'))) AND " +
           "(:specialization IS NULL OR :specialization = '' OR CAST(spec AS string) = :specialization)")
    List<Sanatorium> findByFilter(@Param("region") String region, @Param("profile") String profile, @Param("specialization") String specialization);

    @Query("SELECT s FROM Sanatorium s LEFT JOIN Review r ON r.sanatorium = s " +
           "GROUP BY s ORDER BY COALESCE(AVG(r.rating), 0) DESC")
    List<Sanatorium> findTopRated();

    @Query("SELECT DISTINCT s.region FROM Sanatorium s WHERE s.region IS NOT NULL ORDER BY s.region")
    List<String> findAllRegions();
}
