package br.com.alura.screenmatch.main;

import br.com.alura.screenmatch.model.Episode;
import br.com.alura.screenmatch.model.SeasonData;
import br.com.alura.screenmatch.model.Series;
import br.com.alura.screenmatch.model.SeriesData;
import br.com.alura.screenmatch.repository.SeriesRepository;
import br.com.alura.screenmatch.service.ApiConsumption;
import br.com.alura.screenmatch.service.ConvertsData;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

public class Main {
    private final String ADDRESS = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=6585022c";

    private Scanner read = new Scanner(System.in);
    private ApiConsumption consumer = new ApiConsumption();
    private ConvertsData converter = new ConvertsData();
    private List<SeriesData> seriesData = new ArrayList<>();
    private SeriesRepository repository;
    private List<Series> series = new ArrayList<>();

    public Main(SeriesRepository repository) {
        this.repository = repository;
    }

    public void showMenu() {
        var option = -1;
        while (option != 0) {
            var menu = """
                    
                    1 - Buscar séries
                    2 - Buscar episódios
                    3 - Listar séries buscadas
                    0 - Sair
                    """;

            System.out.println(menu);
            System.out.print("Digite uma opção: ");
            option = read.nextInt();
            read.nextLine();

            switch (option) {
                case 1:
                    searchSeriesInWeb();
                    break;
                case 2:
                    searchEpisodeBySeries();
                    break;
                case 3:
                    listSearchedSeries();
                    break;
                case 0:
                    System.out.println("Saindo...");
                    break;
                default:
                    System.out.println("Opção inválida");
            }
        }
    }

    private void searchSeriesInWeb() {
        SeriesData data = getSeriesData();
        Series series = new Series(data);
        this.repository.save(series);
        System.out.println(data);
    }

    private SeriesData getSeriesData() {
        System.out.print("Digite o nome da série para busca: ");
        var seriesName = read.nextLine();
        var json = consumer.getData(ADDRESS + seriesName.replace(" ", "+") + API_KEY);
        SeriesData data = converter.getData(json, SeriesData.class);
        return data;
    }

    private void searchEpisodeBySeries() {
        listSearchedSeries();
        System.out.print("Escolha uma série pelo nome: ");
        var seriesName = read.nextLine();

        Optional<Series> searchedSeries = series.stream()
                .filter(s -> s.getTitle().toLowerCase().contains(seriesName.toLowerCase()))
                .findFirst();

        if(searchedSeries.isPresent()) {
            var foundSeries = searchedSeries.get();
            List<SeasonData> seasons = new ArrayList<>();

            for (int i = 1; i <= foundSeries.getTotalSeasons(); i++) {
                var json = consumer.getData(ADDRESS + foundSeries.getTitle().replace(" ", "+").trim() + "&season=" + i + API_KEY);
                SeasonData seasonsData = converter.getData(json, SeasonData.class);
                seasons.add(seasonsData);
            }
            seasons.forEach(System.out::println);

            List<Episode> episodes = seasons.stream()
                    .flatMap(data -> data.episodes().stream()
                            .map(episode -> new Episode(data.number(), episode)))
                    .collect((Collectors.toList()));

            foundSeries.setEpisodes(episodes);
            repository.save(foundSeries);
        } else {
            System.out.println("Série não encotrada");;
        }
    }

    private void listSearchedSeries() {
        this.series = repository.findAll();
        series.stream()
                .sorted(Comparator.comparing(Series::getGenre))
                .forEach(System.out::println);
    }
}