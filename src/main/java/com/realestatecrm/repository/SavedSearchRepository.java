package com.realestatecrm.repository;

import com.realestatecrm.entity.SavedSearch;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavedSearchRepository extends JpaRepository<SavedSearch, Long> {

    // LAZY FIX: Optimized query with user eager loading to avoid LazyInitializationException
    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT s FROM SavedSearch s WHERE s.id = :id")
    Optional<SavedSearch> findByIdWithUser(@Param("id") Long id);

    // LAZY FIX: Optimized findAll with user
    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT s FROM SavedSearch s")
    List<SavedSearch> findAllWithUser();

    // LAZY FIX: Find all searches for a specific user
    @EntityGraph(attributePaths = {"user"})
    @Query("SELECT s FROM SavedSearch s WHERE s.user.id = :userId ORDER BY s.updatedDate DESC")
    List<SavedSearch> findByUserIdWithUser(@Param("userId") Long userId);

    // Check if a saved search exists for a user
    boolean existsByIdAndUserId(Long id, Long userId);

    // Count searches for a user
    long countByUserId(Long userId);
}
