package simulator.plane;
import observers.Observer;
import simulator.airport.ControlTower;
import simulator.airport.GateManager;
import states.event.EventState;
import states.plane.AircraftInterface;
import states.plane.FlySchedule;
import states.plane.LandingClearance;
import states.plane.RadarData;
import states.plane.stateMachine.GateState;
import states.plane.stateMachine.PlaneState;
import states.weather.WeatherState;
import subjects.EventGenerator;
import subjects.Subject;
import subjects.WeatherEngine;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public abstract class Plane extends Subject implements Runnable, Observer, AircraftInterface {
    private PlaneState planeState;
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
        this.planeState= new GateState();
        this.id= index++;
    }

    public double getWeight() {
        return weight;
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

    public void clearSchedule(){
        this.schedule.clear();
    }

    public void setPlaneState(PlaneState planeState){
        this.planeState = planeState;
    }

    public void completeCurrentFlight(){
        this.schedule.removeFirst();
    }

    public LandingClearance getClearance(){
        return this.clearance;
    }

    public void resetClearance(){
        this.clearance= null;
    }

    public synchronized void waitForClearance() throws InterruptedException {
        this.wait(1000);
    }

    public synchronized void waitForClearance(long timeout) throws InterruptedException {
        this.wait(timeout);
    }

    public abstract double getTotalWeight();
    public abstract void loadAircraft(FlySchedule schedule);
    public abstract String getPlaneType();
    public abstract int getPassengerCount();
    public abstract double getPayloadWeight();


    public double consumptionPerTimeUnit(){
        double activeConsumptionRate= (this.baseConsumptionRate+ this.emergencyDrain)* this.weatherFuelMulti;
        return (activeConsumptionRate* getTotalWeight())* this.weatherDrag;
    }

    public double getRemainingTimeFlight(){
        double currentRate= this.consumptionPerTimeUnit();
        if(currentRate<= 0) return Double.MAX_VALUE;

        return fuel/ currentRate;
    }

    public void updateFuel(double timePassed){
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

    public void broadcastUpdate(){
        Map<String, Object> stateDetails= this.planeState.getStateDetails(this);

        RadarData radarData= new RadarData(
                this.getId(),
                this.getPlaneType(),
                this.getFlyingPlan().getDestination(),
                this.getFlyingPlan().getStartingPoint(),
                this.getFuel(),
                this.consumptionPerTimeUnit(),
                this.getEmergency() != null ? this.getEmergency().type().toString() : "NONE",
                this.getPayloadWeight(),
                this.getPassengerCount(),
                stateDetails
        );

        notifyObservers(radarData);
    }

    @Override
    public void run() {
        try {
            while(!schedule.isEmpty()&& !Thread.currentThread().isInterrupted())
                this.planeState.execute(this);
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

            RadarData endSignal= new RadarData(
              this.getId(), this.getPlaneType(), "END", "END",
              this.getFuel(), 0.0, "NONE", 0.0,
              0, Map.of("status", "FINISHED"));

            notifyObservers(endSignal);
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
