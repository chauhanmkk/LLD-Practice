package Elevator.strategy;

import Elevator.model.Elevator;
import Elevator.model.ElevatorState;
import Elevator.model.Request;
import Elevator.model.RequestType;

import java.util.List;

public class NearestElevatorAllocationStrategy implements ElevatorAssignmentStrategy {

    @Override
    public Elevator assignElevator(List<Elevator> elevatorList, Request request) {
        if(request.getRequestType().equals(RequestType.HALL)) {
            Elevator assignElevator = null;
            for(Elevator elevator : elevatorList) {
                int floorDiff = Math.abs(elevator.getCurrentFloor() - request.getTargetFloor());
                ElevatorState direction = elevator.getElevatorState();
                if(assignElevator == null) assignElevator = elevator;
                else {
                    int assignElevatorDiff = Math.abs(assignElevator.getCurrentFloor() - request.getTargetFloor());
                     if(floorDiff < assignElevatorDiff) {
                         assignElevator = elevator;
                     }
                }
            }
            return assignElevator;
        }
        return null;
    }
}
