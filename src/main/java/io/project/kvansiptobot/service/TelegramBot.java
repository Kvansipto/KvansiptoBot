package io.project.kvansiptobot.service;

import io.project.kvansiptobot.config.BotConfig;
import io.project.kvansiptobot.service.command.Command;
import io.project.kvansiptobot.service.command.menu.MainMenuCommand;
import io.project.kvansiptobot.service.wrapper.BotApiMethodInterface;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Component
public class TelegramBot extends TelegramLongPollingBot {

  final BotConfig config;

  private final List<Command> commandList;

  public TelegramBot(List<Command> commandList, BotConfig config) {
    this.config = config;
    this.commandList = commandList;
    List<BotCommand> mainMenuCommandList = new ArrayList<>();

    commandList.stream().filter(MainMenuCommand.class::isInstance).forEach(
        clazz -> mainMenuCommandList.add(new BotCommand(clazz.getClass().getAnnotation(Component.class).value(),
            ((MainMenuCommand) clazz).explanation())));
    try {
      this.execute(new SetMyCommands(mainMenuCommandList, new BotCommandScopeDefault(), null));
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

    if (update.hasMessage() && update.getMessage().hasText() || update.hasCallbackQuery()) {

      BotApiMethodInterface sendMessage = commandList.stream()
          .filter(clazz -> clazz.supports(update))
          .findFirst()
          .map(m -> m.process(update))
          .orElseThrow(RuntimeException::new);

      executeTelegramAction(sendMessage);
    }
  }

  public void executeTelegramAction(BotApiMethodInterface action) {
    try {
      action.accept(this);
    } catch (TelegramApiException e) {
      e.printStackTrace();
    }
  }

  @Override
  public String getBotToken() {
    return config.getBotToken();
  }
}
