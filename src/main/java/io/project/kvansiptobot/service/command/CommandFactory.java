package io.project.kvansiptobot.service.command;

import io.project.kvansiptobot.service.command.MuscleCommand;
import io.project.kvansiptobot.model.MuscleGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
public class CommandFactory {

  @Autowired
  private ApplicationContext applicationContext;
  @Autowired
  private MuscleCommand muscleCommand;

  public MuscleCommand createMuscleCommand(MuscleGroup muscleGroup) {
    MuscleCommand muscleCommand = applicationContext.getBean(MuscleCommand.class);
    muscleCommand.setMuscleGroup(muscleGroup);
    return muscleCommand;
  }
}
