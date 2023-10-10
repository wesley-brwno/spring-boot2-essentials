package academy.devdojo.springboot2essentials.util;

import academy.devdojo.springboot2essentials.requests.AnimePostRequestBody;

public class AnimePostRequestBodyCreator {
    public static AnimePostRequestBody createAnimePostRequestBody() {
        return AnimePostRequestBody.builder()
                .name(AnimeCreator.createValidAnime().getName())
                .build();
    }
}
