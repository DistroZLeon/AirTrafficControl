package states.plane.stateMachine;

import simulator.Clock;
import simulator.ControlTower;
import simulator.plane.Plane;
import states.plane.FlySchedule;

public class FlyingState implements PlaneState {
    @Override
    public void execute(Plane plane) throws InterruptedException{
        FlySchedule nextFlight= plane.getFlyingPlan();
        ControlTower tower= ControlTower.getInstance();
        double updateInterval= .1;
        double timePassed= 0, flightDuration= nextFlight.getTimeOfArrival() - nextFlight.getTimeOfDeparture();

        while (timePassed < flightDuration) {
            Thread.sleep((long) (updateInterval* Clock.getScale()* 1000));
            plane.updateFuel(updateInterval);
            timePassed += updateInterval;
        }
        if(nextFlight.getDestination().equalsIgnoreCase(tower.getAirportName()))
            plane.setPlaneState(new ArrivingState());
        else{
            Thread.sleep(1000);
            plane.completeCurrentFlight();
            plane.setPlaneState(new GateState());
        }
    }
}
