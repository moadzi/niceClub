package com.ogcnice.football.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotNull
    @Size(max = 10, message = "Acronym must not exceed 10 characters")
    private String acronym;

    @NotNull
    @Positive(message = "Budget must be positive")
    private BigDecimal budget;


    private List<Long> playerIds;
}
