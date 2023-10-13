package academy.devdojo.springboot2essentials.service;

import academy.devdojo.springboot2essentials.domain.Anime;
import academy.devdojo.springboot2essentials.execption.BadRequestException;
import academy.devdojo.springboot2essentials.repository.AnimeRepository;
import academy.devdojo.springboot2essentials.util.AnimeCreator;
import academy.devdojo.springboot2essentials.util.AnimePostRequestBodyCreator;
import academy.devdojo.springboot2essentials.util.AnimePutRequestBodyCreator;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

@ExtendWith(SpringExtension.class)
class AnimeServiceTest {

    @InjectMocks
    private AnimeService animeService;

    @Mock
    private AnimeRepository animeRepositoryMock;

    @BeforeEach
    void setUp() {
        PageImpl<Anime> animePage = new PageImpl<>(List.of(AnimeCreator.createValidAnime()));

        BDDMockito.when(animeRepositoryMock.findAll(ArgumentMatchers.any(PageRequest.class)))
                .thenReturn(animePage);

        BDDMockito.when(animeRepositoryMock.findAll())
                .thenReturn(animePage.toList());

        BDDMockito.when(animeRepositoryMock.findByName(ArgumentMatchers.anyString()))
                .thenReturn(animePage.toList());

        BDDMockito.when(animeRepositoryMock.findById(ArgumentMatchers.anyLong()))
                .thenReturn(Optional.ofNullable(AnimeCreator.createValidAnime()));

        BDDMockito.when(animeRepositoryMock.save(ArgumentMatchers.any(Anime.class)))
                .thenReturn(AnimeCreator.createValidAnime());

        BDDMockito.doNothing().when(animeRepositoryMock).delete(ArgumentMatchers.any(Anime.class));

        BDDMockito.when(animeRepositoryMock.save(ArgumentMatchers.any(Anime.class)))
                .thenReturn(AnimeCreator.createValidAnime());
    }

    @Test
    @DisplayName("listAll returns List of Anime inside Page object when successful")
    void listAll_ReturnListOfAnimeInsideOfPageObject_WhenSuccessful() {
        Anime expectedAnime = AnimeCreator.createValidAnime();
        Page<Anime> animePage = animeService.listAll(PageRequest.of(0, 10));

        Assertions.assertThat(animePage).isNotNull();

        Assertions.assertThat(animePage.toList())
                .isNotNull()
                .hasSize(1);

        Assertions.assertThat(animePage.toList().get(0).getName())
                .isEqualTo(expectedAnime.getName());
    }

    @Test
    @DisplayName("listAllNonPageable restuns List of Anime when successfull")
    void listAllNonPageable_ReturnListOfAnime_WhenSuccessful() {
        Anime expectedAnime = AnimeCreator.createValidAnime();
        List<Anime> animeList = animeService.listAllNonPageable();

        Assertions.assertThat(animeList)
                .isNotNull()
                .isNotEmpty()
                .hasSize(1);

        Assertions.assertThat(animeList.get(0).getName())
                .isNotNull()
                .isEqualTo(expectedAnime.getName());
    }

    @Test
    @DisplayName("findByName returns a List of Anime when successful")
    void findByName_ReturnListOfAnime_WhenSuccessful() {
        Anime expectedAnime = AnimeCreator.createValidAnime();
        List<Anime> animeList = animeService.findByName(expectedAnime.getName());

        Assertions.assertThat(animeList)
                .isNotNull()
                .isNotEmpty()
                .hasSize(1);

        Assertions.assertThat(animeList.get(0).getName())
                .isNotNull()
                .isEqualTo(expectedAnime.getName());
    }

    @Test
    @DisplayName("findByIdOrThrowBadRequestException return Anime when Successful")
    void findByIdOrThrowBadRequestException_ReturnAnime_WhenSuccessful() {
        Anime expectedAnime = AnimeCreator.createValidAnime();
        Anime anime = animeService.findByIdOrThrowBadRequestException(1L);

        Assertions.assertThat(anime)
                .isNotNull()
                .isInstanceOf(Anime.class);

        Assertions.assertThat(anime.getName()).isEqualTo(expectedAnime.getName());
    }

    @Test
    @DisplayName("findByIdOrThrowBadRequestException throws BadRequestException when Anime is not foud")
    void findByIdOrThrowBadRequestException_ThrowBadRequestException_WhenAnimeIsNotFound() {
        BDDMockito.when(animeRepositoryMock.findById(ArgumentMatchers.anyLong()))
                .thenReturn(Optional.empty());

        Assertions.assertThatExceptionOfType(BadRequestException.class)
                        .isThrownBy(() -> animeService.findByIdOrThrowBadRequestException(1L));

//        Assertions.assertThatThrownBy(() -> animeService.findByIdOrThrowBadRequestException(2L))
//                        .isInstanceOf(BadRequestException.class);

    }

    @Test
    @DisplayName("save_ReturnAnime_WhenSuccesful")
    void save_ReturnAnime_WhenSuccessful() {
        Anime expectedAnime = AnimeCreator.createValidAnime();
        Anime anime = animeService.save(AnimePostRequestBodyCreator.createAnimePostRequestBody());

        Assertions.assertThat(anime).isNotNull();

        Assertions.assertThat(anime.getName()).isEqualTo(expectedAnime.getName());
    }

    @Test
    @DisplayName("delete removes Anime when successful")
    void delete_RemoveAnime_WhenSuccessful() {
        Assertions.assertThatCode(() -> animeService.delete(1L))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("replace updates Anime When successful")
    void replace_UpdateAnime_WhenSuccessful() {
        Assertions.assertThatCode(() -> animeService.replace(AnimePutRequestBodyCreator.createAnimePutRequestBody()))
                .doesNotThrowAnyException();
    }
}