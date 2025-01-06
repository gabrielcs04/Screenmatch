package br.com.alura.screenmatch.dto;

import br.com.alura.screenmatch.model.Category;

public record SeriesDTO(Long id,
                        String titulo,
                        Integer totalTemporadas,
                        Category genero,
                        String atores,
                        String sinopse,
                        String poster,
                        Double avaliacao) {
}