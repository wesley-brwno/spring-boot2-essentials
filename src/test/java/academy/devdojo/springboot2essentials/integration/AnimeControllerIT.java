package academy.devdojo.springboot2essentials.integration;

import academy.devdojo.springboot2essentials.domain.Anime;
import academy.devdojo.springboot2essentials.domain.DevDojoUser;
import academy.devdojo.springboot2essentials.repository.AnimeRepository;
import academy.devdojo.springboot2essentials.repository.DevDojoUserRepository;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
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
    @Qualifier(value = "testRestTemplateRoleUser")
    private TestRestTemplate testRestTemplateRoleUser;
    @Autowired
    @Qualifier(value = "testRestTemplateRoleAdmin")
    private TestRestTemplate testRestTemplateRoleAdmin;


//    @LocalServerPort
//    private int port;
    @Autowired
    private AnimeRepository animeRepository;
    @Autowired
    private DevDojoUserRepository devDojoUserRepository;

    private static final DevDojoUser USER = DevDojoUser.builder()
            .name("Devdojo Academy")
            .password("{bcrypt}$2a$10$edC3nxbf8mDBKV2JKKNFx.cDL8LLf/Twg7tMIbZXwOSr8NNxAbom2")
            .authorities("ROLE_USER")
            .username("devdojo")
            .build();

    private static final DevDojoUser ADMIN = DevDojoUser.builder()
            .name("Wesley Bruno")
            .password("{bcrypt}$2a$10$edC3nxbf8mDBKV2JKKNFx.cDL8LLf/Twg7tMIbZXwOSr8NNxAbom2")
            .authorities("ROLE_USER,ROLE_ADMIN")
            .username("wesley")
            .build();

    @TestConfiguration
    @Lazy
    static class Config {
        @Bean(name = "testRestTemplateRoleUser")
        public TestRestTemplate testRestTemplateRoleUserCreator(@Value("${local.server.port}") int port) {
            RestTemplateBuilder restTemplateBuilder = new RestTemplateBuilder()
                    .rootUri("http://localhost:" + port)
                    .basicAuthentication("devdojo", "academy");
            return new TestRestTemplate(restTemplateBuilder);
        }

        @Bean(name = "testRestTemplateRoleAdmin")
        public TestRestTemplate testRestTemplateRoleAdminCreator(@Value("${local.server.port}") int port) {
            RestTemplateBuilder restTemplateBuilder = new RestTemplateBuilder()
                    .rootUri("http://localhost:" + port)
                    .basicAuthentication("wesley", "academy");
            return new TestRestTemplate(restTemplateBuilder);
        }
    }

    @Test
    @DisplayName("List returns List of Anime inside Page object when successful")
    void list_ReturnListOfAnimeInsidePageObject_WhenSuccessful() {
        Anime savedAnime = animeRepository.save(AnimeCreator.createAnimeToBeSaved());
        devDojoUserRepository.save(USER);

        String expectedName = savedAnime.getName();

        PageableResponse<Anime> animePage = testRestTemplateRoleUser.exchange("/animes?size=3&page=0", HttpMethod.GET, null,
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
        devDojoUserRepository.save(USER);

        String expectedName = savedAnime.getName();

        ResponseEntity<List<Anime>> animeEntity = testRestTemplateRoleUser.exchange("/animes/all", HttpMethod.GET, null,
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
        devDojoUserRepository.save(USER);

        Long expectedId = savedAnime.getId();

        ResponseEntity<Anime> animeEntity = testRestTemplateRoleUser.getForEntity("/animes/{id}", Anime.class, expectedId);

        Assertions.assertThat(animeEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        Assertions.assertThat(animeEntity.getBody()).isNotNull().isInstanceOf(Anime.class);

        Assertions.assertThat(animeEntity.getBody().getName()).isNotNull().isEqualTo(savedAnime.getName());
    }

    @Test
    @DisplayName("findByName returns a List of Anime when successful")
    void findByName_ReturnListOfAnime_WhenSuccessful() {
        Anime savedAnime = animeRepository.save(AnimeCreator.createAnimeToBeSaved());
        devDojoUserRepository.save(USER);

        String expectedName = savedAnime.getName();

        String url = String.format("/animes/find?name=%s", expectedName);

        ResponseEntity<List<Anime>> animeEntity = testRestTemplateRoleUser.exchange(url, HttpMethod.GET, null,
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
        devDojoUserRepository.save(USER);

        String url = String.format("/animes/find?name=%s", "!-unexpectedName^!");

        List<Anime> animeList = testRestTemplateRoleUser.exchange(url, HttpMethod.GET, null,
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
        devDojoUserRepository.save(USER);

        ResponseEntity<Anime> animeResponseEntity = testRestTemplateRoleUser.postForEntity("/animes", animePostRequestBody, Anime.class);

        Assertions.assertThat(animeResponseEntity).isNotNull();

        Assertions.assertThat(animeResponseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        Assertions.assertThat(Objects.requireNonNull(animeResponseEntity.getBody()).getId()).isNotNull();
    }

    @Test
    @DisplayName("delete deletes Anime when successful")
    void delete_DeletesAnime_WhenSuccessful() {
        Anime savedAnime = animeRepository.save(AnimeCreator.createAnimeToBeSaved());
        devDojoUserRepository.save(ADMIN);

        ResponseEntity<Void> responseEntity = testRestTemplateRoleAdmin.exchange("/animes/admin/{id}", HttpMethod.DELETE,
                null, Void.class, savedAnime.getId());

        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        Assertions.assertThat(responseEntity).isNotNull();
    }

    @Test
    @DisplayName("delete return 403 when user is not an admin")
    void delete_Return403_WhenUserIsNotAdmin() {
        Anime savedAnime = animeRepository.save(AnimeCreator.createAnimeToBeSaved());
        devDojoUserRepository.save(USER);

        ResponseEntity<Void> responseEntity = testRestTemplateRoleUser.exchange("/animes/admin/{id}", HttpMethod.DELETE,
                null, Void.class, savedAnime.getId());

        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

        Assertions.assertThat(responseEntity).isNotNull();
    }

    @Test
    @DisplayName("replace updates Anime when successful")
    void replace_UpdateAnime_WhenSuccessful() {
        Anime savedAnime = animeRepository.save(AnimeCreator.createAnimeToBeSaved());
        devDojoUserRepository.save(USER);

        AnimePutRequestBody animePutRequestBody = AnimePutRequestBodyCreator.createAnimePutRequestBody();

        animePutRequestBody.setName("new name");

        ResponseEntity<Void> responseEntity = testRestTemplateRoleUser.exchange("/animes", HttpMethod.PUT,
                new HttpEntity<>(animePutRequestBody), Void.class);

        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        Assertions.assertThat(responseEntity).isNotNull();
    }
}