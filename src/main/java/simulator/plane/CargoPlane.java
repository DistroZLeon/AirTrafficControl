package simulator.plane;
import states.plane.FlySchedule;
import java.util.List;

public class CargoPlane extends Plane{
    private double cargoWeight;
    public CargoPlane(List<FlySchedule> flySchedule, double weight, double baseConsumptionRate){
        super(flySchedule, weight, baseConsumptionRate);
    }

    @Override
    public String getPlaneType() {return "CARGO";}
    @Override
    public double getPayloadWeight() {return this.cargoWeight;}
    @Override
    public int getPassengerCount(){return 0;}

    @Override
    public double getTotalWeight(){
        return this.weight+ this.cargoWeight;
    }

    @Override
    public void loadAircraft(FlySchedule flySchedule){
        this.cargoWeight= flySchedule.getCargoWeight();
    }
}
