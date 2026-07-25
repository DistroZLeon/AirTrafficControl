package subjects;
import config.Registry;
import states.weather.WeatherState;
import states.weather.WeatherType;

import java.util.Random;


public class WeatherEngine extends Subject implements Runnable {
    private final Random random = new Random();
    private final int weatherIntervals;
    private WeatherEngine(){
        this.weatherIntervals= Registry.activeConfiguration.generatorSettings().WeatherUpdateIntervalsSeconds()*1000;
    }

    private static class Holder{
        private static final WeatherEngine INSTANCE = new WeatherEngine();
    }

    public static WeatherEngine getInstance(){
        return Holder.INSTANCE;
    }

    public void triggerWeatherChange(WeatherState state){
        notifyObservers(state);
    }

    @Override
    public void run() {
        try{
            while(!Thread.currentThread().isInterrupted()){
                Thread.sleep(this.weatherIntervals);
                if(random.nextDouble()>=0.15){
                    WeatherType[] types = WeatherType.values();
                    WeatherType type = types[random.nextInt(types.length)];

                    double windStrength = random.nextDouble() * 2.0;
                    double consumptionRate = type.getBaseFuelConsumption() + random.nextDouble();
                    double visibility = type.getBaseVisibility() + random.nextDouble() * .3;
                    int lanesClosed = random.nextDouble() >= .5 ? type.getBaseLanesClosed() + 1 : type.getBaseLanesClosed();

                    WeatherState state = new WeatherState(type, windStrength, visibility, lanesClosed, consumptionRate);
                    System.out.println("+ Weather is now: " + type.name());
                    triggerWeatherChange(state);
                }
            }
        } catch (InterruptedException e){
            System.out.println("WeatherEngine closed");
            Thread.currentThread().interrupt();
        }
    }
}
