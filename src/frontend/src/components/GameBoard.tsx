import React from 'react';
import {GameState} from '../types/GameState';

interface GameBoardProps {
    gameState: GameState;
}

export const GameBoard: React.FC<GameBoardProps> = ({gameState}) => {
    return (
        <div className="bg-gray-100 p-4 rounded">
            <h2 className="text-xl font-bold mb-2">Game Info:</h2>
            <div className="space-y-2">
                <p>
                    <span className="font-semibold">Players:</span>{' '}
                    {gameState.players.map(p => p.name).join(', ')}
                </p>
                <p><span className="font-semibold">Turn Number:</span> {gameState.turnNum}</p>
                <p><span className="font-semibold">Pass Streak:</span> {gameState.passStreak}</p>
            </div>
        </div>
    );
};