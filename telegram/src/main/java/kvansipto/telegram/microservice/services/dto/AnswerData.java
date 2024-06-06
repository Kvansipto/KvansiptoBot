package kvansipto.telegram.microservice.services.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@RequiredArgsConstructor
public class AnswerData {

  private static final String PREFIX = "/answer";
  private static final String DELIMITER = "#";

  public static String serialize(AnswerDto answer) {
    List<String> builder = new ArrayList<>() {
    };
    builder.add(PREFIX);
    builder.add(answer.getButtonCode());
    builder.add(answer.getButtonText());
    builder.add(answer.getHiddenText());

    return String.join(DELIMITER, builder);
  }

  public static AnswerDto deserialize(String text) {
    Pattern pattern = Pattern.compile("^" + Pattern.quote(PREFIX + DELIMITER)
        + "([^" + DELIMITER + "]+)" + Pattern.quote(DELIMITER) + "([^" + DELIMITER + "]+)" + Pattern.quote(DELIMITER)
        + "([^" + DELIMITER + "]+)");
    Matcher matcher = pattern.matcher(text);
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
