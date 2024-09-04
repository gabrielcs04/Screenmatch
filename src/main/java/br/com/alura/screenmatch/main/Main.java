package br.com.alura.screenmatch.main;

import br.com.alura.screenmatch.model.EpisodeData;
import br.com.alura.screenmatch.model.SeasonData;
import br.com.alura.screenmatch.model.SeriesData;
import br.com.alura.screenmatch.service.ApiConsumption;
import br.com.alura.screenmatch.service.ConvertsData;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

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

        seasons.forEach(season -> season.episodes().forEach(episode -> System.out.println(episode.title())));
    }
}
