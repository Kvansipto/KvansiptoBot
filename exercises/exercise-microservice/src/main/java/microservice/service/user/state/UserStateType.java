package microservice.service.user.state;

public enum UserStateType {
  CHOOSING_MUSCLE_GROUP("CHOOSING MUSCLE GROUP"),
  CHOOSING_EXERCISE("CHOOSING EXERCISE"),
  VIEWING_EXERCISE("VIEWING EXERCISE"),
  CHOOSING_DATE("CHOOSING DATE"),
  WAITING_FOR_RESULT("WAITING_FOR_RESULT");


  private final String name;

  UserStateType(String name) {
    this.name = name;
  }
  }
