import java.util.List;

public class Plane implements Runnable{
    private enum State{
        GATE(), FLYING(), LEAVING(), ARRIVING()
    }
    private State state;
    private List<FlySchedule> schedule;
    private boolean emergency;
    private final double weight, consumptionRate;
    private double extraWeight;
    private int nrOfPassengers, id;
    private double fuel;
    public boolean isEmergency() {
        return this.emergency;
    }
    Plane(List<FlySchedule> schedule, double weight, double consumptionRate) {
        this.schedule = schedule;
        this.weight = weight;
        this.consumptionRate = consumptionRate;
        this.state = State.GATE;
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
                        int runwayId = -1, taxiwayId = -1;
                        while (runwayId == -1 || taxiwayId == -1) {
                            runwayId = tower.acquire(0, this.id);
                            taxiwayId = tower.acquire(2, this.id);
                        }
                        System.out.println("Runway " + runwayId + ", Taxiway" + taxiwayId);
                        Thread.sleep(1000);
                        this.state = State.FLYING;
                    }
                    case ARRIVING -> {
                        int gateId = -1, runwayId = -1, taxiwayId = -1;
                        while (gateId == -1 || runwayId == -1 || taxiwayId == -1) {
                            if(fuel< safeLevelFuel)
                                this.emergency = true;
                            gateId = gateManager.acquire(this.id);
                            runwayId = tower.acquire(0, this.id);
                            taxiwayId = tower.acquire(1, this.id);
                            Thread.sleep((int) (updateInterval* Clock.getScale()* 1000));
                            updateFuel(updateInterval);
                        }
                        System.out.println("Gate " + gateId + ", Runway " + runwayId + ", Taxiway" + taxiwayId);
                        Thread.sleep(1000);
                        this.schedule.removeFirst();
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
}
