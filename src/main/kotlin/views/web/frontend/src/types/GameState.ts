export enum Multiplier {
    NORMAL = 'NORMAL',
    DOUBLE_LETTER = 'DOUBLE_LETTER',
    TRIPLE_LETTER = 'TRIPLE_LETTER',
    DOUBLE_WORD = 'DOUBLE_WORD',
    TRIPLE_WORD = 'TRIPLE_WORD'
}

// Base interfaces
export interface Piece {
    letter: string;
    value: number;
}

export interface Hand {
    pieces: Piece[];
}

export interface Player {
    name: string;
    hand: Hand;
    score: number;
}

export interface Square {
    multiplier: Multiplier;
    piece: Piece | null;
    turnPlaced: number | null;
    playerPlaced: Player | null;
}

export interface Board {
    squares: Square[][];
}

export interface GameState {
    players: Player[];
    board: Board;
    turnNum: number;
    passStreak: number;
}