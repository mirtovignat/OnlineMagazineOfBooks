package com.example.demo.sorter;

import com.example.demo.dto.joined_to_user.OwnableDTO;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Sorter {

    public static <DTO extends OwnableDTO> List<DTO> sortByReleaseDateConsideringOwn(List<DTO> reviews) {
        return reviews.stream()
                .sorted(Comparator.comparing(DTO::own, Comparator.reverseOrder())
                        .thenComparing(DTO::addedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }

}
