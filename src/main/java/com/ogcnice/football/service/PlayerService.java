package com.ogcnice.football.service;

import com.ogcnice.football.dto.request.PlayerRequest;
import com.ogcnice.football.dto.response.PlayerResponse;
import com.ogcnice.football.entity.Player;
import com.ogcnice.football.entity.Team;
import com.ogcnice.football.exception.ResourceNotFoundException;
import com.ogcnice.football.mapper.PlayerMapper;
import com.ogcnice.football.repository.PlayerRepository;
import com.ogcnice.football.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Business logic for managing {@link Player} resources, including
 * assigning, transferring and releasing players from teams.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final TeamRepository teamRepository;
    private final PlayerMapper playerMapper;

    /**
     * Retrieves every player.
     *
     * @return the list of all {@link PlayerResponse}
     */
    public List<PlayerResponse> getPlayers() {
        log.info("Fetching all players");
        return playerMapper.toResponseList(playerRepository.findAll());
    }

    /**
     * Retrieves a single player by its id.
     *
     * @param id the player id
     * @return the matching {@link PlayerResponse}
     * @throws ResourceNotFoundException if no player exists with the given id
     */
    public PlayerResponse getPlayerById(Long id) {
        log.info("Fetching player with id: {}", id);
        Player player = playerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id));
        return playerMapper.toResponse(player);
    }

    /**
     * Creates a new player, optionally assigning it to an existing team.
     *
     * @param request the player creation payload
     * @return the created {@link PlayerResponse}
     * @throws ResourceNotFoundException if the provided teamId does not exist
     */
    @Transactional
    public PlayerResponse createPlayer(PlayerRequest request) {
        log.info("Creating player: {}", request.getName());
        Player player = playerMapper.toEntity(request);
        assignTeam(player, request.getTeamId());
        Player saved = playerRepository.save(player);
        log.info("Player created with id: {}", saved.getId());
        return playerMapper.toResponse(saved);
    }

    /**
     * Updates an existing player's fields, optionally transferring it to another
     * team or releasing it to free agency when {@code teamId} is null.
     *
     * @param id      the player id
     * @param request the update payload
     * @return the updated {@link PlayerResponse}
     * @throws ResourceNotFoundException if the player or the provided teamId does not exist
     */
    @Transactional
    public PlayerResponse updatePlayer(Long id, PlayerRequest request) {
        log.info("Updating player with id: {}", id);
        Player player = playerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id));

        player.setName(request.getName());
        player.setPosition(request.getPosition());
        assignTeam(player, request.getTeamId());

        Player updated = playerRepository.save(player);
        log.info("Player updated with id: {}", updated.getId());
        return playerMapper.toResponse(updated);
    }

    /**
     * Deletes a player by its id.
     *
     * @param id the player id
     */
    @Transactional
    public void deletePlayer(Long id) {
        log.info("Deleting player with id: {}", id);
        playerRepository.deleteById(id);
        log.info("Player deleted with id: {}", id);
    }

    /**
     * Assigns the player to the given team, or releases it to free agency
     * when {@code teamId} is null.
     *
     * @param player the player to update
     * @param teamId the id of the team to assign, or null to release the player
     * @throws ResourceNotFoundException if the team id does not match an existing team
     */
    private void assignTeam(Player player, Long teamId) {
        if (teamId == null) {
            player.setTeam(null);
            return;
        }
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException(teamId));
        player.setTeam(team);
    }
}
