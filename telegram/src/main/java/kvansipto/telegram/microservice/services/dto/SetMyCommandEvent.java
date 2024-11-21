package kvansipto.telegram.microservice.services.dto;

import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;

public record SetMyCommandEvent(SetMyCommands action) {

}
