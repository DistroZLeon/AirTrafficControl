public class FlySchedule {
    private String destination, startingPoint;
    private double timeOfArrival, timeOfDeparture;
    private int nrOfPassengers;
    private double cargoWeight;

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
