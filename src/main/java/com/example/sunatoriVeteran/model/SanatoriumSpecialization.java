package com.example.sunatoriVeteran.model;

public enum SanatoriumSpecialization {
    AMPUTATION_LOWER("Ампутація нижніх кінцівок"),
    AMPUTATION_UPPER("Ампутація верхніх кінцівок"),
    WHEELCHAIR("Крісло колісне"),
    MENTAL_HEALTH("Психічне здоров'я (ПТСР тощо)"),
    HEARING("Вади слуху"),
    VISION("Вади зору"),
    GENERAL_TRAUMA("Загальні травми / ОРА"),
    NEUROLOGICAL("Неврологічні порушення");

    private final String description;

    SanatoriumSpecialization(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
