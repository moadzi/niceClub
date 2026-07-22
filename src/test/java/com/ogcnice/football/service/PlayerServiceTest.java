package com.ogcnice.football.service;

import com.ogcnice.football.dto.request.PlayerRequest;
import com.ogcnice.football.dto.response.PlayerResponse;
import com.ogcnice.football.entity.Player;
import com.ogcnice.football.entity.Team;
import com.ogcnice.football.enums.Position;
import com.ogcnice.football.exception.ResourceNotFoundException;
import com.ogcnice.football.mapper.PlayerMapper;
import com.ogcnice.football.repository.PlayerRepository;
import com.ogcnice.football.repository.TeamRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Pure unit tests for {@link PlayerService}. Repositories and the mapper are
 * mocked, so the test only for the service logic (team assignment,
 * transfers and not-found handling).
 */
@ExtendWith(MockitoExtension.class)
class PlayerServiceTest {

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private PlayerMapper playerMapper;

    @InjectMocks
    private PlayerService playerService;

    @Test
    void createPlayer_withTeamId_shouldAssignTeam() {
        PlayerRequest request = PlayerRequest.builder().name("Moad ZIZI")
                .position(Position.FORWARD).teamId(1L).build();
        Team team = Team.builder().id(1L).name("OGC Nice").acronym("OGCN")
                .budget(BigDecimal.valueOf(50_000_000)).build();
        Player player = Player.builder().name("Moad ZIZI").position(Position.FORWARD).build();

        when(playerMapper.toEntity(request)).thenReturn(player);
        when(teamRepository.findById(1L)).thenReturn(Optional.of(team));
        when(playerRepository.save(any(Player.class))).thenAnswer(inv -> inv.getArgument(0));
        when(playerMapper.toResponse(any(Player.class))).thenReturn(PlayerResponse.builder().build());

        playerService.createPlayer(request);

        assertThat(player.getTeam()).isSameAs(team);
        verify(playerRepository).save(player);
    }

    @Test
    void createPlayer_withoutTeamId_shouldCreateFreeAgent() {
        PlayerRequest request = PlayerRequest.builder().name("Free Agent")
                .position(Position.MIDFIELDER).build();
        Player player = Player.builder().name("Free Agent").position(Position.MIDFIELDER).build();

        when(playerMapper.toEntity(request)).thenReturn(player);
        when(playerRepository.save(any(Player.class))).thenAnswer(inv -> inv.getArgument(0));
        when(playerMapper.toResponse(any(Player.class))).thenReturn(PlayerResponse.builder().build());

        playerService.createPlayer(request);

        assertThat(player.getTeam()).isNull();
        verify(teamRepository, never()).findById(any());
    }

    @Test
    void updatePlayer_withNewTeamId_shouldTransferPlayer() {
        Player existing = Player.builder().id(1L).name("Moad ZIZI").position(Position.FORWARD).build();
        Team newTeam = Team.builder().id(2L).name("AS Monaco").acronym("ASM")
                .budget(BigDecimal.valueOf(40_000_000)).build();
        PlayerRequest request = PlayerRequest.builder().name("Moad ZIZI")
                .position(Position.FORWARD).teamId(2L).build();

        when(playerRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(teamRepository.findById(2L)).thenReturn(Optional.of(newTeam));
        when(playerRepository.save(any(Player.class))).thenAnswer(inv -> inv.getArgument(0));
        when(playerMapper.toResponse(any(Player.class))).thenReturn(PlayerResponse.builder().build());

        playerService.updatePlayer(1L, request);

        assertThat(existing.getTeam()).isSameAs(newTeam);
    }

    @Test
    void getPlayer_notFound_shouldThrowResourceNotFoundException() {
        when(playerRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> playerService.getPlayerById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }
}
