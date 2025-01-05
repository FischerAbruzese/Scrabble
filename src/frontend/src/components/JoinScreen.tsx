import React, {useState} from 'react';

interface JoinScreenProps {
    onJoin: (name: string) => void;
}

export const JoinScreen: React.FC<JoinScreenProps> = ({onJoin}) => {
    const [playerName, setPlayerName] = useState('');

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        if (playerName.trim()) {
            onJoin(playerName.trim());
        }
    };

    return (
        <div className="p-4">
            <form onSubmit={handleSubmit} className="space-y-4">
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
};