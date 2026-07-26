package simulator.plane;

import observers.Observer;
import simulator.Clock;
import simulator.ControlTower;
import simulator.GateManager;
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

public abstract class Plane implements Runnable, Observer, AircraftInterface {
    private enum State{
        GATE(), FLYING(), LEAVING(), ARRIVING()
    }
    private State state;
    private final List<FlySchedule> schedule;

    private volatile EventState emergency= null;
    private volatile double weatherDrag= 1.0, weatherFuelMulti= 1.0, emergencyDrain= 0.0;
    private volatile LandingClearance clearance= null;

    protected final double weight;
    private final double baseConsumptionRate;
    private final int id;
    private static int index= 1;
    private double fuel;

    public Plane(List<FlySchedule> schedule, double weight, double baseConsumptionRate) {
        this.schedule = schedule;
        this.weatherDrag = 1.0;
        this.weight = weight;
        this.baseConsumptionRate = baseConsumptionRate;
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

    protected abstract double getTotalWeight();
    protected abstract void loadAircraft(FlySchedule schedule);

    private double consumptionPerTimeUnit(){
        double activeConsumptionRate= (this.baseConsumptionRate+ this.emergencyDrain)* this.weatherFuelMulti;
        return (activeConsumptionRate* getTotalWeight())* this.weatherDrag;
    }

    public double getRemainingTimeFlight(){
        double currentRate= this.consumptionPerTimeUnit();
        if(currentRate<= 0) return Double.MAX_VALUE;

        return fuel/ currentRate;
    }

    private void updateFuel(double timePassed){
        this.fuel-= consumptionPerTimeUnit()* timePassed;
    }

    public void calculateFuel(FlySchedule plan){
        double duration= plan.getTimeOfArrival()- plan.getTimeOfDeparture();
        double baseConsumption= this.baseConsumptionRate* getTotalWeight();
        double baseFuel= duration* baseConsumption;

        double weatherReserve= (Math.random()+2.5)* baseFuel;
        double holdingReserve= (Math.random()+15.0)* baseConsumption ;

        this.fuel= baseFuel+ weatherReserve + holdingReserve;
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
            this.weatherFuelMulti= weather.consumptionRate();
        }
    }

    private void handleEvent(Object arg){
        if(arg instanceof EventState event){
            if(event.targetId()!= this.id) return;
            this.emergency= event;
            if(event.fuelDrainRate()!= 0) this.emergencyDrain= event.fuelDrainRate();
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
                        this.loadAircraft(nextFlight);
                        this.calculateFuel(nextFlight);

                        int timeToTakeoff = (int) (nextFlight.getTimeOfDeparture() - Clock.getCurrentTime() * (Clock.getScale() * 1e9));
                        if(timeToTakeoff> 0) Thread.sleep(timeToTakeoff);

                        if(nextFlight.getStartingPoint().equalsIgnoreCase(gateManager.getAirportName())) {
                            if (gateManager.getGateId(this.id)== -1) {
                                System.out.println("Error: Plane "+ id+ " has no physical gate!");
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
                        System.out.println("Plane " + id + " Takeoff with "+ String.format("%.2f", this.fuel)+ " fuel: Runway-" + clearance.runwayId() + ", Taxiway-" + clearance.taxiwayId());
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
                                    tower.changePriority(this);
                                    if (this.fuel <= 0) {
                                        System.out.println("Plane " + id + " ran out of fuel with a consumption of "+ String.format("%.2f", this.consumptionPerTimeUnit()));
                                        tower.removeFromLandingQueue(this);
                                        this.schedule.clear();
                                        return;
                                    }
                                }
                            }
                        }

                        if(clearance!= null){
                            System.out.println("Plane " + id + " Landed with "+ String.format("%.2f", this.fuel)+ " fuel: Gate-" + clearance.gateId() + ", Runway-" + clearance.runwayId() + ", Taxiway-" + clearance.taxiwayId());
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
                            this.schedule.removeFirst();
                            this.state = State.GATE;
                        }
                    }
                    default -> {}
                }
            }
        }
        catch (InterruptedException e) {
            System.out.println("Plane " + id + " comms interrupted");
            Thread.currentThread().interrupt();
        }
        finally{
            WeatherEngine.getInstance().removeObserver(this);
            EventGenerator.getInstance().removeObserver(this);

            GateManager gateManager = GateManager.getInstance();
            int currentGateId = gateManager.getGateId(id);
            if (currentGateId != -1) {
                gateManager.release(currentGateId);
            }

            System.out.println("Plane " + id + " ended its schedule for today!");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Plane plane = (Plane) o;
        return Double.compare(weight, plane.weight) == 0 && Double.compare(baseConsumptionRate, plane.baseConsumptionRate) == 0 && id == plane.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(weight, baseConsumptionRate, id);
    }
}
