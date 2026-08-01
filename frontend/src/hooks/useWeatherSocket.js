import { useState, useEffect } from 'react';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

export const useWeatherSocket = (websocketUrl) => {
    const [weather, setWeather] = useState({
        type: "CLEAR",
        windStrength: 1.0,
        consumptionRate: 1.0
    });

    useEffect(() => {
        const socket = new SockJS(websocketUrl);
        const stompClient = new Client({
            webSocketFactory: () => socket,
            debug: () => {},
            onConnect: () => {
                stompClient.subscribe('/topic/weather', (message) => {
                    setWeather(JSON.parse(message.body));
                });
            }
        });

        stompClient.activate();
        return () => stompClient.deactivate();
    }, [websocketUrl]);

    return weather;
};