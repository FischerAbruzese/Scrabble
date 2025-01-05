import {useEffect, useRef, useState} from 'react';

interface LobbyState {
    players: string[];
    isStarted: boolean;
}

interface UseLobbyReturn {
    createGame: (playerName: string) => void;
    joinGame: (gameId: string, playerName: string) => void;
    startGame: (gameId: string) => void;
    lobbyState: LobbyState | null;
    error: string | null;
}

export const useGameLobby = (): UseLobbyReturn => {
    const [lobbyState, setLobbyState] = useState<LobbyState | null>(null);
    const [error, setError] = useState<string | null>(null);
    const socketRef = useRef<WebSocket | null>(null);

    useEffect(() => {
        const ws = new WebSocket('ws://localhost:8080/lobby');

        ws.onopen = () => {
            console.log('Connected to lobby');
        };

        ws.onmessage = (event) => {
            try {
                const data = JSON.parse(event.data);
                if (data.type === 'GAME_STATE') {
                    setLobbyState({
                        players: data.players,
                        isStarted: data.isStarted
                    });
                }
            } catch (e) {
                setError('Failed to parse lobby message');
            }
        };

        ws.onerror = () => {
            setError('WebSocket error occurred');
        };

        ws.onclose = () => {
            setError('Lobby connection closed');
        };

        socketRef.current = ws;

        return () => {
            ws.close();
        };
    }, []);

    const createGame = (playerName: string) => {
        if (socketRef.current) {
            socketRef.current.send(JSON.stringify({
                type: 'CREATE_GAME',
                name: playerName
            }));
        }
    };

    const joinGame = (gameId: string, playerName: string) => {
        if (socketRef.current) {
            socketRef.current.send(JSON.stringify({
                type: 'JOIN_GAME',
                gameId,
                name: playerName
            }));
        }
    };

    const startGame = (gameId: string) => {
        if (socketRef.current) {
            socketRef.current.send(JSON.stringify({
                type: 'START_GAME',
                gameId
            }));
        }
    };

    return {
        createGame,
        joinGame,
        startGame,
        lobbyState,
        error
    };
};