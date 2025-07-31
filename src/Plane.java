import java.util.List;
import java.util.Objects;

public class Plane implements Runnable{
    private enum State{
        GATE(), FLYING(), LEAVING(), ARRIVING()
    }
    private State state;
    private final List<FlySchedule> schedule;
    private boolean emergency;
    private final double weight, consumptionRate;
    private double extraWeight;
    private int nrOfPassengers;
    private final int id;
    private static int index= 1;
    private double fuel;
    public boolean isEmergency() {
        return this.emergency;
    }
    Plane(List<FlySchedule> schedule, double weight, double consumptionRate) {
        this.schedule = schedule;
        this.weight = weight;
        this.consumptionRate = consumptionRate;
        this.state = State.GATE;
        this.id= index++;
    }
    private double consumptionPerTimeUnit(){
        double totalWeight= this.weight+ this.extraWeight+ 80* this.nrOfPassengers;
        return this.consumptionRate* totalWeight;
    }
    private void updateFuel(double timePassed){
        this.fuel-= consumptionPerTimeUnit()* timePassed;
    }

    public double calculateFuel(FlySchedule plan){
        double duration= plan.getTimeOfArrival()- plan.getTimeOfDeparture();
        double baseFuel= duration* consumptionPerTimeUnit();
        this.fuel= baseFuel* (1+ Math.random());
        return .02* baseFuel;
    }

    public int getId(){
        return this.id;
    }

    public double getFuel(){
        return this.fuel;
    }

    public FlySchedule getFlyingPlan(){
        return this.schedule.getFirst();
    }

    @Override
    public void run() {
        try {
            ControlTower tower = ControlTower.getInstance();
            GateManager gateManager = GateManager.getInstance();
            final double updateInterval= .1;
            double safeLevelFuel= 0;
            while (!schedule.isEmpty()) {
                FlySchedule nextFlight = this.schedule.getFirst();
                switch (this.state) {
                    case GATE -> {
                        this.extraWeight = nextFlight.getCargoWeight();
                        this.nrOfPassengers = nextFlight.getNrOfPassengers();
                        safeLevelFuel= calculateFuel(nextFlight);
                        int timeToTakeoff = (int) (nextFlight.getTimeOfDeparture() - Clock.getCurrentTime() * (Clock.getScale() * 1e9));
                        Thread.sleep(timeToTakeoff);

                        if(nextFlight.getStartingPoint().equalsIgnoreCase(gateManager.getAirportName()))
                            this.state = State.LEAVING;
                        else{
                            Thread.sleep(1000);
                            this.state= State.FLYING;
                        }
                    }
                    case LEAVING -> {
                        int runwayId = -1, taxiwayId = -1, gateId= gateManager.getGateId(this.id);
                        while (runwayId == -1 || taxiwayId == -1) {
                            tower.updateTakeoffQueue(this);
                            LandingClearance clearance= tower.takeoffClearance(this);
                            if(clearance!= null) {
                                runwayId = clearance.runwayId();
                                taxiwayId = clearance.taxiwayId();
                            }
                        }
                        System.out.println("Runway " + runwayId + ", Taxiway" + taxiwayId);
                        Thread.sleep(1000);
                        tower.finishedTakeoff(runwayId, taxiwayId, gateId);
                        this.state = State.FLYING;
                    }
                    case ARRIVING -> {
                        int gateId = -1, runwayId = -1, taxiwayId = -1;
                        while (gateId == -1 || runwayId == -1 || taxiwayId == -1) {
                            tower.updateLandingQueue(this);
                            LandingClearance clearance = tower.landingRequest(this);
                            if (clearance != null) {
                                gateId= clearance.gateId();
                                runwayId= clearance.runwayId();
                                taxiwayId= clearance.taxiwayId();
                            }
                            else {
                                Thread.sleep((int) (updateInterval * Clock.getScale() * 1000));
                                updateFuel(updateInterval);
                                if (fuel < safeLevelFuel)
                                    this.emergency = true;
                            }
                        }
                        System.out.println("Gate " + gateId + ", Runway " + runwayId + ", Taxiway" + taxiwayId);
                        Thread.sleep(1000);
                        this.schedule.removeFirst();
                        tower.finishedLanding(runwayId, taxiwayId);
                        this.state = State.GATE;
                    }
                    case FLYING -> {
                        double timePassed= 0, flightDuration= nextFlight.getTimeOfArrival() - nextFlight.getTimeOfDeparture();

                        while (timePassed < flightDuration) {
                            Thread.sleep((int) (updateInterval* Clock.getScale()* 1000));
                            updateFuel(updateInterval);
                            timePassed += updateInterval;
                        }
                        if(nextFlight.getDestination().equalsIgnoreCase(tower.getAirportName()))
                            this.state = State.ARRIVING;
                        else{
                            Thread.sleep(1000);
                            this.state = State.GATE;
                        }
                    }
                    default -> {
                    }
                }
            }
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Plane plane = (Plane) o;
        return Double.compare(weight, plane.weight) == 0 && Double.compare(consumptionRate, plane.consumptionRate) == 0 && id == plane.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(weight, consumptionRate, id);
    }
}
