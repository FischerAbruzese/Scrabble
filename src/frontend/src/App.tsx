// import React from 'react';
import './App.css'

import { GameBoard } from './components/GameBoard';

function App() {
    return (
        <div className="min-h-screen bg-gray-100">
            <div className="container mx-auto py-8">
                <h1 className="text-3xl font-bold mb-6">Game Board</h1>
                <GameBoard />
            </div>
        </div>
    );
}

export default App;
