package kvansipto.telegram.microservice.services.dto;

import java.util.List;
import java.util.regex.Pattern;

public class AnswerData {

  private static final String PREFIX = "/answer";
  private static final String DELIMITER = "#";

  public static String serialize(AnswerDto answer) {
    var builder = List.of(PREFIX, answer.getButtonCode(), answer.getButtonText(), answer.getHiddenText());
    return String.join(DELIMITER, builder);
  }

  public static AnswerDto deserialize(String text) {
    var pattern = Pattern.compile("^" + Pattern.quote(PREFIX + DELIMITER)
        + "([^" + DELIMITER + "]+)" + Pattern.quote(DELIMITER) + "([^" + DELIMITER + "]+)" + Pattern.quote(DELIMITER)
        + "([^" + DELIMITER + "]+)");
    var matcher = pattern.matcher(text);
    if (matcher.matches()) {
      String answerText = matcher.group(1);
      String answerCode = matcher.group(2);
      String hiddenText = matcher.group(3);
      return new AnswerDto(answerCode, answerText, hiddenText);
    } else {
      throw new IllegalArgumentException("Invalid input: " + text);
    }
  }
}
