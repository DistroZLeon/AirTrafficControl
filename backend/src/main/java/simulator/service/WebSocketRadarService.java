package simulator.service;
import observers.Observer;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import states.plane.RadarData;
import subjects.Subject;

@Service
public class WebSocketRadarService implements Observer {
    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketRadarService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void update(Subject source, Object arg) {
        if(arg instanceof RadarData data)
            messagingTemplate.convertAndSend("/topic/radar", data);
    }

}
