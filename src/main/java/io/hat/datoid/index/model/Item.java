package io.hat.datoid.index.model;

public record Item (String filename, String link, Thumbnail thumbnail,
                    String suffix, Integer lengthSeconds, Long sizeBytes) {

    public record Thumbnail(String url, String width, String height, String count) {
    }
}


