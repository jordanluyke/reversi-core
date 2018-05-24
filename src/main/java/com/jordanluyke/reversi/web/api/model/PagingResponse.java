package com.jordanluyke.reversi.web.api.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Jordan Luyke <jordanluyke@gmail.com>
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PagingResponse<T> {

    private List<? extends T> data;
    private long position;
    private long total;
}
