package Elevator.controller;

import Elevator.model.Elevator;
import Elevator.model.Request;
import Elevator.model.RequestType;
import Elevator.strategy.ElevatorAssignmentStrategy;

import java.util.List;

public class ElevatorController {
    List<Elevator> elevatorList;
    ElevatorAssignmentStrategy elevatorAssignmentStrategy;

    public ElevatorController(List<Elevator> elevatorList, ElevatorAssignmentStrategy elevatorAssignmentStrategy) {
        this.elevatorList = elevatorList;
        this.elevatorAssignmentStrategy = elevatorAssignmentStrategy;
    }

    void addHallRequest(Request request) {
        if(request.getRequestType().equals(RequestType.HALL)) {
            Elevator elevator = elevatorAssignmentStrategy.assignElevator(elevatorList, request);
        }
    }

    void addCabinRequest(Request request, Elevator elevator) {
        elevator.addRequest(request);
    }
}
