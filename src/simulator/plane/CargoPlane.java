package simulator.plane;
import states.plane.FlySchedule;
import java.util.List;

public class CargoPlane extends Plane{
    private double cargoWeight;
    public CargoPlane(List<FlySchedule> flySchedule, double weight, double baseConsumptionRate){
        super(flySchedule, weight, baseConsumptionRate);
    }

    @Override
    protected double getTotalWeight(){
        return this.weight+ this.cargoWeight;
    }

    @Override
    public void loadAircraft(FlySchedule flySchedule){
        this.cargoWeight= flySchedule.getCargoWeight();
    }
}
