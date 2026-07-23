import observers.Observer;
import states.event.EventState;
import states.plane.AircraftInterface;
import states.plane.FlySchedule;
import states.plane.LandingClearance;
import states.weather.WeatherState;
import subjects.EventGenerator;
import subjects.Subject;
import subjects.WeatherEngine;

import java.util.List;
import java.util.Objects;

public class Plane implements Runnable, Observer, AircraftInterface {
    private enum State{
        GATE(), FLYING(), LEAVING(), ARRIVING()
    }
    private State state;
    private final List<FlySchedule> schedule;

    private volatile EventState emergency= null;
    private volatile double weatherDrag;
    private volatile LandingClearance clearance= null;

    private final double weight;
    private double extraWeight, consumptionRate;
    private int nrOfPassengers;
    private final int id;
    private static int index= 1;
    private double fuel;

    Plane(List<FlySchedule> schedule, double weight, double consumptionRate) {
        this.schedule = schedule;
        this.weatherDrag = 1.0;
        this.weight = weight;
        this.consumptionRate = consumptionRate;
        this.state = State.GATE;
        this.id= index++;
    }

    public EventState getEmergency() {
        return this.emergency;
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

    private double consumptionPerTimeUnit(){
        double totalWeight= this.weight+ this.extraWeight+ 80* this.nrOfPassengers;
        return (this.consumptionRate* totalWeight)*this.weatherDrag;
    }

    public double getRemainingTimeFlight(){
        double currentRate= this.consumptionPerTimeUnit();
        if(currentRate<= 0) return Double.MAX_VALUE;

        return fuel/ currentRate;
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
    public void update(Subject source, Object arg) {
        if(source instanceof WeatherEngine){
            this.handleWeatherChange(arg);
        } else if (source instanceof EventGenerator) {
            this.handleEvent(arg);
        }
    }

    private void handleWeatherChange(Object arg){
        if(arg instanceof WeatherState weather){
            this.weatherDrag= weather.windStrength();
            this.consumptionRate= weather.consumptionRate();
        }
    }

    private void handleEvent(Object arg){
        if(arg instanceof EventState event){
            if(event.targetId()!= this.id) return;
            this.emergency= event;
            if(event.fuelDrainRate()!= 0) this.consumptionRate+= event.fuelDrainRate();
            ControlTower.getInstance().changePriority(this);
        }
    }

    public synchronized void grantClearance(LandingClearance clearance){
        this.clearance= clearance;
        this.notify();
    }

    @Override
    public void run() {
        try {
            ControlTower tower = ControlTower.getInstance();
            GateManager gateManager = GateManager.getInstance();
            final double updateInterval= .1;

            while (!schedule.isEmpty()) {
                FlySchedule nextFlight = this.schedule.getFirst();

                switch (this.state) {
                    case GATE -> {
                        this.extraWeight = nextFlight.getCargoWeight();
                        this.nrOfPassengers = nextFlight.getNrOfPassengers();
                        int timeToTakeoff = (int) (nextFlight.getTimeOfDeparture() - Clock.getCurrentTime() * (Clock.getScale() * 1e9));
                        if(timeToTakeoff> 0) Thread.sleep(timeToTakeoff);

                        if(nextFlight.getStartingPoint().equalsIgnoreCase(gateManager.getAirportName())) {
                            if (gateManager.getGateId(this.id)== -1) {
                                System.out.println("Error: Plane " + id + " has no physical gate! Despawning.");
                                this.schedule.clear();
                                return;
                            }

                            this.state = State.LEAVING;
                        }
                        else{
                            Thread.sleep(1000);
                            this.state= State.FLYING;
                        }
                    }
                    case LEAVING -> {
                        this.clearance= null;
                        tower.updateTakeoffQueue(this);
                        synchronized (this) {
                            while (clearance == null) {
                                this.wait(1000);
                            }
                        }
                        System.out.println("Plane " + id + " Takeoff: Runway " + clearance.runwayId() + ", Taxiway " + clearance.taxiwayId());
                        Thread.sleep(1000);
                        tower.finishedTakeoff(clearance.runwayId(), clearance.taxiwayId(), clearance.gateId());
                        this.state= State.FLYING;
                    }
                    case ARRIVING -> {
                        this.clearance= null;
                        tower.updateLandingQueue(this);

                        synchronized (this) {
                            while (clearance == null) {
                                this.wait((long) (updateInterval * Clock.getScale() * 1000));


                                if (clearance == null) {
                                    updateFuel(updateInterval);

                                    if (this.fuel <= 0) {
                                        System.out.println("Plane " + id + " ran out of fuel");
                                        tower.removeFromLandingQueue(this);
                                        this.schedule.clear();
                                        return;
                                    }
                                }
                            }
                        }

                        if(clearance!= null){
                            System.out.println("Plane " + id + " Landed: Gate " + clearance.gateId() + ", Runway " + clearance.runwayId() + ", Taxiway " + clearance.taxiwayId());
                            Thread.sleep(1000);
                            this.schedule.removeFirst();
                            tower.finishedLanding(clearance.runwayId(),  clearance.taxiwayId());
                            this.state= State.GATE;
                        }
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
            System.out.println("Plane " + id + " comms interrupted");
            Thread.currentThread().interrupt();
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
