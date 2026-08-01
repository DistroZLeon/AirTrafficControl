export const formatRemainingTime = (rawSeconds) => {
    if (!rawSeconds || isNaN(rawSeconds) || !isFinite(rawSeconds) || rawSeconds <= 0) {
        return "--:--";
    }

    const h = Math.floor(rawSeconds / 3600);
    const m = Math.floor((rawSeconds % 3600) / 60).toString().padStart(2, '0');
    const s = Math.floor(rawSeconds % 60).toString().padStart(2, '0');

    return h > 0 ? `${h}:${m}:${s}` : `${m}:${s}`;
};