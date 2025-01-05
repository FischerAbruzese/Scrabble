import React, {useEffect, useRef, useState} from 'react';
import {GameMessage} from '../types/ConsoleMessages';
import {Clock, Send} from 'lucide-react';

interface Message {
    player: string;
    content: string;
    timestamp: string;
}

interface GameConsoleProps {
    playerName: string;
    socket: WebSocket | null;
    messages: Message[];
}

const GameConsole: React.FC<GameConsoleProps> = ({playerName, socket, messages}) => {
    const [input, setInput] = useState('');
    const messagesEndRef = useRef<HTMLDivElement>(null);

    const scrollToBottom = () => {
        messagesEndRef.current?.scrollIntoView({behavior: "smooth"});
    };

    useEffect(() => {
        scrollToBottom();
    }, [messages]);

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        if (!input.trim() || !socket) return;

        const message: GameMessage = {
            type: 'MESSAGE',
            player: playerName,
            content: input
        };

        socket.send(JSON.stringify(message));
        setInput('');
    };

    const isCurrentPlayer = (msgPlayer: string) => msgPlayer === playerName;

    return (
        <div className="flex flex-col h-full">
            {/* Messages container */}
            <div className="flex-grow overflow-y-auto mb-4 space-y-6 p-4">
                {messages.map((msg, index) => (
                    <div key={index} className="space-y-2">
                        {/* System message - full width */}
                        {!isCurrentPlayer(msg.player) && (
                            <div className="w-full bg-gray-800 rounded-lg p-4 shadow-lg">
                                <div className="flex items-center justify-between mb-2">
                                    <span className="text-gray-400 font-medium">
                                        {msg.player}
                                    </span>
                                    <div className="flex items-center text-xs text-gray-500">
                                        <Clock className="w-3 h-3 mr-1"/>
                                        {msg.timestamp}
                                    </div>
                                </div>
                                <p className="text-gray-100">{msg.content}</p>
                            </div>
                        )}

                        {/* User message - post-it note style */}
                        {isCurrentPlayer(msg.player) && (
                            <div className="flex justify-end">
                                <div className="max-w-[80%] ml-8">
                                    <div
                                        className="bg-yellow-100 rounded-lg p-4 shadow-lg transform -rotate-1 relative">
                                        {/* Fake tape effect */}
                                        <div
                                            className="absolute -top-2 left-1/2 w-8 h-2 bg-gray-200/50 rounded transform -translate-x-1/2 rotate-1"/>

                                        <div className="flex items-center justify-between mb-2">
                                            <span className="text-gray-700 font-medium">
                                                {msg.player}
                                            </span>
                                            <div className="flex items-center text-xs text-gray-600">
                                                <Clock className="w-3 h-3 mr-1"/>
                                                {msg.timestamp}
                                            </div>
                                        </div>
                                        <p className="text-gray-800">{msg.content}</p>
                                    </div>
                                </div>
                            </div>
                        )}
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
                    className="flex-1 bg-gray-900 text-gray-100 rounded-lg px-4 py-2 border border-gray-700
                             focus:outline-none focus:border-blue-500 placeholder-gray-500"
                    placeholder="Type a message..."
                />
                <button
                    type="submit"
                    className="bg-blue-600 text-white p-2 rounded-lg hover:bg-blue-700
                             transition-colors duration-200 flex items-center justify-center"
                    disabled={!input.trim()}
                >
                    <Send className="w-5 h-5"/>
                </button>
            </form>
        </div>
    );
};

export default GameConsole;