package dev.cervantvfr.vacationsplease.api.dto;

import java.util.List;

/**
 * API wrapper for a paginated list. Mirrors Spring Data {@code Page} metadata
 * without exposing framework types.
 */
public record PagedResponse<T>(
    List<T> content,
    int page,
    int size,
    long totalElements,
    int totalPages,
    boolean first,
    boolean last
) {

}