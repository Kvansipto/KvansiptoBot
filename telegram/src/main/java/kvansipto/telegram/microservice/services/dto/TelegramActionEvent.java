package kvansipto.telegram.microservice.services.dto;

import kvansipto.exercise.wrapper.BotApiMethodInterface;

public record TelegramActionEvent(BotApiMethodInterface action) {

}
