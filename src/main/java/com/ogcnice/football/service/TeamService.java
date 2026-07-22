package com.ogcnice.football.service;

import com.ogcnice.football.dto.request.TeamRequest;
import com.ogcnice.football.dto.response.TeamResponse;
import com.ogcnice.football.entity.Player;
import com.ogcnice.football.entity.Team;
import com.ogcnice.football.exception.ResourceNotFoundException;
import com.ogcnice.football.mapper.TeamMapper;
import com.ogcnice.football.repository.PlayerRepository;
import com.ogcnice.football.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Business logic for managing {@link Team} resources, including their
 * relationship to the players who make up the squad.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;
    private final TeamMapper teamMapper;

    /**
     * Retrieves a paginated, sorted list of teams, each including its full list of players.
     *
     * @param page    zero-based page index
     * @param size    page size
     * @param sortBy  field to sort by (name, acronym, budget)
     * @param sortDir sort direction ("asc" or "desc")
     * @return a page of {@link TeamResponse}
     */
    @Transactional(readOnly = true)
    public Page<TeamResponse> getTeams(int page, int size, String sortBy, String sortDir) {
        log.info("Fetching teams - page: {}, size: {}, sortBy: {}, sortDir: {}", page, size, sortBy, sortDir);
        Sort sort = "desc".equalsIgnoreCase(sortDir) ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return teamRepository.findAll(pageable).map(teamMapper::toResponse);
    }

    /**
     * Retrieves a single team by its id.
     *
     * @param id the team id
     * @return the matching {@link TeamResponse}
     * @throws ResourceNotFoundException if no team exists with the given id
     */
    @Transactional(readOnly = true)
    public TeamResponse getTeamById(Long id) {
        log.info("Fetching team with id: {}", id);
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id));
        return teamMapper.toResponse(team);
    }

    /**
     * Creates a new team, optionally assigning existing players to it.
     *
     * @param request the team creation payload
     * @return the created {@link TeamResponse}
     * @throws ResourceNotFoundException if any of the provided player ids does not exist
     */
    @Transactional
    public TeamResponse createTeam(TeamRequest request) {
        log.info("Creating team: {}", request.getName());
        Team team = teamMapper.toEntity(request);
        team.setPlayers(new ArrayList<>());
        assignPlayers(team, request.getPlayerIds());
        Team saved = teamRepository.save(team);
        log.info("Team created with id: {}", saved.getId());
        return teamMapper.toResponse(saved);
    }

    /**
     * Updates an existing team's fields and player roster.
     *
     * @param id      the team id
     * @param request the update payload
     * @return the updated {@link TeamResponse}
     * @throws ResourceNotFoundException if the team or any provided player id does not exist
     */
    @Transactional
    public TeamResponse updateTeam(Long id, TeamRequest request) {
        log.info("Updating team with id: {}", id);
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id));

        team.setName(request.getName());
        team.setAcronym(request.getAcronym());
        team.setBudget(request.getBudget());

        if (team.getPlayers() == null) {
            team.setPlayers(new ArrayList<>());
        }
        team.getPlayers().forEach(player -> player.setTeam(null));
        team.getPlayers().clear();
        assignPlayers(team, request.getPlayerIds());

        Team updated = teamRepository.save(team);
        log.info("Team updated with id: {}", updated.getId());
        return teamMapper.toResponse(updated);
    }

    /**
     * Deletes a team by its id.
     *
     * @param id the team id
     */
    @Transactional
    public void deleteTeam(Long id) {
        log.info("Deleting team with id: {}", id);
        teamRepository.deleteById(id);
        log.info("Team deleted with id: {}", id);
    }

    /**
     * Resolves the given player ids and attaches the matching players to the team.
     * Does nothing when no ids are provided.
     *
     * @param team      the team to assign players to
     * @param playerIds the ids of the players to assign, may be null or empty
     * @throws ResourceNotFoundException if any id does not match an existing player
     */
    private void assignPlayers(Team team, List<Long> playerIds) {
        if (playerIds == null || playerIds.isEmpty()) {
            return;
        }
        List<Player> players = playerRepository.findAllById(playerIds);
        if (players.size() != playerIds.size()) {
            List<Long> foundIds = players.stream().map(Player::getId).toList();
            Long missingId = playerIds.stream()
                    .filter(requestedId -> !foundIds.contains(requestedId))
                    .findFirst()
                    .orElse(null);
            throw new ResourceNotFoundException(missingId);
        }
        players.forEach(player -> player.setTeam(team));
        team.getPlayers().addAll(players);
    }
}
