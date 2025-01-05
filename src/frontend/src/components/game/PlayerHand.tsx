import React from 'react';
import {Hand} from '../../types/GameState.ts';

interface PlayerHandProps {
    hand: Hand;
}

// @ts-ignore
export const PlayerHand: React.FC<PlayerHandProps> = ({hand}) => {
    return (
        <div className="w-full flex items-center justify-center border-2 border-dashed border-gray-600 p-4">
            <span className="text-lg text-gray-400">Player Hand goes here</span>
        </div>
    );
};

export default PlayerHand;