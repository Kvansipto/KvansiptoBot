package microservice.entity;

import lombok.Getter;

@Getter
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

  public static MuscleGroup fromName(String name) {
    for (MuscleGroup muscleGroup : values()) {
      if (muscleGroup.getName().equalsIgnoreCase(name)) {
        return muscleGroup;
      }
    }
    throw new IllegalArgumentException("No enum constant with name " + name);
  }
}
