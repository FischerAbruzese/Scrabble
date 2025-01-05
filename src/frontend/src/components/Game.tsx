import {useState} from 'react';
import {useGameLobby} from '../hooks/useGameLobby';
import {useGameState} from '../hooks/useGameState';
import {LobbyScreen} from './LobbyScreen';
import {WaitingRoom} from './WaitingRoom';
import {GameLayout} from './game/GameLayout.tsx';
import {AlertCircle, Loader2} from 'lucide-react';

export const Game = () => {
    const [playerName, setPlayerName] = useState('');
    // @ts-ignore
    const [gameId, setGameId] = useState<string | null>(null);

    const {
        createGame,
        joinGame,
        startGame,
        lobbyState,
        error: lobbyError
    } = useGameLobby();

    const {
        gameState,
        error: gameError,
        messages,
        sendMessage
    } = useGameState(gameId, playerName);

    const handleCreateGame = () => {
        if (playerName) {
            createGame(playerName);
        }
    };

    const handleJoinGame = (joinGameId: string) => {
        if (playerName) {
            joinGame(joinGameId, playerName);
        }
    };

    const handleStartGame = () => {
        if (gameId) {
            startGame(gameId);
        }
    };

    const copyGameCode = () => {
        if (gameId) {
            navigator.clipboard.writeText(gameId);
        }
    };

    // Show initial lobby screen
    if (!playerName || !gameId) {
        return (
            <div className="flex items-center justify-center w-full h-full bg-gray-900">
                <LobbyScreen
                    playerName={playerName}
                    onNameSubmit={setPlayerName}
                    onCreateGame={handleCreateGame}
                    onJoinGame={handleJoinGame}
                />
            </div>
        );
    }

    // Show waiting room
    if (!(gameState == null)) {
        return (
            <div className="flex items-center justify-center w-full h-full bg-gray-900">
                <WaitingRoom
                    gameId={gameId}
                    players={lobbyState?.players || []}
                    onStartGame={handleStartGame}
                    onCopyCode={copyGameCode}
                    isCreator={lobbyState?.players[0] === playerName}
                />
            </div>
        );
    }

    // Show error states
    if (lobbyError || gameError) {
        return (
            <div className="flex items-center justify-center w-full h-full bg-gray-900">
                <div
                    className="flex items-center space-x-2 bg-gray-800 text-red-400 p-4 rounded-lg border border-red-900">
                    <AlertCircle className="h-4 w-4"/>
                    <span>Error: {lobbyError || gameError}</span>
                </div>
            </div>
        );
    }

    // Show loading state
    if (!gameState) {
        return (
            <div className="flex items-center justify-center w-full h-full bg-gray-900">
                <div className="flex items-center space-x-2 text-gray-300">
                    <Loader2 className="h-6 w-6 animate-spin"/>
                    <span>Loading game state...</span>
                </div>
            </div>
        );
    }

    // Show main game
    return (
        <div className="flex flex-col w-full h-full bg-gray-900">
            <GameLayout
                gameState={gameState}
                playerName={playerName}
                messages={messages}
                sendMessage={sendMessage}
            />
        </div>
    );
};

export default Game;