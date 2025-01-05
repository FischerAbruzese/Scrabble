// GameState.ts
export interface GameState {
    players: Player[];
    board: Board;
    turnNum: number;
    passStreak: number;
    yourHand: Hand;
}

export interface Player {
    name: string;
    score: number;
}

export interface Hand {
    pieces: Piece[];
}

export interface Board {
    squares: Square[][];
}

export interface Square {
    multiplier: Multiplier;
    piece: Piece | null;
    turnPlaced: number | null;
    playerPlaced: Player | null;
}

export interface Piece {
    letter: string;
    value: number;
}

export enum Multiplier {
    NONE = "NONE",
    DOUBLE_LETTER = "DOUBLE_LETTER",
    TRIPLE_LETTER = "TRIPLE_LETTER",
    DOUBLE_WORD = "DOUBLE_WORD",
    TRIPLE_WORD = "TRIPLE_WORD"
}