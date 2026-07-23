import simulator.ControlTower;
import simulator.GateManager;
import simulator.Plane;
import states.plane.FlySchedule;
import subjects.EventGenerator;
import subjects.WeatherEngine;

import java.util.ArrayList;
import java.util.List;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("-- Initialization --");

        ControlTower tower= ControlTower.getInstance();
        GateManager gateManager= GateManager.getInstance();
        WeatherEngine weatherEngine= WeatherEngine.getInstance();
        EventGenerator eventGenerator= EventGenerator.getInstance();

        weatherEngine.addObserver(tower);

        new Thread(eventGenerator, "EventThread").start();
        new Thread(weatherEngine, "WeatherThread").start();

        List<FlySchedule> schedule1= new ArrayList<>();
        schedule1.add(new FlySchedule("Otopeni", "JFK", 20, 0, 150, 2000));
        Plane plane1 = new Plane(schedule1, 45000.0, 12.0);

        List<FlySchedule> schedule2 = new ArrayList<>();
        // Departs at time 150, arrives at time 1500
        schedule2.add(new FlySchedule("Otopeni", "LHR", 30, 15, 200, 3000));
        Plane plane2 = new Plane(schedule2, 50000.0, 14.0);

        List<FlySchedule> schedule3 = new ArrayList<>();
        // Departs at time 0, arrives at time 1025
        schedule3.add(new FlySchedule("JFK", "Otopeni", 25, 10, 100, 1500));
        Plane plane3 = new Plane(schedule3, 35000.0, 10.0);

        Plane[] fleet = {plane1, plane2, plane3};

        for(Plane plane: fleet) {
            weatherEngine.addObserver(plane);
            eventGenerator.addObserver(plane);

            if(plane.getFlyingPlan().getStartingPoint().equalsIgnoreCase(tower.getAirportName())){
                int gateId = gateManager.acquire(plane.getId());
                if (gateId != -1) {
                    System.out.println("simulator.Plane " + plane.getId() + " at Gate " + gateId);
                } else
                    System.out.println("- Airport full!");
            }
            new Thread(plane, "simulator.Plane-" + plane.getId()).start();
        }

        System.out.println("-- Running --");
    }
}
