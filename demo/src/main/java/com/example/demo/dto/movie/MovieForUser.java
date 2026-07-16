package com.example.demo.dto.movie;

public record MovieForUser<Movie extends MovieData>(
        Movie movie,
        MovieUserStatus status
) {
}