import {useEffect, useRef, useState} from 'react';
import {GameState} from "../types/GameState";
import {GameMessage, WebSocketMessage} from "../types/ConsoleMessages";

interface UseGameStateReturn {
    gameState: GameState | null;
    error: string | null;
    socket: WebSocket | null;
    messages: Array<{
        player: string;
        content: string;
        timestamp: string;
    }>;
    sendMessage: (content: string) => void;
}

export const useGameState = (playerName: string): UseGameStateReturn => {
    const [gameState, setGameState] = useState<GameState | null>(null);
    const [error, setError] = useState<string | null>(null);
    const [messages, setMessages] = useState<Array<{
        player: string;
        content: string;
        timestamp: string;
    }>>([]);
    const socketRef = useRef<WebSocket | null>(null);

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
            console.log('Sending message:', message);
            socketRef.current.send(JSON.stringify(message));
            console.log('Message sent to WebSocket');
            addMessage(playerName, content);
        } else {
            console.warn('Could not send message - socket or playerName not available', {
                socketExists: !!socketRef.current,
                playerName
            });
        }
    };

    useEffect(() => {
        if (!playerName) {
            return;
        }

        // const ws = new WebSocket('wss://scrabbledockerbackend.onrender.com/game-state');
        const ws = new WebSocket('ws://localhost:8080/game-state');

        ws.onopen = () => {
            console.log('Connected to game server');
            const joinMessage: WebSocketMessage = {
                type: 'JOIN',
                name: playerName
            };
            ws.send(JSON.stringify(joinMessage));
            console.log('Sent join message:', joinMessage);
        };

        ws.onmessage = (event) => {
            try {
                const data = JSON.parse(event.data);

                if ('type' in data) {
                    switch (data.type) {
                        case 'MESSAGE':
                            const gameMessage = data as GameMessage;
                            // Only add messages from other players
                            if (gameMessage.player !== playerName) {
                                addMessage(gameMessage.player, gameMessage.content);
                            }
                            return;
                        case 'JOIN':
                            // Something is wrong if the server is sending you a join message lol
                            return;
                        case 'INPUT':
                            // Something is wrong if the server is sending you a input message lol
                            return;
                    }
                }

                // If we get here, it's a game state update
                setGameState(data);
            } catch (e) {
                setError('Failed to parse message');
                console.error('Error parsing message:', e);
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

        return () => {
            ws.close();
        };
    }, [playerName]);

    return {
        gameState,
        error,
        socket: socketRef.current,
        messages,
        sendMessage
    };
};