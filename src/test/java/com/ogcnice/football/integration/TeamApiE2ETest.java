package com.ogcnice.football.integration;

import com.ogcnice.football.dto.request.PlayerRequest;
import com.ogcnice.football.dto.request.TeamRequest;
import com.ogcnice.football.enums.Position;
import com.ogcnice.football.repository.PlayerRepository;
import com.ogcnice.football.repository.TeamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * end-to-end integration test of the two endpoints required
 * It boots the whole Spring context against an in-memory H2 database.
 *
 * 
 *   POST /api/teams — create a team with associated players.</li>
 *   GET  /api/teams — paginated, server-side sorted list of teams with players.</li>
 * 
 */
@SpringBootTest
@ActiveProfiles("test")
class TeamApiE2ETest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        // Keep the test independent of any leftover rows (the H2 schema is shared).
        playerRepository.deleteAll();
        teamRepository.deleteAll();
    }

    @Test
    void createTeamWithPlayers_thenListTeamsSortedByBudget() throws Exception {
        // A team references players that already exist, so create them first.
        Long MZId = createPlayer("Moad ZIZI", Position.FORWARD);
        Long VVId = createPlayer("Victor Valdes", Position.GOALKEEPER);

        // Endpoint #2: create a team WITH associated players.
        TeamRequest niceRequest = TeamRequest.builder()
                .name("OGC Nice")
                .acronym("OGCN")
                .budget(BigDecimal.valueOf(50_000_000))
                .playerIds(List.of(MZId, VVId))
                .build();

        mockMvc.perform(post("/api/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(niceRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.name").value("OGC Nice"))
                .andExpect(jsonPath("$.players", hasSize(2)));

        // A second team with a smaller budget, no players.
        TeamRequest monacoRequest = TeamRequest.builder()
                .name("AS Monaco")
                .acronym("ASM")
                .budget(BigDecimal.valueOf(40_000_000))
                .build();

        mockMvc.perform(post("/api/teams")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(monacoRequest)))
                .andExpect(status().isCreated());

        // Endpoint #1: paginated list, sorted by budget desc, each team with its players.
        mockMvc.perform(get("/api/teams")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "budget")
                        .param("sortDir", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].name").value("OGC Nice"))     // 50M first
                .andExpect(jsonPath("$.content[0].players", hasSize(2)))
                .andExpect(jsonPath("$.content[1].name").value("AS Monaco"))    // 40M second
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    private Long createPlayer(String name, Position position) throws Exception {
        PlayerRequest request = PlayerRequest.builder().name(name).position(position).build();
        String body = mockMvc.perform(post("/api/players")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("id").asLong();
    }
}
