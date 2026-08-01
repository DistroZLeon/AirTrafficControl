package states.plane;

public class FlySchedule {
    private final String destination, startingPoint;
    private final double timeOfArrival, timeOfDeparture;
    private final int nrOfPassengers;
    private final double cargoWeight;

    public FlySchedule(String destination, String startingPoint, double timeOfArrival, double timeOfDeparture, int nrOfPassengers, double cargoWeight) {
        this.destination = destination;
        this.startingPoint = startingPoint;
        this.timeOfArrival = timeOfArrival;
        this.timeOfDeparture = timeOfDeparture;
        this.nrOfPassengers = nrOfPassengers;
        this.cargoWeight = cargoWeight;
    }

    public String getDestination() {
        return this.destination;
    }

    public String getStartingPoint() {
        return this.startingPoint;
    }

    public double getTimeOfArrival() {
        return this.timeOfArrival;
    }

    public double getTimeOfDeparture() {
        return this.timeOfDeparture;
    }

    public int getNrOfPassengers() {
        return this.nrOfPassengers;
    }

    public double getCargoWeight() {
        return this.cargoWeight;
    }
}
