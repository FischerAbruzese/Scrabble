import React from 'react';
import { useGameState } from '../hooks/useGameState';

export const GameBoard: React.FC = () => {
    const { gameState } = useGameState();

    if (!gameState) {
        return <div>Loading game state...</div>;
    }

    return (
        <div className="p-4">
            <div>Turn: {gameState.turnNum}</div>
            <div>Pass Streak: {gameState.passStreak}</div>
            <div>
                Current Player: {gameState.turnNum % gameState.players.length}
            </div>
            {/* Add your board rendering logic here */}
            <div className="mt-4">
                {/* Example board rendering - customize based on your Board structure */}
                Board goes here
            </div>
        </div>
    );
};