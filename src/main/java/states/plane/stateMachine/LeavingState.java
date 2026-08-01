package states.plane.stateMachine;
import simulator.airport.ControlTower;
import simulator.plane.CargoPlane;
import simulator.plane.PassengerPlane;
import simulator.plane.Plane;
import states.plane.LandingClearance;
import states.plane.RadarData;

import java.util.Map;

public class LeavingState implements PlaneState {
    @Override
    public void execute(Plane plane) throws InterruptedException{
        plane.resetClearance();
        ControlTower tower= ControlTower.getInstance();
        tower.updateTakeoffQueue(plane);

        while(plane.getClearance()== null) {
            plane.broadcastUpdate();
            plane.waitForClearance();
        }

        LandingClearance clearance= plane.getClearance();
        System.out.println("Plane " + plane.getId() + " Takeoff with "+ String.format("%.2f", plane.getFuel())+ " fuel: Runway-" + clearance.runwayId() + ", Taxiway-" + clearance.taxiwayId());

        Thread.sleep(1000);
        tower.finishedTakeoff(clearance.runwayId(), clearance.taxiwayId(), clearance.gateId());

        plane.setPlaneState(new FlyingState());
    }

    @Override
    public Map<String, Object> getStateDetails(Plane plane) {
        return Map.of(
                "status", plane.getClearance() == null ? "WAITING_TAKEOFF_CLEARANCE" : "IN_TRANSIT_TO_LEAVE"
        );
    }
}
