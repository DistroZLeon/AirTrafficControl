package simulator.plane;
import states.plane.FlySchedule;
import java.util.List;

public class PassengerPlane extends Plane{
    private double luggageWeight;
    private int nrOfPassengers;
    public PassengerPlane(List<FlySchedule> schedule, double weight, double baseConsumptionRate) {
        super(schedule,weight,baseConsumptionRate);
    }

    @Override
    protected double getTotalWeight(){
        return this.weight + this.luggageWeight+ (80* this.nrOfPassengers);
    }

    @Override
    protected void loadAircraft(FlySchedule flySchedule){
        this.luggageWeight= flySchedule.getCargoWeight();
        this.nrOfPassengers= flySchedule.getNrOfPassengers();
    }
}
