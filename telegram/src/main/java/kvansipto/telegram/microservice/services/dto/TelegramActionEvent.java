package kvansipto.telegram.microservice.services.dto;

import kvansipto.telegram.microservice.services.wrapper.BotApiMethodInterface;

public record TelegramActionEvent(BotApiMethodInterface action) {

}
