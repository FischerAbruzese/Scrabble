import React from 'react';
import {GameState} from '../../types/GameState.ts';

interface GameBoardProps {
    gameState: GameState;
}

// @ts-ignore
export const GameBoard: React.FC<GameBoardProps> = ({gameState}) => {
    return (
        <div className="w-full h-full flex items-center justify-center border-2 border-dashed border-gray-600 p-4">
            <span className="text-lg text-gray-400">Game Board goes here</span>
        </div>
    );
};

export default GameBoard;