package kvansipto.telegram.microservice.services.dto;

import java.util.List;
import kvansipto.exercise.dto.ExerciseResultDto;

public record ExerciseResultEvent(Long chatId, List<ExerciseResultDto> exerciseResults) {

}
