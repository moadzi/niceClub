package com.ogcnice.football.service;

import com.ogcnice.football.dto.request.TeamRequest;
import com.ogcnice.football.dto.response.TeamResponse;
import com.ogcnice.football.entity.Player;
import com.ogcnice.football.entity.Team;
import com.ogcnice.football.enums.Position;
import com.ogcnice.football.exception.ResourceNotFoundException;
import com.ogcnice.football.mapper.TeamMapper;
import com.ogcnice.football.repository.PlayerRepository;
import com.ogcnice.football.repository.TeamRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pure unit tests for {@link TeamService}. Every collaborator (repositories and
 * mapper) is mocked, so no Spring context or database is involved and the tests
 * focus purely on the service's own logic.
 */
@ExtendWith(MockitoExtension.class)
class TeamServiceTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private TeamMapper teamMapper;

    @InjectMocks
    private TeamService teamService;

    @Test
    void getTeams_shouldReturnPagedResultSortedByRequestedField() {
        Team team = Team.builder().id(1L).name("OGC Nice").acronym("OGCN")
                .budget(BigDecimal.valueOf(50_000_000)).players(new ArrayList<>()).build();
        when(teamRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(team)));
        when(teamMapper.toResponse(team)).thenReturn(TeamResponse.builder().id(1L).name("OGC Nice").build());

        Page<TeamResponse> result = teamService.getTeams(0, 10, "budget", "desc");

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("OGC Nice");

        // The requested server-side sort must be forwarded to the repository.
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(teamRepository).findAll(pageableCaptor.capture());
        Pageable used = pageableCaptor.getValue();
        assertThat(used.getSort().getOrderFor("budget")).isNotNull();
        assertThat(used.getSort().getOrderFor("budget").isDescending()).isTrue();
    }

    @Test
    void createTeam_withPlayerIds_shouldAssignPlayersToTeam() {
        TeamRequest request = TeamRequest.builder().name("OGC Nice").acronym("OGCN")
                .budget(BigDecimal.valueOf(50_000_000)).playerIds(List.of(1L)).build();
        Player player = Player.builder().id(1L).name("Terem Moffi").position(Position.FORWARD).build();

        when(teamMapper.toEntity(request)).thenReturn(newEmptyTeam());
        when(playerRepository.findAllById(List.of(1L))).thenReturn(List.of(player));
        when(teamRepository.save(any(Team.class))).thenAnswer(inv -> inv.getArgument(0));
        when(teamMapper.toResponse(any(Team.class))).thenReturn(TeamResponse.builder().build());

        teamService.createTeam(request);

        // Both sides of the relationship must be wired up before saving.
        ArgumentCaptor<Team> savedTeam = ArgumentCaptor.forClass(Team.class);
        verify(teamRepository).save(savedTeam.capture());
        assertThat(savedTeam.getValue().getPlayers()).containsExactly(player);
        assertThat(player.getTeam()).isSameAs(savedTeam.getValue());
    }

    @Test
    void createTeam_withoutPlayerIds_shouldCreateTeamWithEmptyPlayerList() {
        TeamRequest request = TeamRequest.builder().name("OGC Nice").acronym("OGCN")
                .budget(BigDecimal.valueOf(50_000_000)).build();

        when(teamMapper.toEntity(request)).thenReturn(newEmptyTeam());
        when(teamRepository.save(any(Team.class))).thenAnswer(inv -> inv.getArgument(0));
        when(teamMapper.toResponse(any(Team.class))).thenReturn(TeamResponse.builder().build());

        teamService.createTeam(request);

        ArgumentCaptor<Team> savedTeam = ArgumentCaptor.forClass(Team.class);
        verify(teamRepository).save(savedTeam.capture());
        assertThat(savedTeam.getValue().getPlayers()).isEmpty();
        verify(playerRepository, never()).findAllById(any());
    }

    @Test
    void createTeam_withInvalidPlayerId_shouldThrowResourceNotFoundException() {
        TeamRequest request = TeamRequest.builder().name("OGC Nice").acronym("OGCN")
                .budget(BigDecimal.valueOf(50_000_000)).playerIds(List.of(99L)).build();

        when(teamMapper.toEntity(request)).thenReturn(newEmptyTeam());
        when(playerRepository.findAllById(List.of(99L))).thenReturn(List.of()); // none found

        assertThatThrownBy(() -> teamService.createTeam(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
        verify(teamRepository, never()).save(any());
    }

    @Test
    void deleteTeam_shouldCallRepositoryDeleteById() {
        teamService.deleteTeam(1L);

        verify(teamRepository).deleteById(1L);
    }

    private Team newEmptyTeam() {
        return Team.builder().players(new ArrayList<>()).build();
    }
}
