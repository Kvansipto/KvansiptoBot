package microservice.service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnswerDto {
  private String buttonText;  // Текст на кнопке
  private String buttonCode;  // Код для идентификации действия
}
