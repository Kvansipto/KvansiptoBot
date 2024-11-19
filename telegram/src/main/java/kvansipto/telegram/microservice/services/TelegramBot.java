package kvansipto.telegram.microservice.services;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import kvansipto.telegram.microservice.config.BotConfig;
import kvansipto.telegram.microservice.services.command.CommandFactory;
import kvansipto.telegram.microservice.services.command.menu.MainMenuCommand;
import kvansipto.telegram.microservice.services.dto.TelegramActionEvent;
import kvansipto.telegram.microservice.services.wrapper.BotApiMethodInterface;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class TelegramBot extends TelegramLongPollingBot {

  private final BotConfig config;
  private static final long INACTIVITY_TIMEOUT = TimeUnit.MINUTES.toMillis(5);

  private final CommandFactory commands;
  private final Map<Long, ExecutorService> userExecutors;
  private final Map<Long, Long> userLastInteractionTime;
  private final ScheduledExecutorService scheduler;

  @PostConstruct
  public void init() throws TelegramApiException {
    TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
    var mainMenuCommandList = commands.getCommandList().stream()
            .filter(MainMenuCommand.class::isInstance)
            .map(c -> (MainMenuCommand) c)
            .map(c -> new BotCommand(c.getClass().getAnnotation(Component.class).value(), c.explanation()))
            .toList();
    this.execute(new SetMyCommands(mainMenuCommandList, new BotCommandScopeDefault(), null));
    telegramBotsApi.registerBot(this);
    scheduler.scheduleAtFixedRate(this::terminateInactiveUserThread, 1, 1, TimeUnit.MINUTES);
  }

  @Override
  public String getBotUsername() {
    return config.getBotName();
  }

  @Override
  public void onUpdateReceived(Update update) {
    if (update.hasMessage() && update.getMessage().hasText() || update.hasCallbackQuery()) {
      Long userId = update.hasMessage()
              ? update.getMessage().getFrom().getId()
              : update.getCallbackQuery().getFrom().getId();

      ExecutorService userExecutor = userExecutors.computeIfAbsent(userId, id -> Executors.newSingleThreadExecutor());

      userExecutor.submit(() -> {
        BotApiMethodInterface sendMessage = commands.getCommandList().stream()
            .filter(clazz -> clazz.supports(update))
            .findFirst()
            .map(m -> m.process(update))
            .orElseThrow(RuntimeException::new);

          try {
              sendMessage.accept(this);
          } catch (TelegramApiException e) {
              throw new RuntimeException(e);
          }
      });
    }
  }

  @EventListener
  @Async
  public void handleTelegramActionEvent(TelegramActionEvent event) throws TelegramApiException {
    BotApiMethodInterface action = event.action();
    action.accept(this);
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

  @PreDestroy
  public void destroy() {
    shutDownAndAwaitTermination(scheduler);
    userExecutors.values().forEach(this::shutDownAndAwaitTermination);
  }
}
