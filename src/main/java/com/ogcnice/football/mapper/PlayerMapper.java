package com.ogcnice.football.mapper;

import com.ogcnice.football.dto.request.PlayerRequest;
import com.ogcnice.football.dto.response.PlayerResponse;
import com.ogcnice.football.entity.Player;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * MapStruct mapper converting between {@link Player} entities and their DTOs.
 * The {@code team} association is deliberately excluded from the generated
 * mappings to avoid circular references; it is assigned explicitly in the
 * service layer.
 */
@Mapper(componentModel = "spring")
public interface PlayerMapper {

    PlayerResponse toResponse(Player player);

    List<PlayerResponse> toResponseList(List<Player> players);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "team", ignore = true)
    Player toEntity(PlayerRequest request);
}
