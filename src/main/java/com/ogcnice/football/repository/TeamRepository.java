package com.ogcnice.football.repository;

import com.ogcnice.football.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for {@link Team} entities.
 */
@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
}
