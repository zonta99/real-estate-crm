package com.realestatecrm.repository;

import com.realestatecrm.exception.EntityNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * Base repository interface providing common utility methods for all repositories.
 * This interface reduces boilerplate by providing default implementations for common operations
 * like finding entities with proper exception handling.
 *
 * @param <T> The entity type
 * @param <ID> The ID type (typically Long)
 */
@NoRepositoryBean
public interface BaseRepository<T, ID> extends JpaRepository<T, ID> {

    /**
     * Find an entity by ID or throw EntityNotFoundException if not found.
     * This eliminates the need for repeated orElseThrow() patterns in service classes.
     *
     * @param id The entity ID
     * @return The entity
     * @throws EntityNotFoundException if entity is not found
     */
    default T findByIdOrThrow(ID id) {
        return findById(id).orElseThrow(() ->
            new EntityNotFoundException(
                getEntityName() + " not found with id: " + id
            )
        );
    }

    /**
     * Delete an entity by ID or throw EntityNotFoundException if not found.
     * This eliminates the need for existsById checks before deletion.
     *
     * @param id The entity ID
     * @throws EntityNotFoundException if entity is not found
     */
    default void deleteByIdOrThrow(ID id) {
        if (!existsById(id)) {
            throw new EntityNotFoundException(
                getEntityName() + " not found with id: " + id
            );
        }
        deleteById(id);
    }

    /**
     * Get the entity name for error messages.
     * Override this method in specific repositories if needed for custom entity names.
     *
     * @return The entity name
     */
    default String getEntityName() {
        // Extract entity name from repository class name
        // e.g., "PropertyRepository" -> "Property"
        String className = getClass().getSimpleName();
        if (className.contains("$")) {
            // Handle proxy classes
            className = className.substring(0, className.indexOf("$"));
        }
        return className.replace("Repository", "");
    }
}
