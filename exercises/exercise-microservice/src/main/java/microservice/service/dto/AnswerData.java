package microservice.service.dto;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AnswerData {

  private static final String PREFIX = "/answer";
  private static final String DELIMITER = "#";

  public static String serialize(AnswerDto answer) {
    return PREFIX + DELIMITER + answer.getButtonCode() + DELIMITER + answer.getButtonText();
  }

  public static AnswerDto deserialize(String text) {
    Pattern pattern = Pattern.compile("^" + Pattern.quote(PREFIX + DELIMITER)
        + "([^" + DELIMITER + "]+)" + Pattern.quote(DELIMITER) + "([^" + DELIMITER + "]+)");
    Matcher matcher = pattern.matcher(text);
    if (matcher.matches()) {
      String buttonCode = matcher.group(1);
      String buttonText = matcher.group(2);
      return new AnswerDto(buttonText, buttonCode);
    } else {
      throw new IllegalArgumentException("Invalid input: " + text);
    }
  }
}
