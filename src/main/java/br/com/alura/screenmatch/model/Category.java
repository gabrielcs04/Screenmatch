package br.com.alura.screenmatch.model;

public enum Category {
    ACAO("Action", "Ação"),
    ROMANCE("Romance", "Romance"),
    COMEDIA("Comedy", "Comédia"),
    DRAMA("Drama", "Drama"),
    CRIME("Crime", "Crime");

    private String omdbCategory;
    private String ptCategory;

    Category(String omdbCategory, String ptCategory) {
        this.omdbCategory = omdbCategory;
        this.ptCategory = ptCategory;
    }

    public static Category fromString(String text) {
        for (Category category : Category.values()) {
            if (category.omdbCategory.equalsIgnoreCase(text)) {
                return category;
            }
        }
        throw new IllegalArgumentException("Nenhuma categoria encontrada para a string fornecida: " + text);
    }

    public static Category fromPortuguese(String text) {
        for (Category category : Category.values()) {
            if (category.ptCategory.equalsIgnoreCase(text)) {
                return category;
            }
        }
        throw new IllegalArgumentException("Nenhuma categoria encontrada para a string fornecida: " + text);
    }
}
