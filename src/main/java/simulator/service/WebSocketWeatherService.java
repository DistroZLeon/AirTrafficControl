package simulator.service;
import observers.Observer;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import states.weather.WeatherState;
import subjects.Subject;
import subjects.WeatherEngine;

import java.util.HashMap;
import java.util.Map;

@Service
public class WebSocketWeatherService implements Observer {
    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketWeatherService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void update(Subject source, Object arg){
        if(source instanceof WeatherEngine)
            if(arg instanceof WeatherState weather){
                String type= weather.type().name();

                Map<String, Object> weatherData = Map.of(
                        "type", type,
                        "windStrength", weather.windStrength(),
                        "consumptionRate", weather.consumptionRate()
                );

                messagingTemplate.convertAndSend("/topic/weather", (Object) weatherData);
            }
    }
}
