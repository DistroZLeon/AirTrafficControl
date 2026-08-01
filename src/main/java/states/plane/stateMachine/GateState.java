package states.plane.stateMachine;
import simulator.airport.Clock;
import simulator.airport.GateManager;
import simulator.plane.CargoPlane;
import simulator.plane.PassengerPlane;
import simulator.plane.Plane;
import states.plane.FlySchedule;
import states.plane.RadarData;

import java.util.Map;

public class GateState implements PlaneState {
    @Override
    public void execute(Plane plane) throws InterruptedException{
        FlySchedule nextFlight= plane.getFlyingPlan();
        GateManager gateManager= GateManager.getInstance();
        plane.loadAircraft(nextFlight);
        plane.calculateFuel(nextFlight);

        int timeToTakeoff = (int) (nextFlight.getTimeOfDeparture() - Clock.getCurrentTime() * (Clock.getScale() * 1e9));
        while (timeToTakeoff > 0) {
            plane.broadcastUpdate();
            long sleepTime = Math.min(1000, timeToTakeoff);
            Thread.sleep(sleepTime);
            timeToTakeoff -= sleepTime;
        }

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

    @Override
    public Map<String, Object> getStateDetails(Plane plane) {
        return Map.of("status", "BOARDING");
    }
}
