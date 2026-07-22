package com.ogcnice.football.dto.request;

import com.ogcnice.football.enums.Position;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerRequest {

    private Long teamId;

    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Position is required")
    private Position position;

}
