package microservice.service.event;

import kvansipto.exercise.dto.UpdateDto;

public record UserInputCommandEvent(Object source, Long chatId, UpdateDto update) {

}
