package com.example.sunatoriVeteran.service;

import com.example.sunatoriVeteran.model.Sanatorium;
import com.example.sunatoriVeteran.repository.ReviewRepository;
import com.example.sunatoriVeteran.repository.SanatoriumRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SanatoriumService {

    @Autowired
    private SanatoriumRepository repository;

    @Autowired
    private ReviewRepository reviewRepository;

    public Page<Sanatorium> getFilteredSanatoriumsPaged(String region, String profile, String specialization,
                                                         String search, Double minPrice, Double maxPrice,
                                                         Pageable pageable) {
        Page<Sanatorium> page = repository.findByFilterPaged(region, profile, specialization, search, minPrice, maxPrice, pageable);
        populateRatings(page.getContent());
        return page;
    }

    public List<Sanatorium> getFilteredSanatoriums(String region, String profile, String specialization) {
        List<Sanatorium> sanatoriums = repository.findByFilter(region, profile, specialization);
        populateRatings(sanatoriums);
        return sanatoriums;
    }

    public List<Sanatorium> getTopRatedSanatoriums(int limit) {
        List<Sanatorium> all = repository.findTopRated();
        populateRatings(all);
        return all.stream().limit(limit).toList();
    }

    public Optional<Sanatorium> getSanatoriumById(Long id) {
        Optional<Sanatorium> opt = repository.findById(id);
        opt.ifPresent(s -> {
            s.setAverageRating(reviewRepository.findAverageRatingBySanatoriumId(s.getId()));
            s.setReviewCount(reviewRepository.countBySanatoriumId(s.getId()));
        });
        return opt;
    }

    public List<String> getDistinctRegions() {
        return repository.findAllRegions();
    }

    public Sanatorium saveSanatorium(Sanatorium sanatorium) {
        return repository.save(sanatorium);
    }

    public Sanatorium updateSanatorium(Long id, Sanatorium sanatoriumDetails) {
        Sanatorium sanatorium = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Санаторій не знайдено"));
        
        sanatorium.setName(sanatoriumDetails.getName());
        sanatorium.setRegion(sanatoriumDetails.getRegion());
        sanatorium.setAddress(sanatoriumDetails.getAddress());
        sanatorium.setMedicalProfile(sanatoriumDetails.getMedicalProfile());
        sanatorium.setDescription(sanatoriumDetails.getDescription());
        sanatorium.setImagePaths(sanatoriumDetails.getImagePaths());
        sanatorium.setSpecializations(sanatoriumDetails.getSpecializations());
        
        return repository.save(sanatorium);
    }

    public void deleteSanatorium(Long id) {
        Sanatorium sanatorium = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Санаторій не знайдено"));
        repository.delete(sanatorium);
    }

    private void populateRatings(List<Sanatorium> sanatoriums) {
        List<Object[]> allRatings = reviewRepository.findAllAverageRatings();
        Map<Long, Double> avgMap = allRatings.stream()
                .collect(Collectors.toMap(
                        r -> (Long) r[0],
                        r -> Math.round((Double) r[1] * 10.0) / 10.0
                ));
        Map<Long, Integer> countMap = allRatings.stream()
                .collect(Collectors.toMap(
                        r -> (Long) r[0],
                        r -> ((Long) r[2]).intValue()
                ));
        for (Sanatorium s : sanatoriums) {
            s.setAverageRating(avgMap.getOrDefault(s.getId(), 0.0));
            s.setReviewCount(countMap.getOrDefault(s.getId(), 0));
        }
    }
}
