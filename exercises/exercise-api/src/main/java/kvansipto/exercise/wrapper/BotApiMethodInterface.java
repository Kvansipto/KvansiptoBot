package kvansipto.exercise.wrapper;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
    @JsonSubTypes.Type(value = BotApiMethodWrapper.class, name = "BotApiMethodWrapper"),
    @JsonSubTypes.Type(value = SendMessageWrapper.class, name = "SendMessageWrapper"),
    @JsonSubTypes.Type(value = EditMessageWrapper.class, name = "EditMessageWrapper"),
    @JsonSubTypes.Type(value = SendPhotoWrapper.class, name = "SendPhotoWrapper"),
    @JsonSubTypes.Type(value = DeleteMessagesWrapper.class, name = "DeleteMessageWrapper")
})
public interface BotApiMethodInterface {

  void accept(TelegramLongPollingBot bot) throws TelegramApiException;
}
