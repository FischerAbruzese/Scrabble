import React, {useEffect, useRef, useState} from 'react';
import {Send} from 'lucide-react';

interface Message {
    player: string;
    content: string;
    timestamp: string;
}

interface GameConsoleProps {
    playerName: string;
    messages: Message[];
    sendMessage: (content: string) => void;
}

const GameConsole: React.FC<GameConsoleProps> = ({
                                                     playerName,
                                                     messages,
                                                     sendMessage,
                                                 }) => {
    const [input, setInput] = useState('');
    const messagesEndRef = useRef<HTMLDivElement>(null);

    const scrollToBottom = () => {
        messagesEndRef.current?.scrollIntoView({behavior: 'smooth'});
    };

    useEffect(() => {
        scrollToBottom();
    }, [messages]);

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        if (!input.trim()) return;

        sendMessage(input);
        setInput('');
    };

    const isCurrentPlayer = (msgPlayer: string) => msgPlayer === playerName;

    return (
        <div className="flex flex-col h-full relative">
            {/* Messages container */}
            <div className="flex-grow overflow-y-auto mb-1 space-y-1 p-1">
                {messages.map((msg, index) => (
                    <div key={index} className="w-full space-y-2">
                        <div className={`w-full rounded-lg p-4 shadow-lg ${
                            isCurrentPlayer(msg.player) ? 'bg-gray-700' : 'bg-gray-800'
                        }`}>
                            <p className="text-gray-100">{msg.content}</p>
                        </div>
                    </div>
                ))}
                <div ref={messagesEndRef}/>
            </div>

            {/* Input form */}
            <form
                onSubmit={handleSubmit}
                className="flex gap-2 bg-gray-800 p-2 rounded-lg border border-gray-700"
            >
                <input
                    type="text"
                    value={input}
                    onChange={(e) => setInput(e.target.value)}
                    className="flex-1 bg-gray-900 text-gray-100 rounded-lg px-4 py-2 border border-gray-700 focus:outline-none focus:border-blue-500 placeholder-gray-500"
                    placeholder="Type a message..."
                />
                <button
                    type="submit"
                    className="bg-blue-600 text-white p-2 rounded-lg hover:bg-blue-700 transition-colors duration-200 flex items-center justify-center"
                    disabled={!input.trim()}
                >
                    <Send className="w-5 h-5"/>
                </button>
            </form>
        </div>
    );
};

export default GameConsole;