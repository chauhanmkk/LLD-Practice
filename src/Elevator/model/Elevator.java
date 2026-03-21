package Elevator.model;

import java.util.Collections;
import java.util.PriorityQueue;

public class Elevator {
    int currentFloor;
    ElevatorState elevatorState;
    PriorityQueue<Integer> upPriorityQueue;
    PriorityQueue<Integer> downPQ;

    public int getCurrentFloor() {
        return currentFloor;
    }

    public void setCurrentFloor(int currentFloor) {
        this.currentFloor = currentFloor;
    }

    public ElevatorState getElevatorState() {
        return elevatorState;
    }

    public void setElevatorState(ElevatorState elevatorState) {
        this.elevatorState = elevatorState;
    }

    public PriorityQueue<Integer> getUpPriorityQueue() {
        return upPriorityQueue;
    }

    public void setUpPriorityQueue(PriorityQueue<Integer> upPriorityQueue) {
        this.upPriorityQueue = upPriorityQueue;
    }

    public PriorityQueue<Integer> getDownPQ() {
        return downPQ;
    }

    public void setDownPQ(PriorityQueue<Integer> downPQ) {
        this.downPQ = downPQ;
    }

    public Elevator() {
        this.currentFloor = 0;
        this.elevatorState = ElevatorState.IDLE;
        this.upPriorityQueue = new PriorityQueue<>();
        this.downPQ = new PriorityQueue<>(Collections.reverseOrder());
    }

    public void addRequest(Request request) {
        if(request.getTargetFloor() > currentFloor) {
            upPriorityQueue.add(request.targetFloor);
        } else {
            downPQ.add(request.targetFloor);
        }
    }
}
