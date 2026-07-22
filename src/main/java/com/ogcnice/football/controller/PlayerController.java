package com.ogcnice.football.controller;

import com.ogcnice.football.dto.request.PlayerRequest;
import com.ogcnice.football.dto.response.PlayerResponse;
import com.ogcnice.football.service.PlayerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST endpoints for managing {@link com.ogcnice.football.entity.Player} resources.
 * These endpoints are a bonus on top of the two team endpoints required by the
 * exercise; they let you create the players that a team can later reference by id.
 */
@RestController
@RequestMapping("/api/players")
@RequiredArgsConstructor
@Slf4j
public class PlayerController {

    private final PlayerService playerService;

    /**
     * Lists every player.
     *
     * @return a 200 response with the list of all {@link PlayerResponse}
     */
    @GetMapping
    public ResponseEntity<List<PlayerResponse>> getPlayers() {
        log.info("GET /api/players");
        return ResponseEntity.ok(playerService.getPlayers());
    }

    /**
     * Retrieves a single player by its id.
     *
     * @param id the player id
     * @return a 200 response with the matching {@link PlayerResponse}
     */
    @GetMapping("/{id}")
    public ResponseEntity<PlayerResponse> getPlayerById(@PathVariable Long id) {
        log.info("GET /api/players/{}", id);
        return ResponseEntity.ok(playerService.getPlayerById(id));
    }

    /**
     * Creates a new player, optionally attaching it to an existing team by id.
     *
     * @param request the validated player creation payload
     * @return a 201 response with the created {@link PlayerResponse}
     */
    @PostMapping
    public ResponseEntity<PlayerResponse> createPlayer(@Valid @RequestBody PlayerRequest request) {
        log.info("POST /api/players - name: {}", request.getName());
        PlayerResponse response = playerService.createPlayer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Updates an existing player, optionally transferring it to another team
     * or releasing it to free agency when {@code teamId} is null.
     *
     * @param id      the player id
     * @param request the validated update payload
     * @return a 200 response with the updated {@link PlayerResponse}
     */
    @PutMapping("/{id}")
    public ResponseEntity<PlayerResponse> updatePlayer(
            @PathVariable Long id,
            @Valid @RequestBody PlayerRequest request) {
        log.info("PUT /api/players/{}", id);
        return ResponseEntity.ok(playerService.updatePlayer(id, request));
    }

    /**
     * Deletes a player by its id.
     *
     * @param id the player id
     * @return a 204 response
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePlayer(@PathVariable Long id) {
        log.info("DELETE /api/players/{}", id);
        playerService.deletePlayer(id);
        return ResponseEntity.noContent().build();
    }
}
