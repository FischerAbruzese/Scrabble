import React, {useState} from 'react';
import {Button} from './ui/Button.tsx';
import {Card, CardContent, CardHeader, CardTitle} from './ui/Card.tsx';
import {Input} from './ui/Input.tsx';


interface LobbyScreenProps {
    playerName: string;
    onNameSubmit: (name: string) => void;
    onCreateGame: () => void;
    onJoinGame: (gameId: string) => void;
}

export const LobbyScreen: React.FC<LobbyScreenProps> = ({
                                                            playerName,
                                                            onNameSubmit,
                                                            onCreateGame,
                                                            onJoinGame
                                                        }) => {
    const [joinCode, setJoinCode] = useState('');

    return (
        <Card className="w-full max-w-md bg-gray-800 text-gray-100">
            <CardHeader>
                <CardTitle>Welcome to the Game Lobby</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
                <Input
                    placeholder="Enter your name"
                    value={playerName}
                    onChange={(e) => onNameSubmit(e.target.value)}
                    className="bg-gray-700"
                />
                {playerName && (
                    <>
                        <Button
                            onClick={onCreateGame}
                            className="w-full"
                        >
                            Create New Game
                        </Button>
                        <div className="flex space-x-2">
                            <Input
                                placeholder="Enter game code"
                                value={joinCode}
                                onChange={(e) => setJoinCode(e.target.value)}
                                className="bg-gray-700"
                            />
                            <Button
                                onClick={() => onJoinGame(joinCode)}
                                disabled={!joinCode}
                            >
                                Join Game
                            </Button>
                        </div>
                    </>
                )}
            </CardContent>
        </Card>
    );
};

export default LobbyScreen;