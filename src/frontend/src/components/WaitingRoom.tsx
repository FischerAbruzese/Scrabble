import React from 'react';
import {Button} from './ui/Button.tsx';
import {Card, CardContent, CardHeader, CardTitle} from './ui/Card';
import {Copy} from 'lucide-react';

interface WaitingRoomProps {
    gameId: string;
    players: string[];
    onStartGame: () => void;
    onCopyCode: () => void;
    isCreator: boolean;
}

export const WaitingRoom: React.FC<WaitingRoomProps> = ({
                                                            gameId,
                                                            players,
                                                            onStartGame,
                                                            onCopyCode,
                                                            isCreator
                                                        }) => {
    return (
        <Card className="w-full max-w-md bg-gray-800 text-gray-100">
            <CardHeader>
                <CardTitle>Waiting Room</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
                <div className="flex items-center justify-between bg-gray-700 p-2 rounded">
                    <span>Game Code: {gameId}</span>
                    <Button variant="ghost" size="icon" onClick={onCopyCode}>
                        <Copy className="h-4 w-4"/>
                    </Button>
                </div>
                <div className="space-y-2">
                    <h3 className="font-semibold">Players:</h3>
                    {players.map((player, index) => (
                        <div key={index} className="bg-gray-700 p-2 rounded">
                            {player} {index === 0 && '(Host)'}
                        </div>
                    ))}
                </div>
                {isCreator && (
                    <Button
                        onClick={onStartGame}
                        className="w-full"
                        disabled={players.length < 2}
                    >
                        Start Game
                    </Button>
                )}
            </CardContent>
        </Card>
    );
};

export default WaitingRoom;