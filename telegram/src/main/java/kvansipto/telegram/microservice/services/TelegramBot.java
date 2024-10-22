package kvansipto.telegram.microservice.services;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import kvansipto.telegram.microservice.config.BotConfig;
import kvansipto.telegram.microservice.services.command.Command;
import kvansipto.telegram.microservice.services.command.menu.HelpCommand;
import kvansipto.telegram.microservice.services.command.menu.MainMenuCommand;
import kvansipto.telegram.microservice.services.dto.TelegramActionEvent;
import kvansipto.telegram.microservice.services.wrapper.BotApiMethodInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Component
public class TelegramBot extends TelegramLongPollingBot implements TelegramBotInterface {

  final BotConfig config;
  private static final long INACTIVITY_TIMEOUT = TimeUnit.MINUTES.toMillis(5);

  private final List<Command> commandList;
  public static String HELP_TEXT;
  private final Map<Long, ExecutorService> userExecutors;
  private final Map<Long, Long> userLastInteractionTime;
  private final ScheduledExecutorService scheduler;

  @Autowired
  public TelegramBot(List<Command> commandList, BotConfig config) {
    super(config.getBotToken());
    this.config = config;
    this.commandList = commandList;
    this.userExecutors = new ConcurrentHashMap<>();
    this.userLastInteractionTime = new ConcurrentHashMap<>();
    this.scheduler = Executors.newSingleThreadScheduledExecutor();
    List<BotCommand> mainMenuCommandList = new ArrayList<>();

    StringBuilder helpText = new StringBuilder("This bot was made by Kvansipto\n\n");

    commandList.stream().filter(MainMenuCommand.class::isInstance).forEach(
        clazz -> {
          mainMenuCommandList.add(new BotCommand(clazz.getClass().getAnnotation(Component.class).value(),
              ((MainMenuCommand) clazz).explanation()));
          helpText
              .append("Type ")
              .append(clazz.getClass().getAnnotation(Component.class).value())
              .append(" ")
              .append(
                  clazz instanceof HelpCommand ? " to see this message again" : ((MainMenuCommand) clazz).explanation())
              .append("\n\n");
        });
    HELP_TEXT = helpText.toString();
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
    scheduler.scheduleAtFixedRate(this::terminateInactiveUserThread, 1, 1, TimeUnit.MINUTES);
  }

  @PreDestroy
  public void destroy() {
    shutDownAndAwaitTermination(scheduler);
    userExecutors.values().forEach(this::shutDownAndAwaitTermination);
  }

  @Override
  public String getBotUsername() {
    return config.getBotName();
  }

  @Override
  public void onUpdateReceived(Update update) {
    if (update.hasMessage() && update.getMessage().hasText() || update.hasCallbackQuery()) {
      Long userId =
          update.hasMessage() ? update.getMessage().getFrom().getId() : update.getCallbackQuery().getFrom().getId();

      ExecutorService userExecutor = userExecutors.computeIfAbsent(userId, id -> Executors.newSingleThreadExecutor());

      userExecutor.submit(() -> {
        BotApiMethodInterface sendMessage = commandList.stream()
            .filter(clazz -> clazz.supports(update))
            .findFirst()
            .map(m -> m.process(update))
            .orElseThrow(RuntimeException::new);

        executeTelegramAction(sendMessage);
      });
    }
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

  private void terminateInactiveUserThread() {
    long now = System.currentTimeMillis();
    userExecutors.entrySet().removeIf(entry -> {
          Long userId = entry.getKey();
          ExecutorService executor = entry.getValue();
          Long lastInteractionTime = userLastInteractionTime.get(userId);

          if (lastInteractionTime != null && now - lastInteractionTime > INACTIVITY_TIMEOUT) {
            shutDownAndAwaitTermination(executor);
            userLastInteractionTime.remove(userId);
            return true;
          }
          return false;
        }
    );
  }

  private void shutDownAndAwaitTermination(ExecutorService executor) {
    executor.shutdown();
    try {
      if (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
        executor.shutdownNow();
      }
    } catch (InterruptedException e) {
      executor.shutdownNow();
      Thread.currentThread().interrupt();
    }
  }
}
