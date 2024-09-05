package br.com.alura.screenmatch.main;

import br.com.alura.screenmatch.model.*;
import br.com.alura.screenmatch.repository.SeriesRepository;
import br.com.alura.screenmatch.service.ApiConsumption;
import br.com.alura.screenmatch.service.ConvertsData;

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
                    4 - Buscar série por título
                    5 - Buscar séries por ator
                    6 - Top 5 séries buscadas
                    7 - Buscar série por categoria
                    8 - Filtrar séries por temporada e avaliação
                    0 - Sair
                    """;

            System.out.println(menu);
            System.out.print("Digite uma opção: ");
            option = read.nextInt();
            read.nextLine();

            switch (option) {
                case 1:
                    searchSeriesInApi();
                    break;
                case 2:
                    searchEpisodeBySeries();
                    break;
                case 3:
                    listSearchedSeries();
                    break;
                case 4:
                    seachSeriesByTitle();
                    break;
                case 5:
                    seachSeriesByActor();
                    break;
                case 6:
                    searchTopFiveSeries();
                    break;
                case 7:
                    searchSeriesByCategory();
                    break;
                case 8:
                    filterSeriesBySeasonAndRating();
                    break;
                case 0:
                    System.out.println("Saindo...");
                    break;
                default:
                    System.out.println("Opção inválida");
            }
        }
    }

    private void searchSeriesInApi() {
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
        System.out.print("Digite o nome da série para busca: ");
        var seriesName = read.nextLine();

        Optional<Series> searchedSeries = repository.findByTitleContainingIgnoreCase(seriesName);

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

    private void seachSeriesByTitle() {
        System.out.print("Digite o nome da série para busca: ");
        var seriesName = read.nextLine();
        Optional<Series> searchedSeries = repository.findByTitleContainingIgnoreCase(seriesName);

        if (searchedSeries.isPresent()) {
            System.out.println("Dados da série: " + searchedSeries.get());
        } else {
            System.out.println("Série não encotrada");;
        }
    }

    private void seachSeriesByActor() {
        System.out.print("Digite o nome do ator para busca: ");
        var actorName = read.nextLine();
        System.out.print("Digite a avaliação mínima desejada para busca: ");
        var rating = read.nextDouble();

        List<Series> searchedSeries = repository.findByActorsContainingIgnoreCaseAndRatingGreaterThanEqual(actorName, rating);
        System.out.println("Séries em que " + actorName + " trabalhou:");
        searchedSeries.forEach(serie -> System.out.println(serie.getTitle() + " | Avaliação: " + serie.getRating()));
    }

    private void searchTopFiveSeries() {
        List<Series> topFiveSeries = repository.findTop5ByOrderByRatingDesc();
        System.out.println("Top 5 melhores séries:");
        topFiveSeries.forEach(serie -> System.out.println(serie.getTitle() + " | Avaliação: " + serie.getRating()));
    }

    private void searchSeriesByCategory() {
        System.out.print("Digite o nome da categoria/genêro para busca: ");
        var categoryName = read.nextLine();
        Category category = Category.fromPortuguese(categoryName);
        List<Series> seriesByCategory = repository.findByGenre(category);

        System.out.println("Séries da categoria/gênero " + categoryName + ":");;
        seriesByCategory.forEach(System.out::println);
    }

    private void filterSeriesBySeasonAndRating(){
        System.out.print("Digite o nº máximo de temporadas para filtrar: ");
        var totalSeasons = read.nextInt();
        read.nextLine();
        System.out.print("Digite a avaliação mínima para filtrar: ");
        var rating = read.nextDouble();
        read.nextLine();

        List<Series> filteredSeries = repository.findByTotalSeasonsLessThanEqualAndRatingGreaterThanEqual(totalSeasons, rating);
        System.out.println("Séries filtradas:");
        filteredSeries.forEach(serie -> System.out.println(serie.getTitle() + " | Avaliação: " + serie.getRating()));
    }
}