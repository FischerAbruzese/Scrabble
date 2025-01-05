import React from 'react';
import {Hand} from '../types/GameState';

interface PlayerHandProps {
    hand: Hand;
}

export const PlayerHand: React.FC<PlayerHandProps> = ({hand}) => {
    return (
        <div className="flex flex-col h-full items-center">
            <div className="flex justify-center gap-4">
                {hand.pieces.map((piece, index) => (
                    <div
                        key={`${piece.letter}-${piece.value}-${index}`}
                        className="relative group"
                    >
                        <div className="bg-gray-800 rounded-lg w-14 h-14 flex items-center justify-center
                                    transform transition-all duration-200
                                    hover:bg-gray-700
                                    border border-gray-700 hover:border-gray-600
                                    shadow-lg hover:shadow-xl">
                            <div className="flex flex-col items-center">
                                <span className="text-2xl font-bold text-gray-100">
                                    {piece.letter}
                                </span>
                                <span className="text-xs text-gray-400 -mt-1">
                                    {piece.value}
                                </span>
                            </div>
                        </div>

                        {/* Hover effect - subtle glow */}
                        <div className="absolute inset-0 rounded-lg bg-blue-500 opacity-0
                                      group-hover:opacity-10 transition-opacity duration-200">
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
};

export default PlayerHand;