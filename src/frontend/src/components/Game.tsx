import {useState} from 'react';
import {useGameState} from '../hooks/useGameState';
import {GameBoard} from './GameBoard';
import {PlayerHand} from './PlayerHand';
import {JoinScreen} from './JoinScreen';
import GameConsole from './GameConsole';
import {AlertCircle, Loader2} from 'lucide-react';

export const Game = () => {
    const [playerName, setPlayerName] = useState('');
    const [isNameSubmitted, setIsNameSubmitted] = useState(false);

    const {gameState, error, socket, messages} = useGameState(
        isNameSubmitted ? playerName : ''
    );

    const handleJoin = (name: string) => {
        setPlayerName(name);
        setIsNameSubmitted(true);
    };

    if (!isNameSubmitted) {
        return (
            <div className="flex items-center justify-center w-full h-full bg-gray-900">
                <div className="w-full max-w-md">
                    <JoinScreen onJoin={handleJoin}/>
                </div>
            </div>
        );
    }

    if (error) {
        return (
            <div className="flex items-center justify-center w-full h-full bg-gray-900">
                <div
                    className="flex items-center space-x-2 bg-gray-800 text-red-400 p-4 rounded-lg border border-red-900">
                    <AlertCircle className="h-4 w-4"/>
                    <span>Error: {error}</span>
                </div>
            </div>
        );
    }

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

    return (
        <div className="flex flex-col w-full h-full bg-gray-900">
            <div className="flex flex-grow overflow-hidden">
                {/* Main game board section */}
                <div className="flex-grow h-full">
                    <div className="h-full bg-gray-800 text-gray-100">
                        <GameBoard gameState={gameState}/>
                    </div>
                </div>

                {/* Right sidebar */}
                <div className="w-96 h-full flex flex-col bg-gray-900 border-l border-gray-700">
                    {/* Game console section */}
                    <div className="flex-grow bg-gray-800 p-4 flex flex-col">
                        <h2 className="text-xl font-semibold text-gray-100 mb-4">Game Console</h2>
                        <div className="flex-grow bg-gray-900 rounded-lg p-4 flex flex-col">
                            <div className="flex-grow overflow-auto text-gray-100">
                                <GameConsole
                                    playerName={playerName}
                                    socket={socket}
                                    messages={messages}
                                />
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            {/* Bottom player hand section */}
            <div className="w-full bg-gray-800 border-t border-gray-700">
                <div className="max-w-screen-xl mx-auto p-4">
                    <div className="flex items-center justify-between mb-2">
                        <h2 className="text-xl font-semibold text-gray-100">Your Hand</h2>
                    </div>
                    <div className="bg-gray-900 rounded-lg p-4">
                        <PlayerHand hand={gameState.yourHand}/>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Game;