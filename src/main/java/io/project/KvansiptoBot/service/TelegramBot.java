package io.project.KvansiptoBot.service;

import io.project.KvansiptoBot.config.BotConfig;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Component
public class TelegramBot extends TelegramLongPollingBot {

  final BotConfig config;

  public TelegramBot(BotConfig config) {
    this.config = config;
  }

  @Override
  public String getBotUsername() {
    return config.getBotName();
  }

  @Override
  public void onUpdateReceived(Update update) {

    if (update.hasMessage() && update.getMessage().hasText()) {
      String message = update.getMessage().getText();
      long chatId = update.getMessage().getChatId();

      switch (message) {
        case "/start":
          startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
          break;
        default:
          sendMessage(chatId, "Sorry, command wasn't recognized");
      }
    }
  }

  private void startCommandReceived(long chatId, String name) {
    String answer = "Hi, " + name + "! Nice to meet you!";
    sendMessage(chatId, answer);
  }

  private void sendMessage(long chatId, String messageToSend) {
    SendMessage message = new SendMessage();
    message.setChatId(chatId);
    message.setText(messageToSend);

    try {
      execute(message);
    } catch (TelegramApiException e) {
      e.printStackTrace();
    }
  }

  @Override
  public String getBotToken() {
    return config.getToken();
  }
}
