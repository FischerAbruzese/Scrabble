import React from 'react';
import {GameBoard} from './GameBoard.tsx';
import {PlayerHand} from './PlayerHand.tsx';
import {GameConsole} from './GameConsole.tsx';
import {GameState} from '../../types/GameState.ts';

interface GameLayoutProps {
    gameState: GameState;
    playerName: string;
    messages: Array<{
        player: string;
        content: string;
        timestamp: string;
    }>;
    sendMessage: (content: string) => void;
}

export const GameLayout: React.FC<GameLayoutProps> = ({
                                                          gameState,
                                                          playerName,
                                                          messages,
                                                          sendMessage
                                                      }) => (
    <>
        <div className="flex flex-grow overflow-hidden">
            <div className="flex-grow h-full">
                <div className="h-full bg-gray-800 text-gray-100">
                    <GameBoard gameState={gameState}/>
                </div>
            </div>
            <div className="w-96 h-full flex flex-col bg-gray-900 border-l border-gray-700">
                <div className="flex-grow bg-gray-800 p-4 flex flex-col">
                    <h2 className="text-xl font-semibold text-gray-100 mb-4">Game Console</h2>
                    <GameConsole
                        playerName={playerName}
                        messages={messages}
                        sendMessage={sendMessage}
                    />
                </div>
            </div>
        </div>
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
    </>
);

export default GameLayout;