package br.com.alura.screenmatch.main;

import br.com.alura.screenmatch.model.Episode;
import br.com.alura.screenmatch.model.EpisodeData;
import br.com.alura.screenmatch.model.SeasonData;
import br.com.alura.screenmatch.model.SeriesData;
import br.com.alura.screenmatch.service.ApiConsumption;
import br.com.alura.screenmatch.service.ConvertsData;
import org.springframework.cglib.core.Local;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Main {
    private final String ADDRESS = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=6585022c";

    private Scanner read = new Scanner(System.in);
    private ApiConsumption consumption = new ApiConsumption();
    private ConvertsData converter = new ConvertsData();

    public void showMenu() {
        System.out.print("Digite o nome da série para a busca: ");
        String seriesName = read.nextLine().strip();
        String formattedSeriesName = seriesName.replace(" ", "+");

        String json = consumption.getData( ADDRESS + formattedSeriesName + API_KEY);

        SeriesData seriesData = converter.getData(json, SeriesData.class);
        System.out.println(seriesData);

        List<SeasonData> seasons = new ArrayList<>();

		for (int i = 1; i <= seriesData.totalSeasons(); i++) {
			json = consumption.getData(ADDRESS + formattedSeriesName + "&season=" + i + API_KEY);
			SeasonData seasonData = converter.getData(json, SeasonData.class);
            seasons.add(seasonData);
		}

//        seasons.forEach(season -> season.episodes().forEach(episode -> System.out.println(episode.title())));

        List <EpisodeData> episodesData = seasons.stream()
                .flatMap(season -> season.episodes().stream())
                .collect(Collectors.toList());

        System.out.println("\nTop 5 episódios:");
        episodesData.stream()
                .filter(episode -> !episode.rating().equalsIgnoreCase("N/A"))
                .sorted(Comparator.comparing(EpisodeData::rating).reversed())
                .limit(5)
                .forEach(System.out::println);

        List<Episode> episodes = seasons.stream()
                .flatMap(season -> season.episodes().stream()
                        .map(episode -> new Episode(season.number(), episode))
                ).collect(Collectors.toList());

        episodes.forEach(System.out::println);

        System.out.print("A partir de que ano você deseja ver os episódios?: ");
        Integer year = read.nextInt();
        read.nextLine();

        LocalDate searchDate = LocalDate.of(year, 1, 1);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        episodes.stream()
                .filter(episode -> episode.getReleaseDate() != null && episode.getReleaseDate().isAfter(searchDate))
                .forEach(episode -> {System.out.println(
                        "Season: " + episode.getSeason() +
                        " | Episode: " + episode.getTitle() +
                        " | Realease Date: " + episode.getReleaseDate().format(formatter));
                });

    }
}
