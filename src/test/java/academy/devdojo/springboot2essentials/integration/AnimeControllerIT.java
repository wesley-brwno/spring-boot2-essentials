package academy.devdojo.springboot2essentials.integration;

import academy.devdojo.springboot2essentials.domain.Anime;
import academy.devdojo.springboot2essentials.repository.AnimeRepository;
import academy.devdojo.springboot2essentials.requests.AnimePostRequestBody;
import academy.devdojo.springboot2essentials.requests.AnimePutRequestBody;
import academy.devdojo.springboot2essentials.util.AnimeCreator;
import academy.devdojo.springboot2essentials.util.AnimePostRequestBodyCreator;
import academy.devdojo.springboot2essentials.util.AnimePutRequestBodyCreator;
import academy.devdojo.springboot2essentials.wrapper.PageableResponse;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;
import java.util.Objects;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class AnimeControllerIT {
    @Autowired
    private TestRestTemplate testRestTemplate;
    @LocalServerPort
    private int port;
    @Autowired
    private AnimeRepository animeRepository;

    @Test
    @DisplayName("List returns List of Anime inside Page object when successful")
    void list_ReturnListOfAnimeInsidePageObject_WhenSuccessful() {
        Anime savedAnime = animeRepository.save(AnimeCreator.createAnimeToBeSaved());

        String expectedName = savedAnime.getName();

        PageableResponse<Anime> animePage = testRestTemplate.exchange("/animes?size=3&page=0", HttpMethod.GET, null,
                new ParameterizedTypeReference<PageableResponse<Anime>>() {
                }).getBody();

        Assertions.assertThat(animePage).isNotNull();

        Assertions.assertThat(animePage.toList())
                .isNotEmpty()
                .hasSize(1);

        Assertions.assertThat(animePage.toList().get(0).getName()).isEqualTo(expectedName);
    }

    @Test
    @DisplayName("listAll return a List of Anime when successful")
    void listAll_ReturnListOfAnime_WhenSuccessful() {
        Anime savedAnime = animeRepository.save(AnimeCreator.createAnimeToBeSaved());

        String expectedName = savedAnime.getName();

        ResponseEntity<List<Anime>> animeEntity = testRestTemplate.exchange("/animes/all", HttpMethod.GET, null,
                new ParameterizedTypeReference<>() {
                });

        Assertions.assertThat(animeEntity.getBody())
                .isNotNull()
                .isNotEmpty()
                .hasSize(1);

        Assertions.assertThat(animeEntity.getBody().get(0).getName())
                .isNotNull()
                .isEqualTo(expectedName);

        Assertions.assertThat(animeEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    @DisplayName("findById returns Anime when successful")
    void findById_ReturnAnAnime_WhenSuccessful() {
        Anime savedAnime = animeRepository.save(AnimeCreator.createAnimeToBeSaved());

        Long expectedId = savedAnime.getId();

        ResponseEntity<Anime> animeEntity = testRestTemplate.getForEntity("/animes/{id}", Anime.class, expectedId);

        Assertions.assertThat(animeEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        Assertions.assertThat(animeEntity.getBody()).isNotNull().isInstanceOf(Anime.class);

        Assertions.assertThat(animeEntity.getBody().getName()).isNotNull().isEqualTo(savedAnime.getName());
    }

    @Test
    @DisplayName("findByName returns a List of Anime when successful")
    void findByName_ReturnListOfAnime_WhenSuccessful() {
        Anime savedAnime = animeRepository.save(AnimeCreator.createAnimeToBeSaved());

        String expectedName = savedAnime.getName();

        String url = String.format("/animes/find?name=%s", expectedName);

        ResponseEntity<List<Anime>> animeEntity = testRestTemplate.exchange(url, HttpMethod.GET, null,
                new ParameterizedTypeReference<List<Anime>>() {
                });

        Assertions.assertThat(animeEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        Assertions.assertThat(Objects.requireNonNull(animeEntity.getBody()).stream().toList()).isNotEmpty().hasSize(1);

        Assertions.assertThat(animeEntity.getBody().stream().toList().get(0).getName()).isNotNull().isEqualTo(expectedName);
    }

    @Test
    @DisplayName("findByName returns an empty List of Anime when anime is not found")
    void findByName_ReturnEmptyList_WhenAnimeIsNotFound() {
        Anime savedAnime = animeRepository.save(AnimeCreator.createAnimeToBeSaved());

        String url = String.format("/animes/find?name=%s", "!-unexpectedName^!");

        List<Anime> animeList = testRestTemplate.exchange(url, HttpMethod.GET, null,
                new ParameterizedTypeReference<List<Anime>>() {
                }).getBody();

        Assertions.assertThat(animeList)
                .isNotNull()
                .isEmpty();
    }


    @Test
    @DisplayName("save returns Anime when successful")
    void save_ReturnAnime_WhenSuccessful() {
        AnimePostRequestBody animePostRequestBody = AnimePostRequestBodyCreator.createAnimePostRequestBody();

        ResponseEntity<Anime> animeResponseEntity = testRestTemplate.postForEntity("/animes", animePostRequestBody, Anime.class);

        Assertions.assertThat(animeResponseEntity).isNotNull();

        Assertions.assertThat(animeResponseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        Assertions.assertThat(Objects.requireNonNull(animeResponseEntity.getBody()).getId()).isNotNull();
    }

    @Test
    @DisplayName("delete deletes Anime when successful")
    void delete_DeletesAnime_WhenSuccessful() {
        Anime savedAnime = animeRepository.save(AnimeCreator.createAnimeToBeSaved());

        ResponseEntity<Void> responseEntity = testRestTemplate.exchange("/animes/{id}", HttpMethod.DELETE,
                null, Void.class, savedAnime.getId());

        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        Assertions.assertThat(responseEntity).isNotNull();
    }

    @Test
    @DisplayName("replace updates Anime when successful")
    void replace_UpdateAnime_WhenSuccessful() {
        Anime savedAnime = animeRepository.save(AnimeCreator.createAnimeToBeSaved());

        AnimePutRequestBody animePutRequestBody = AnimePutRequestBodyCreator.createAnimePutRequestBody();

        animePutRequestBody.setName("new name");

        ResponseEntity<Void> responseEntity = testRestTemplate.exchange("/animes", HttpMethod.PUT,
                new HttpEntity<>(animePutRequestBody), Void.class);

        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        Assertions.assertThat(responseEntity).isNotNull();
    }
}
