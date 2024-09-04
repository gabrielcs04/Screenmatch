package br.com.alura.screenmatch.main;

import br.com.alura.screenmatch.model.Episode;
import br.com.alura.screenmatch.model.EpisodeData;
import br.com.alura.screenmatch.model.SeasonData;
import br.com.alura.screenmatch.model.SeriesData;
import br.com.alura.screenmatch.service.ApiConsumption;
import br.com.alura.screenmatch.service.ConvertsData;

import java.util.*;
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

//        System.out.println("\nTop 10 episódios:");
//        episodesData.stream()
//                .filter(episode -> !episode.rating().equalsIgnoreCase("N/A"))
//                .peek(episode -> System.out.println("Primeiro filtro (N/A) " + episode))
//                .sorted(Comparator.comparing(EpisodeData::rating).reversed())
//                .peek(episode -> System.out.println("Ordenação " + episode))
//                .limit(10)
//                .peek(episode -> System.out.println("Limite " + episode))
//                .map(episode -> episode.title().toUpperCase())
//                .peek(episode -> System.out.println("Mapeamento " + episode))
//                .forEach(System.out::println);

        List<Episode> episodes = seasons.stream()
                .flatMap(season -> season.episodes().stream()
                        .map(episode -> new Episode(season.number(), episode))
                ).collect(Collectors.toList());

        episodes.forEach(System.out::println);

//        System.out.print("Digite o título do episódio que deseja encontrar: ");
//        var titleChunk = read.nextLine();
//
//        Optional<Episode> episodeSearched = episodes.stream()
//                .filter(episode -> episode.getTitle().toUpperCase().contains(titleChunk.toUpperCase()))
//                .findFirst();
//
//        if (episodeSearched.isPresent()) {
//            System.out.println("Episódio encontrado!");
//            System.out.println("Episódio: " + episodeSearched.get().getNumber() + " | Temporada: " + episodeSearched.get().getSeason());
//        } else {
//            System.out.println("Episódio não encontrado!");
//        }

//        System.out.print("A partir de que ano você deseja ver os episódios?: ");
//        int year = read.nextInt();
//        read.nextLine();
//
//        LocalDate searchDate = LocalDate.of(year, 1, 1);
//
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
//        episodes.stream()
//                .filter(episode -> episode.getReleaseDate() != null && episode.getReleaseDate().isAfter(searchDate))
//                .forEach(episode -> {System.out.println(
//                        "Temporada: " + episode.getSeason() +
//                        " | Episódio: " + episode.getTitle() +
//                        " | Data de Lançamento: " + episode.getReleaseDate().format(formatter));
//                });

        Map<Integer, Double> ratingPerSeason = episodes.stream()
                .filter(episode -> episode.getRating() > 0.0)
                .collect(Collectors.groupingBy(Episode::getSeason,
                        Collectors.averagingDouble(Episode::getRating))
                );

        System.out.println(ratingPerSeason);

        DoubleSummaryStatistics statistics = episodes.stream()
                .filter(episode -> episode.getRating() > 0.0)
                .mapToDouble(Episode::getRating)
                .summaryStatistics();

        System.out.println("Média: " + statistics.getAverage());
        System.out.println("Melhor avaliação: " + statistics.getMax());
        System.out.println("Pior avaliação: " + statistics.getMin());
        System.out.println("Episódios avaliados: " + statistics.getCount());
    }
}
