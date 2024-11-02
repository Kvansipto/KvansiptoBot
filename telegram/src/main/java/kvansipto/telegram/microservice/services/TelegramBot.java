package kvansipto.telegram.microservice.services;

import jakarta.annotation.PostConstruct;
import java.sql.Timestamp;
import java.util.List;
import kvansipto.exercise.dto.UpdateDto;
import kvansipto.exercise.dto.UserDto;
import kvansipto.exercise.wrapper.BotApiMethodInterface;
import kvansipto.telegram.microservice.config.BotConfig;
import kvansipto.telegram.microservice.services.dto.TelegramActionEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Component
@Slf4j
public class TelegramBot extends TelegramLongPollingBot implements TelegramBotInterface {

  final BotConfig config;

  @Value("${kafka.topic.messages}")
  private String messagesToExercisesTopicName;
//  @Value("${kafka.topic.callbacks}")
//  private String callbacksToExercisesTopicName;
//  @Value("${kafka.topic.actions}")
//  private String actionsFromExercisesTopicName;

  private final KafkaTemplate<Long, UpdateDto> kafkaTemplate;

  @Autowired
  public TelegramBot(BotConfig config, KafkaTemplate<Long, UpdateDto> kafkaTemplate) {
    super(config.getBotToken());
    this.config = config;
    this.kafkaTemplate = kafkaTemplate;
  }

  @KafkaListener(topics = "${kafka.topic.main-menu-commands}",
      groupId = "${kafka.group.id.main-menu-commands}",
      containerFactory = "botCommandListKafkaListenerFactory")
  @Override
  public void receivedCommandList(List<BotCommand> commands) {
    log.info("Received commands: {}", commands);
    try {
      this.execute(new SetMyCommands(commands, new BotCommandScopeDefault(), null));
    } catch (TelegramApiException e) {
      e.printStackTrace();
    }
  }

  @PostConstruct
  public void init() throws TelegramApiException {
    TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
    try {
      telegramBotsApi.registerBot(this);
    } catch (TelegramApiException e) {
      e.printStackTrace();
    }
  }

  @Override
  public String getBotUsername() {
    return config.getBotName();
  }

  @Override
  public void onUpdateReceived(Update update) {
    log.info("Update received: {}", update);
    boolean isMessage = update.hasMessage() && update.getMessage().hasText();
    Long chatId;
    User user;

    if (isMessage) {
      Message message = update.getMessage();
      chatId = message.getChatId();
      user = message.getFrom();
    } else if (update.hasCallbackQuery()) {
      CallbackQuery callbackQuery = update.getCallbackQuery();
      chatId = callbackQuery.getMessage().getChatId();
      user = callbackQuery.getFrom();
    } else {
      return;
    }

    final UserDto userDto = UserDto.builder()
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

    kafkaTemplate.send(messagesToExercisesTopicName, chatId, updateDto.build());
    log.info("Message sent to {} kafka topic : {}", messagesToExercisesTopicName, updateDto.build());
  }

  @EventListener
  @Async
  @Override
  public void handleTelegramActionEvent(TelegramActionEvent event) {
    executeTelegramAction(event.action());
  }

  private void executeTelegramAction(BotApiMethodInterface action) {
    try {
      action.accept(this);
    } catch (TelegramApiException e) {
      e.printStackTrace();
    }
  }
}
