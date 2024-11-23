package microservice.service.dto;

import lombok.Getter;

public record AnswerDto(
    @Getter
    String buttonText,
    @Getter
    String buttonCode) {

}
