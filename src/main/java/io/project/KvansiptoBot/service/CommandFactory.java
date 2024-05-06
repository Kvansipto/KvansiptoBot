package io.project.KvansiptoBot.service;

import io.project.KvansiptoBot.model.MuscleCommand;
import io.project.KvansiptoBot.model.MuscleGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
public class CommandFactory {

  @Autowired
  private ApplicationContext applicationContext;

  public MuscleCommand createMuscleCommand(MuscleGroup muscleGroup) {
    MuscleCommand muscleCommand = applicationContext.getBean(MuscleCommand.class);
    muscleCommand.setMuscleGroup(muscleGroup);
    return muscleCommand;
  }
}
