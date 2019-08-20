package com.jordanluyke.reversi.session.dto;

import com.jordanluyke.reversi.account.model.PlayerStats;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
@Getter
@Setter
@Builder
public class ProfileResponse {
    private String name;
    private PlayerStats stats;
}
