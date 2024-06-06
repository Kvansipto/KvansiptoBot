package kvansipto.telegram.microservice.services.wrapper;

import java.util.ArrayList;
import java.util.List;
import kvansipto.telegram.microservice.services.TelegramBot;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class BotApiMethodWrapper implements BotApiMethodInterface {

  private final List<BotApiMethodInterface> actions;

  public BotApiMethodWrapper() {
    this.actions = new ArrayList<>();
  }

  public void addAction(BotApiMethodInterface action) {
    this.actions.add(action);
  }

  @Override
  public void accept(TelegramBot bot) throws TelegramApiException {
    for (BotApiMethodInterface action : actions) {
      action.accept(bot);
    }
  }
}
