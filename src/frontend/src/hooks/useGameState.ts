import {useCallback, useEffect, useRef, useState} from 'react';
import {GameState} from "../types/GameState.ts";

export const useGameState = (playerName: string) => {
    const [gameState, setGameState] = useState<GameState | null>(null);
    const [error, setError] = useState<string | null>(null);
    const socketRef = useRef<WebSocket | null>(null);

    // Only establish WebSocket connection when we have a playerName
    useEffect(() => {
        if (!playerName) {
            return;
        }

        const ws = new WebSocket('ws://localhost:8080/game-state');

        ws.onopen = () => {
            console.log('Connected to game server');
            const joinMessage = {
                type: 'JOIN',
                name: playerName
            };
            ws.send(JSON.stringify(joinMessage));
            console.log('Sent join message:', joinMessage);
        };

        ws.onmessage = (event) => {
            try {
                const data = JSON.parse(event.data);
                setGameState(data);
            } catch (e) {
                setError('Failed to parse game state');
                console.error('Error parsing game state:', e);
            }
        };

        ws.onerror = (event) => {
            setError('WebSocket error occurred');
            console.error('WebSocket error:', event);
        };

        ws.onclose = () => {
            setError('Connection closed');
            console.log('Disconnected from game server');
        };

        socketRef.current = ws;

        // Cleanup on unmount or when playerName changes
        return () => {
            ws.close();
        };
    }, [playerName]); // Only run when playerName changes

    const getCurrentPlayerHand = useCallback(() => {
        if (!gameState) return null;
        return gameState.players.find(p => p.name === playerName)?.hand || null;
    }, [gameState, playerName]);

    return {
        gameState,
        error,
        currentPlayerHand: getCurrentPlayerHand(),
    };
};