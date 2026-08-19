package com.example.demo.dto.movie;

import com.example.demo.dto.base.Identifiable;
import com.example.demo.dto.base.Titled;

public record MovieForUser<Movie extends Identifiable & Titled>(
        Movie movie,
        MovieUserStatus status
) {
}