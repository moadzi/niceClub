package com.ogcnice.football.controller;

import com.ogcnice.football.dto.request.TeamRequest;
import com.ogcnice.football.dto.response.TeamResponse;
import com.ogcnice.football.service.TeamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoints for managing {@link com.ogcnice.football.entity.Team} resources.
 */
@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
@Slf4j
public class TeamController {

    private final TeamService teamService;

    /**
     * Lists teams page by page, sorted by the given field. This is one of the two
     * endpoints required by the exercise.
     *
     * @param page    zero-based page index (default 0)
     * @param size    page size (default 10)
     * @param sortBy  field to sort by: name, acronym or budget (default name)
     * @param sortDir sort direction: asc or desc (default asc)
     * @return a 200 response with a page of {@link TeamResponse}, each including its players
     */
    @GetMapping
    public ResponseEntity<Page<TeamResponse>> getTeams(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        log.info("GET /api/teams - page: {}, size: {}, sortBy: {}, sortDir: {}", page, size, sortBy, sortDir);
        return ResponseEntity.ok(teamService.getTeams(page, size, sortBy, sortDir));
    }

    /**
     * Retrieves a single team by its id, including its players.
     *
     * @param id the team id
     * @return a 200 response with the matching {@link TeamResponse}
     */
    @GetMapping("/{id}")
    public ResponseEntity<TeamResponse> getTeamById(@PathVariable Long id) {
        log.info("GET /api/teams/{}", id);
        return ResponseEntity.ok(teamService.getTeamById(id));
    }

    /**
     * Creates a new team, with or without associated players. This is one of the two
     * endpoints required by the exercise. Players are referenced by id (see
     * {@link TeamRequest#getPlayerIds()}), not created inline.
     *
     * @param request the validated team creation payload
     * @return a 201 response with the created {@link TeamResponse}
     */
    @PostMapping
    public ResponseEntity<TeamResponse> createTeam(@Valid @RequestBody TeamRequest request) {
        log.info("POST /api/teams - name: {}", request.getName());
        TeamResponse response = teamService.createTeam(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Updates an existing team, including its fields and its player roster.
     *
     * @param id      the team id
     * @param request the validated update payload
     * @return a 200 response with the updated {@link TeamResponse}
     */
    @PutMapping("/{id}")
    public ResponseEntity<TeamResponse> updateTeam(@PathVariable Long id, @Valid @RequestBody TeamRequest request) {
        log.info("PUT /api/teams/{}", id);
        return ResponseEntity.ok(teamService.updateTeam(id, request));
    }

    /**
     * Deletes a team by its id.
     *
     * @param id the team id
     * @return a 204 response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTeam(@PathVariable Long id) {
        log.info("DELETE /api/teams/{}", id);
        teamService.deleteTeam(id);
        return ResponseEntity.noContent().build();
    }
}
