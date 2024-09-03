package br.com.alura.screenmatch;

import br.com.alura.screenmatch.model.SeasonData;
import br.com.alura.screenmatch.model.SerieData;
import br.com.alura.screenmatch.service.ApiConsumption;
import br.com.alura.screenmatch.service.ConvertsData;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.ArrayList;
import java.util.List;

@SpringBootApplication
public class ScreenmatchApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(ScreenmatchApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		var apiConsumption = new ApiConsumption();

		var json = apiConsumption.getData("https://www.omdbapi.com/?t=gilmore+girls&apikey=6585022c");
		System.out.println(json);

		ConvertsData converter = new ConvertsData();

		SerieData serieData = converter.getData(json, SerieData.class);
		System.out.println(serieData);

		List<SeasonData> seasons = new ArrayList<>();

		for (int i = 1; i <= serieData.totalSeasons(); i++) {
			json = apiConsumption.getData("https://www.omdbapi.com/?t=gilmore+girls&season=" + i + "&apikey=6585022c");
			SeasonData seasonData = converter.getData(json, SeasonData.class);
			seasons.add(seasonData);
		}

		seasons.forEach(System.out::println);
    }
}
