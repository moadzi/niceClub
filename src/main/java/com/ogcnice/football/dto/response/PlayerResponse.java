package com.ogcnice.football.dto.response;

import com.ogcnice.football.enums.Position;
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
public class PlayerResponse {

    private Long id;
    private String name;
    private Position position;
}
