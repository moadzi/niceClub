package com.ogcnice.football.repository;

import com.ogcnice.football.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for {@link Player} entities.
 */
@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {
}
