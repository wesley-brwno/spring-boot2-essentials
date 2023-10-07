package academy.devdojo.springboot2essentials.client;

import academy.devdojo.springboot2essentials.domain.Anime;
import lombok.extern.log4j.Log4j2;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Log4j2
public class SrpingClient {
    public static void main(String[] args) {
        ResponseEntity<Anime> entity = new RestTemplate().getForEntity("http://localhost:8080/animes/33", Anime.class);
        log.info(entity);

        Anime object = new RestTemplate().getForObject("http://localhost:8080/animes/{id}", Anime.class, 34);
        log.info(object);

        Anime[] animes = new RestTemplate().getForObject("http://localhost:8080/animes/all", Anime[].class);

        log.info(Arrays.toString(animes));

        ResponseEntity<List<Anime>> exchange = new RestTemplate().exchange("http://localhost:8080/animes/all",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<Anime>>() {});

        log.info(exchange.getBody());

//        Anime kingdom = Anime.builder().name("Kingdom").build();
//        Anime kingdomSaved = new RestTemplate().postForObject("http://localhost:8080/animes/", kingdom, Anime.class);
//        log.info("Anime saved {}", kingdomSaved);

        Anime chunibyou = Anime.builder().name("Chunibyou").build();
        ResponseEntity<Anime> chunibyouSaved = new RestTemplate().exchange("http://localhost:8080/animes/",
                HttpMethod.POST,
                new HttpEntity<>(chunibyou, createJsonHeader()),
                Anime.class);//.getBody
        log.info("Anime saved {}", chunibyouSaved);

        Anime animeToBeUpdated = chunibyouSaved.getBody();
        animeToBeUpdated.setName("chuunibyou demo koi ga shitai");

        ResponseEntity<Void> chunnibyouUpdated = new RestTemplate().exchange("http://localhost:8080/animes",
                HttpMethod.PUT,
                new HttpEntity<>(animeToBeUpdated, createJsonHeader()),
                Void.class);
        log.info(chunnibyouUpdated);

        System.err.println("-----------------Deleting: "+ animeToBeUpdated.getId());
        new RestTemplate().exchange("http://localhost:8080/animes/{id}",
                HttpMethod.DELETE,
                null,
                Void.class, animeToBeUpdated.getId());
    }
    private static HttpHeaders createJsonHeader() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        return httpHeaders;
    }
}
