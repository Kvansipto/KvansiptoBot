package microservice.service.dto;

import java.util.regex.Pattern;
import lombok.experimental.UtilityClass;

@UtilityClass
public class AnswerData {

  private static final String PREFIX = "/answer";
  private static final String DELIMITER = "#";

  public static String serialize(AnswerDto answer) {
    return PREFIX + DELIMITER + answer.getButtonCode() + DELIMITER + answer.getButtonText();
  }

  public static AnswerDto deserialize(String text) {
    var pattern = Pattern.compile(
        "^%s([^%s]+)%s([^%s]+)".formatted(Pattern.quote(PREFIX + DELIMITER), DELIMITER, Pattern.quote(DELIMITER),
            DELIMITER));
    var matcher = pattern.matcher(text);
    if (matcher.matches()) {
      var buttonCode = matcher.group(1);
      var buttonText = matcher.group(2);
      return new AnswerDto(buttonText, buttonCode);
    } else {
      throw new IllegalArgumentException("Invalid input: " + text);
    }
  }
}
