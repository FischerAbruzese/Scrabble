import React from 'react';

interface GameConsoleProps {
    playerName: string;
    messages: Array<{
        player: string;
        content: string;
        timestamp: string;
    }>;
    sendMessage: (content: string) => void;
}

// @ts-ignore
export const GameConsole: React.FC<GameConsoleProps> = ({playerName, messages, sendMessage}) => {
    return (
        <div className="w-full h-full flex items-center justify-center border-2 border-dashed border-gray-600">
            <span className="text-lg text-gray-400">Game Console goes here</span>
        </div>
    );
};

export default GameConsole;