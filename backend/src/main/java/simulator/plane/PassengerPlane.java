package simulator.plane;
import states.plane.FlySchedule;
import java.util.List;

public class PassengerPlane extends Plane{
    private double luggageWeight;
    private int nrOfPassengers;
    public PassengerPlane(List<FlySchedule> schedule, double weight, double baseConsumptionRate) {
        super(schedule,weight,baseConsumptionRate);
    }

    public String getPlaneType() {return "PASSENGER";}
    @Override
    public double getPayloadWeight() {return this.luggageWeight;}
    @Override
    public int getPassengerCount(){return nrOfPassengers;}

    @Override
    public double getTotalWeight(){
        return this.weight + this.luggageWeight+ (80* this.nrOfPassengers);
    }

    @Override
    public void loadAircraft(FlySchedule flySchedule){
        this.luggageWeight= flySchedule.getCargoWeight();
        this.nrOfPassengers= flySchedule.getNrOfPassengers();
    }
}
