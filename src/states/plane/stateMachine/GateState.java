package states.plane.stateMachine;
import simulator.Clock;
import simulator.GateManager;
import simulator.plane.Plane;
import states.plane.FlySchedule;

public class GateState implements PlaneState {
    @Override
    public void execute(Plane plane) throws InterruptedException{
        FlySchedule nextFlight= plane.getFlyingPlan();
        GateManager gateManager= GateManager.getInstance();
        plane.loadAircraft(nextFlight);
        plane.calculateFuel(nextFlight);

        int timeToTakeoff = (int) (nextFlight.getTimeOfDeparture() - Clock.getCurrentTime() * (Clock.getScale() * 1e9));
        if(timeToTakeoff> 0) Thread.sleep(timeToTakeoff);

        if(nextFlight.getStartingPoint().equalsIgnoreCase(gateManager.getAirportName())) {
            if (gateManager.getGateId(plane.getId())== -1) {
                System.out.println("Error: Plane "+ plane.getId()+ " has no physical gate!");
                plane.clearSchedule();
                return;
            }

            plane.setPlaneState(new LeavingState());
        }
        else{
            Thread.sleep(1000);
            plane.setPlaneState(new FlyingState());
        }
    }
}
