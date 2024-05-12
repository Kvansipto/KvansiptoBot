package io.project.kvansiptobot.service;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

@Getter
public class MuscleCommandEvent extends ApplicationEvent {

  private final String message;

  private final long chatId;

  private final ReplyKeyboard replyKeyboard;

  public MuscleCommandEvent(Object source, long chatId, String message, ReplyKeyboard replyKeyboard) {
    super(source);
    this.message = message;
    this.chatId = chatId;
    this.replyKeyboard = replyKeyboard;
  }
}
