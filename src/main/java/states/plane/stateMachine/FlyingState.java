package states.plane.stateMachine;
import simulator.airport.Clock;
import simulator.airport.ControlTower;
import simulator.plane.Plane;
import states.plane.FlySchedule;
import java.util.Map;

public class FlyingState implements PlaneState {
    private double timePassed = 0;
    private double flightDuration = 0;

    @Override
    public void execute(Plane plane) throws InterruptedException{
        FlySchedule nextFlight= plane.getFlyingPlan();
        ControlTower tower= ControlTower.getInstance();
        double updateInterval= .1;
        this.timePassed= 0;
        this.flightDuration= nextFlight.getTimeOfArrival() - nextFlight.getTimeOfDeparture();

        while (timePassed < flightDuration) {
            plane.broadcastUpdate();
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

    @Override
    public Map<String, Object> getStateDetails(Plane plane) {
        return Map.of(
                "status", "EN_ROUTE",
                "remainingTimeFlight", plane.getRemainingTimeFlight(),
                "timeOfArrival", plane.getFlyingPlan().getTimeOfArrival(),
                "timePassed", this.timePassed,
                "flightDuration", this.flightDuration
        );
    }
}
