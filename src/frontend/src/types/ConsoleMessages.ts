export type WebSocketMessage =
    | JoinMessage
    | GameMessage
    | GameInput;

interface JoinMessage {
    type: 'JOIN';
    name: string;
}

export interface GameMessage {
    type: 'MESSAGE';
    player: string;
    content: string;
}

interface GameInput {
    type: 'INPUT';
    player: string;
    content: string;
}