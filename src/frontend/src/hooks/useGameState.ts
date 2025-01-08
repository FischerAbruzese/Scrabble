import {useEffect, useRef, useState} from 'react';
import {GameState} from '../types/GameState';
import {GameMessage} from '../types/WebSocketMessages';

interface UseGameStateReturn {
    gameState: GameState | null;
    error: string | null;
    messages: Array<{
        player: string;
        content: string;
        timestamp: string;
    }>;
    sendMessage: (content: string) => void;
}

// useGameState.ts
export const useGameState = (
    gameId: string | null,
    playerName: string
): UseGameStateReturn => {
    const [gameState, setGameState] = useState<GameState | null>(null);
    const [error, setError] = useState<string | null>(null);
    const [messages, setMessages] = useState<Array<{
        player: string;
        content: string;
        timestamp: string;
    }>>([]);
    const socketRef = useRef<WebSocket | null>(null);

    useEffect(() => {
        if (!gameId || !playerName) return;

        const wsUrl = import.meta.env.VITE_WS_URL || 'ws://localhost:8080';
        const ws = new WebSocket(`${wsUrl}/game/${gameId}`);

        ws.onopen = () => {
            console.log('Connected to game room');
        };

        ws.onmessage = (event) => {
            try {
                const data = JSON.parse(event.data);

                if (data.type === 'MESSAGE') {
                    const message = data as GameMessage;
                    if (message.player !== playerName) {
                        addMessage(message.player, message.content);
                    }
                    return;
                }

                // If we get here, it's a game state update
                setGameState(data);
            } catch (e) {
                setError('Failed to parse game message');
            }
        };

        ws.onerror = () => {
            setError('Game WebSocket error occurred');
        };

        ws.onclose = () => {
            setError('Game connection closed');
        };

        socketRef.current = ws;

        return () => {
            ws.close();
        };
    }, [gameId, playerName]);

    const addMessage = (player: string, content: string) => {
        setMessages(prev => [...prev, {
            player,
            content,
            timestamp: new Date().toLocaleTimeString()
        }]);
    };

    const sendMessage = (content: string) => {
        if (socketRef.current && playerName) {
            const message: GameMessage = {
                type: 'MESSAGE',
                player: playerName,
                content
            };
            socketRef.current.send(JSON.stringify(message));
            addMessage(playerName, content);
        }
    };

    return {
        gameState,
        error,
        messages,
        sendMessage
    };
};