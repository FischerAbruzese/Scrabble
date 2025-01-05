// GameBoard.tsx
import React, {useState} from 'react';
import {useGameState} from '../hooks/useGameState';

export const GameBoard: React.FC = () => {
    const [playerName, setPlayerName] = useState<string>('');
    const [isNameSubmitted, setIsNameSubmitted] = useState(false);

    const handleSubmitName = (e: React.FormEvent) => {
        e.preventDefault();
        if (playerName.trim()) {
            setIsNameSubmitted(true);
        }
    };

    const {gameState, error, currentPlayerHand} = useGameState(
        isNameSubmitted ? playerName : ''
    );

    if (!isNameSubmitted) {
        return (
            <div className="p-4">
                <form onSubmit={handleSubmitName} className="space-y-4">
                    <div>
                        <label htmlFor="playerName" className="block text-sm font-medium">
                            Enter your name to join the game:
                        </label>
                        <input
                            type="text"
                            id="playerName"
                            value={playerName}
                            onChange={(e) => setPlayerName(e.target.value)}
                            className="mt-1 block w-full rounded-md border-gray-300 shadow-sm"
                            required
                        />
                    </div>
                    <button
                        type="submit"
                        className="bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-600"
                    >
                        Join Game
                    </button>
                </form>
            </div>
        );
    }

    if (error) {
        return <div className="p-4 text-red-500">Error: {error}</div>;
    }

    if (!gameState) {
        return <div className="p-4">Loading game state...</div>;
    }

    return (
        <div className="p-4 space-y-4">
            <h2 className="text-xl font-bold">Game State:</h2>
            <pre className="bg-gray-100 p-4 rounded overflow-auto">
        {JSON.stringify(gameState, null, 2)}
      </pre>

            {currentPlayerHand && (
                <div className="mt-4">
                    <h3 className="text-lg font-bold">Your Hand:</h3>
                    <pre className="bg-gray-100 p-4 rounded overflow-auto">
            {JSON.stringify(currentPlayerHand, null, 2)}
          </pre>
                </div>
            )}
        </div>
    );
};