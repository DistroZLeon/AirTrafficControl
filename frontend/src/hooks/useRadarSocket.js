import { useState, useEffect } from 'react';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

export const useRadarSocket = (websocketUrl) => {
    const [planes, setPlanes] = useState({});

    useEffect(() => {
        const socket = new SockJS(websocketUrl);
        const stompClient = new Client({
            webSocketFactory: () => socket,
            debug: () => {},
            onConnect: () => {
                console.log("React Connected!");

                stompClient.subscribe('/topic/radar', (message) => {
                    const planeData = JSON.parse(message.body);
                    const details = planeData.stateDetails || planeData.stateDate || {};

                    setPlanes(prev => {
                        if (details.status === 'FINISHED') {
                            const nextPlanes = { ...prev };
                            delete nextPlanes[planeData.planeId];
                            return nextPlanes;
                        }

                        return {
                            ...prev,
                            [planeData.planeId]: planeData
                        };
                    });
                });
            }
        });

        stompClient.activate();
        return () => stompClient.deactivate();
    }, [websocketUrl]);

    return planes;
};