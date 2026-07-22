package com.ogcnice.football.mapper;

import com.ogcnice.football.dto.request.TeamRequest;
import com.ogcnice.football.dto.response.TeamResponse;
import com.ogcnice.football.entity.Team;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper converting between Team entities and their DTOs.
 */

@Mapper(componentModel = "spring", uses = PlayerMapper.class)

public interface TeamMapper {
    TeamResponse toResponse(Team team);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "players", ignore = true)
    Team toEntity(TeamRequest request);
}
