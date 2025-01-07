package br.com.alura.screenmatch.service;

import br.com.alura.screenmatch.dto.EpisodeDTO;
import br.com.alura.screenmatch.dto.SeriesDTO;
import br.com.alura.screenmatch.model.Category;
import br.com.alura.screenmatch.model.Series;
import br.com.alura.screenmatch.repository.SeriesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SeriesService {
    @Autowired
    private SeriesRepository repository;

    public List<SeriesDTO> getAllSeries() {
        return convertData(repository.findAll());
    }

    public List<SeriesDTO> getTop5Series() {
        return convertData(repository.findTop5ByOrderByRatingDesc());
    }

    public List<SeriesDTO> getRelease() {
        return convertData(repository.getLastFiveSeriesReleased());
    }

    public List<SeriesDTO> getSeriesByCategory(String name) {
        Category category = Category.fromPortuguese(name);
        return convertData(repository.findByGenre(category));
    }

    public SeriesDTO getSeriesById(Long id) {
        Optional<Series> series = repository.findById(id);

        if (series.isPresent()) {
            Series s = series.get();
            return new SeriesDTO(s.getId(), s.getTitle(), s.getTotalSeasons(), s.getGenre(), s.getActors(), s.getPlot(), s.getPoster(), s.getRating());
        }

        return null;
    }

    public List<EpisodeDTO> getAllSeasons(Long id) {
        Optional<Series> series = repository.findById(id);

        if (series.isPresent()) {
            Series s = series.get();
            return s.getEpisodes().stream()
                    .map(e -> new EpisodeDTO(e.getSeason(), e.getNumber(), e.getTitle()))
                    .collect(Collectors.toList());
        }

        return null;
    }

    public List<EpisodeDTO> getSeasonByNumber(Long id, Long number) {
        return repository.getEpisodesBySeason(id, number)
                .stream()
                .map(e -> new EpisodeDTO(e.getSeason(), e.getNumber(), e.getTitle()))
                .collect(Collectors.toList());
    }

    private List<SeriesDTO> convertData(List<Series> series) {
        return series.stream()
                .map(s -> new SeriesDTO(s.getId(), s.getTitle(), s.getTotalSeasons(), s.getGenre(), s.getActors(), s.getPlot(), s.getPoster(), s.getRating()))
                .collect(Collectors.toList());
    }
}
