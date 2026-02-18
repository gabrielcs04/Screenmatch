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
    private Optional<Series> searchedSeries;

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
                    9 - Buscar episódio por trecho do título
                    10 - Top 5 episódios de uma série
                    11 - Buscar episódios de uma série a partir de um ano
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
                    searchSeriesByTitle();
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
                case 9:
                    searchEpisodeByTitleSnippet();
                    break;
                case 10:
                    searchTopFiveEpisodesBySeries();
                    break;
                case 11:
                    searchEpisodesBySeriesAndAfterDate();
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
            System.out.println("Série não encontrada");;
        }
    }

    private void listSearchedSeries() {
        this.series = repository.findAll();
        series.stream()
                .sorted(Comparator.comparing(Series::getGenre))
                .forEach(System.out::println);
    }

    private void searchSeriesByTitle() {
        System.out.print("Digite o nome da série para busca: ");
        var seriesName = read.nextLine();
        this.searchedSeries = repository.findByTitleContainingIgnoreCase(seriesName);

        if (this.searchedSeries.isPresent()) {
            System.out.println("Dados da série: " + this.searchedSeries.get());
        } else {
            System.out.println("Série não encontrada");;
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

        List<Series> filteredSeries = repository.getSeriesByMaximumSeasonAndMinimumRating(totalSeasons, rating);
        System.out.println("Séries filtradas:");
        filteredSeries.forEach(serie -> System.out.println(serie.getTitle() + " | Avaliação: " + serie.getRating()));
    }

    private void searchEpisodeByTitleSnippet() {
        System.out.print("Digite o nome do episódigo para busca: ");
        var episodeTitleSnippet = read.nextLine();

        List<Episode> searchedEpisodes = repository.getEpisodeByTitleSnippet(episodeTitleSnippet);
        searchedEpisodes.forEach(e -> {
            System.out.printf("Série: %s | Temporada %s - Episódio %s | Nome: %s\n",
                    e.getSeries().getTitle(), e.getSeason(), e.getNumber(), e.getTitle());
        });
    }

    private void searchTopFiveEpisodesBySeries() {
        searchSeriesByTitle();
        if (this.searchedSeries.isPresent()) {
            Series series = this.searchedSeries.get();
            List<Episode> topFiveEpisodes = repository.getTopFiveEpisodesBySeries(series);
            topFiveEpisodes.forEach(e -> {
                System.out.printf("Temporada %s - Episódio %s | Nome: %s | Avaliação: %f\n",
                        e.getSeason(), e.getNumber(), e.getTitle(), e.getRating());
            });
        }
    }

    private void searchEpisodesBySeriesAndAfterDate() {
        searchSeriesByTitle();
        if (this.searchedSeries.isPresent()) {
            System.out.print("Digite o ano de lançamento para busca: ");
            var releaseYear = read.nextInt();
            read.nextLine();

            Series series = this.searchedSeries.get();
            List<Episode> filteredEpisodes = repository.getEpisodesBySeriesAndAfterYear(series, releaseYear);
            filteredEpisodes.forEach(System.out::println);
        }
    }
}