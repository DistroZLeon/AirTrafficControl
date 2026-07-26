package states.plane.stateMachine;
import simulator.ControlTower;
import simulator.plane.Plane;
import states.plane.LandingClearance;

public class LeavingState implements PlaneState {
    @Override
    public void execute(Plane plane) throws InterruptedException{
        plane.resetClearance();
        ControlTower tower= ControlTower.getInstance();
        tower.updateTakeoffQueue(plane);

        while(plane.getClearance()== null)
            plane.waitForClearance();

        LandingClearance clearance= plane.getClearance();
        System.out.println("Plane " + plane.getId() + " Takeoff with "+ String.format("%.2f", plane.getFuel())+ " fuel: Runway-" + clearance.runwayId() + ", Taxiway-" + clearance.taxiwayId());

        Thread.sleep(1000);
        tower.finishedTakeoff(clearance.runwayId(), clearance.taxiwayId(), clearance.gateId());

        plane.setPlaneState(new FlyingState());
    }
}
