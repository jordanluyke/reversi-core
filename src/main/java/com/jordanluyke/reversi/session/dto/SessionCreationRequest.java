package com.jordanluyke.reversi.session.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Optional;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SessionCreationRequest {
    private Optional<String> email = Optional.empty();
    private Optional<String> name = Optional.empty();
}
