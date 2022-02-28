package de.tsearch.statuscollector.service.twitch.entity;

import lombok.Data;

import java.util.List;

@Data
public class Wrapper<T> {
    private List<T> data;
    private Pagination pagination;

    private long total;
    private long maxTotalCost;
    private long totalCost;
}
