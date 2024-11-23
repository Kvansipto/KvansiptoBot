package kvansipto.telegram.microservice.services;

import jakarta.annotation.PostConstruct;
import java.sql.Timestamp;
import kvansipto.exercise.dto.UpdateDto;
import kvansipto.exercise.dto.UserDto;
import kvansipto.exercise.wrapper.BotApiMethodInterface;
import kvansipto.telegram.microservice.config.BotConfig;
import kvansipto.telegram.microservice.services.dto.TelegramActionEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Component
@Slf4j
//@RequiredArgsConstructor
public class TelegramBot extends TelegramLongPollingBot implements TelegramBotInterface {

  private final BotConfig config;
  private final KafkaTelegramService kafkaTelegramService;

  @Autowired
  public TelegramBot(BotConfig config,
      KafkaTelegramService kafkaTelegramService) {
    super(config.getBotToken());
    this.config = config;
    this.kafkaTelegramService = kafkaTelegramService;
  }

  @Async
  @EventListener
  public void handleSetMyCommandEvent(SetMyCommands event) {
    try {
      this.execute(event);
    } catch (TelegramApiException e) {
      log.error(e.getMessage(), e);
    }
  }

  @PostConstruct
  public void init() throws TelegramApiException {
    var telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
    try {
      telegramBotsApi.registerBot(this);
    } catch (TelegramApiException e) {
      log.error(e.getMessage(), e);
    }
  }

  @Override
  public String getBotUsername() {
    return config.getBotName();
  }

  @Override
  public void onUpdateReceived(Update update) {
    log.info("Update received: {}", update);
    var isMessage = update.hasMessage() && update.getMessage().hasText();
    Long chatId;
    User user;

    if (isMessage) {
      var message = update.getMessage();
      chatId = message.getChatId();
      user = message.getFrom();
    } else if (update.hasCallbackQuery()) {
      var callbackQuery = update.getCallbackQuery();
      chatId = callbackQuery.getMessage().getChatId();
      user = callbackQuery.getFrom();
    } else {
      return;
    }

    final var userDto = UserDto.builder()
        .id(chatId)
        .userName(user.getUserName())
        .firstName(user.getFirstName())
        .lastName(user.getLastName())
        .registeredAt(new Timestamp(System.currentTimeMillis()))
        .build();

    UpdateDto.UpdateDtoBuilder<?, ?> updateDto = UpdateDto.builder()
        .user(userDto)
        .message(isMessage ? update.getMessage().getText() : update.getCallbackQuery().getData())
        .messageId(
            isMessage ? update.getMessage().getMessageId() : update.getCallbackQuery().getMessage().getMessageId());
    kafkaTelegramService.sendUpdateDto(chatId, updateDto.build()).subscribe();
  }

  @Async
  @EventListener
  public void handleTelegramActionEvent(TelegramActionEvent event) {
    executeTelegramAction(event.action());
  }

  private void executeTelegramAction(BotApiMethodInterface action) {
    try {
      action.accept(this);
    } catch (TelegramApiException e) {
      log.error(e.getMessage(), e);
    }
  }
}
