import React from 'react';
import './ProgressBar.css';

export default function ProgressBar({ details }) {
    const status = details.status || "UNKNOWN";
    let progress = 5;

    if (status === 'BOARDING') {
        progress = 5;
    }
    else if (status.includes('TAKEOFF') || status.includes('LEAVE')) {
        progress = 15;
    }
    else if (status === 'EN_ROUTE') {
        const passed = details.timePassed || 0;
        const duration = details.flightDuration || 1;

        const percentFlown = passed / duration;

        progress = 15 + (percentFlown * 70);
    }
    else if (status.includes('WAITING_LANDING') || status.includes('HOLDING')) {
        progress = 85;
    }
    else if (status.includes('LANDING') || status === 'FINISHED') {
        progress = 95;
    }

    return (
        <div className="travel-track-container">
            <div className="travel-line"></div>
            <div className="travel-nodes">
                <span className="node label-left">DEP</span>
                <span className="node label-right">ARR</span>
            </div>

            <div
                className="animated-plane"
                style={{
                    left: `${progress}%`,
                    transform: `translate(-50%, -50%) rotate(45deg)`
                }}
            >
                ✈️
            </div>
        </div>
    );
}