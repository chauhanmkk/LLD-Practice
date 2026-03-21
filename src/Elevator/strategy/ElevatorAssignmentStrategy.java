package Elevator.strategy;

import Elevator.model.Elevator;
import Elevator.model.Request;

import java.util.List;

public interface ElevatorAssignmentStrategy {
     Elevator assignElevator(List<Elevator> elevatorList, Request request);
}
