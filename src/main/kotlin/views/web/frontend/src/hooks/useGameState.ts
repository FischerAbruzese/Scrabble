import { useState, useEffect } from 'react';
import { GameState } from '../types/GameState';

export const useGameState = () => {
    const [gameState, setGameState] = useState<GameState | null>(null);
    const [wsConnection, setWsConnection] = useState<WebSocket | null>(null);

    useEffect(() => {
        const ws = new WebSocket('ws://localhost:8080/game-state');

        ws.onopen = () => {
            console.log('Connected to game server');
        };

        ws.onmessage = (event) => {
            try {
                const newGameState = JSON.parse(event.data) as GameState;
                setGameState(newGameState);
            } catch (error) {
                console.error('Error parsing game state:', error);
            }
        };

        ws.onerror = (error) => {
            console.error('WebSocket error:', error);
        };

        ws.onclose = () => {
            console.log('Disconnected from game server');
        };

        setWsConnection(ws);

        return () => {
            ws.close();
        };
    }, []);

    return { gameState, wsConnection };
};
