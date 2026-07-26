package simulator.plane;

import states.plane.FlySchedule;

import java.util.List;

public class PlaneFactory {
    public static Plane createPlane(String type, List<FlySchedule> flySchedules, double weight, double baseConsumptionRate){
        if(type.equalsIgnoreCase("Cargo")){
            return new CargoPlane(flySchedules, weight, baseConsumptionRate);
        } else if(type.equalsIgnoreCase("Passenger")){
            return new PassengerPlane(flySchedules, weight, baseConsumptionRate);
        }
        throw new IllegalArgumentException("Invalid plane type: "+ type);
    }
}
