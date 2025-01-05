/**
 * Represents all possible WebSocket message types that can be sent/received
 */
export type WebSocketMessage =
    | CreateGameMessage
    | JoinGameMessage
    | StartGameMessage
    | GameStateMessage
    | GameMessage
    | GameInputMessage;

/**
 * Message sent to create a new game
 */
export interface CreateGameMessage {
    type: 'CREATE_GAME';
    name: string;
}

/**
 * Message sent to join an existing game
 */
export interface JoinGameMessage {
    type: 'JOIN_GAME';
    gameId: string;
    name: string;
}

/**
 * Message sent to start a game
 */
export interface StartGameMessage {
    type: 'START_GAME';
    gameId: string;
}

/**
 * Message containing the current game state
 */
export interface GameStateMessage {
    type: 'GAME_STATE';
    gameId: string;
    players: string[];
    isStarted: boolean;
}

/**
 * Message for in-game chat/notifications
 */
export interface GameMessage {
    type: 'MESSAGE';
    player: string;
    content: string;
}

/**
 * Message for game inputs (moves, actions)
 */
export interface GameInputMessage {
    type: 'GAME_INPUT';
    player: string;
    content: string;
}