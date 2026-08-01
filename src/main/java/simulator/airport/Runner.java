package simulator.airport;
import config.Config;
import config.Registry;
import config.parser.DeserializerV2;
import config.parser.JsonTokenizer;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import simulator.plane.Plane;
import simulator.service.WebSocketRadarService;
import simulator.service.WebSocketWeatherService;
import subjects.EventGenerator;
import subjects.WeatherEngine;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class Runner implements CommandLineRunner {
    private final ApplicationContext context;
    private final WebSocketRadarService radarService;
    private final WebSocketWeatherService weatherService;

    public Runner(ApplicationContext context, WebSocketRadarService radarService, WebSocketWeatherService weatherService) {
        this.context = context;
        this.radarService = radarService;
        this.weatherService= weatherService;
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println("-- Initialization --");

        Config config;
        try (InputStream is = getClass().getResourceAsStream("/config.json");
             BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(is)));){
            JsonTokenizer tokenizer = new JsonTokenizer(reader);
            DeserializerV2 deserializer = new DeserializerV2(tokenizer);
            config= deserializer.parseConfig();
            Registry.activeConfiguration= config;

            System.out.println("Config loaded successfully! Number of planes: "+ config.planes().size());
        }catch(IOException e){
            System.err.println("- Failed to parse config file: " + e.getMessage());
            return;
        }

        ControlTower tower= ControlTower.getInstance();
        GateManager gateManager= GateManager.getInstance();
        WeatherEngine weatherEngine= WeatherEngine.getInstance();
        EventGenerator eventGenerator= EventGenerator.getInstance();

        weatherEngine.addObserver(weatherService);
        weatherEngine.addObserver(tower);

        Thread eventThread= new Thread(eventGenerator, "EventThread");
        eventThread.setDaemon(true);
        eventThread.start();
        Thread weatherThread= new Thread(weatherEngine, "WeatherThread");
        weatherThread.setDaemon(true);
        weatherThread.start();

        List<Plane> planes= Registry.activeConfiguration.planes();
        List<Thread> activePlanes= new ArrayList<>();
        for(Plane plane: planes) {
            weatherEngine.addObserver(plane);
            eventGenerator.addObserver(plane);

            plane.addObserver(radarService);

            if(plane.getFlyingPlan().getStartingPoint().equalsIgnoreCase(tower.getAirportName())){
                int gateId = gateManager.acquire(plane.getId());
                if (gateId != -1) {
                    System.out.println("Plane " + plane.getId() + " at Gate " + gateId);
                } else {
                    System.out.println("- Airport full!");
                    continue;
                }
            }
            Thread t= new Thread(plane, "Plane-" + plane.getId());
            activePlanes.add(t);
            t.start();
        }

        System.out.println("-- Running --");
        Thread watcherThread= new Thread(()->{
            try {
                for(Thread thread: activePlanes) {
                    thread.join();
                }

                System.out.println("-- All planes have finished their schedule. Closing the Simulator --");
                int exitCode= SpringApplication.exit(context, () -> 0);
                System.exit(exitCode);
            } catch(InterruptedException e){
                Thread.currentThread().interrupt();
            }
        }, "ShutdownWatcher");

        watcherThread.start();
    }
}
