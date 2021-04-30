package io.hat.datoid.index.model;

import java.time.LocalDate;

public record Item(
        String link,
        String filename,
        Long sizeBytes,
        LocalDate uploadDate,
        String extension,
        Resolution resolution,
        Integer lengthSeconds,
        Integer fps,
        String codec
) {
    public record Resolution(Integer width, Integer height) {}
}


