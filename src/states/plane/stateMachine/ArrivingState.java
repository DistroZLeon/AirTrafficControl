package states.plane.stateMachine;
import simulator.Clock;
import simulator.ControlTower;
import simulator.plane.Plane;
import states.plane.LandingClearance;

import javax.naming.ldap.Control;

public class ArrivingState implements PlaneState {
    @Override
    public void execute(Plane plane) throws InterruptedException{
        plane.resetClearance();
        ControlTower tower= ControlTower.getInstance();
        tower.updateLandingQueue(plane);
        double updateInterval= .1;

        while (plane.getClearance() == null) {
            plane.waitForClearance((long) (updateInterval * Clock.getScale() * 1000));

            if (plane.getClearance() == null) {
                plane.updateFuel(updateInterval);
                tower.changePriority(plane);

                if (plane.getFuel() <= 0) {
                    System.out.println("Plane " + plane.getId() + " ran out of fuel with a consumption of "+ String.format("%.2f", plane.consumptionPerTimeUnit()));
                    tower.removeFromLandingQueue(plane);
                    plane.clearSchedule();
                    return;
                }
            }
        }

        LandingClearance clearance= plane.getClearance();
        System.out.println("Plane " + plane.getId() + " Landed with "+ String.format("%.2f", plane.getFuel())+ " fuel: Gate-" + clearance.gateId() + ", Runway-" + clearance.runwayId() + ", Taxiway-" + clearance.taxiwayId());

        Thread.sleep(1000);
        plane.completeCurrentFlight();
        tower.finishedLanding(clearance.runwayId(),  clearance.taxiwayId());

        plane.setPlaneState(new GateState());
    }
}
