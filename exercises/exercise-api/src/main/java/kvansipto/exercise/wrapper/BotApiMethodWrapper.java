package kvansipto.exercise.wrapper;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Getter
public class BotApiMethodWrapper implements BotApiMethodInterface {

  private final List<BotApiMethodInterface> actions;

  public BotApiMethodWrapper() {
    this.actions = new ArrayList<>();
  }

  public void addAction(BotApiMethodInterface action) {
    this.actions.add(action);
  }

  @Override
  public void accept(TelegramLongPollingBot bot) throws TelegramApiException {
    for (BotApiMethodInterface action : actions) {
      action.accept(bot);
    }
  }
}
