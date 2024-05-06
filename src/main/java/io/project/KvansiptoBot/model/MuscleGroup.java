package io.project.KvansiptoBot.model;

public enum MuscleGroup {
  CHEST("Грудные"),
  BACK("Спина"),
  LEGS("Ноги"),
  ARMS("Руки"),
  SHOULDERS("Плечи"),
  CORE("Пресс");

  private final String name;

  MuscleGroup(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
