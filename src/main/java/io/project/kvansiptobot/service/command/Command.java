package io.project.kvansiptobot.service.command;

import io.project.kvansiptobot.service.wrapper.BotApiMethodInterface;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public abstract class Command {

  public abstract boolean supports(Update update);

  public abstract BotApiMethodInterface process(Update update);
}
