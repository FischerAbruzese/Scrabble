export interface Player {
    // Add player properties based on your Player class
    isHandEmpty: () => boolean;
}

export interface Board {
    // Add board properties based on your Board class
}

export interface GameState {
    players: Player[];
    board: Board;
    turnNum: number;
    passStreak: number;
}