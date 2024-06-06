package kvansipto.telegram.microservice.services.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class AnswerDto {

  private String buttonText;
  private String buttonCode;
  private String hiddenText;

  public AnswerDto(String buttonText, String buttonCode) {
    this.buttonText = buttonText;
    this.buttonCode = buttonCode;
  }
}
