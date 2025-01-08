import {defineConfig} from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig(({mode}) => {
    const wsUrl = mode === 'production'
        ? process.env.VITE_WS_URL || 'wss://scrabble-j2qi.onrender.com'
        : process.env.VITE_WS_URL || 'ws://localhost:8080';

    return {
        plugins: [react()],
        server: {
            proxy: {
                '/lobby': {
                    target: wsUrl,
                    ws: true,
                },
                '/game': {
                    target: wsUrl,
                    ws: true,
                }
            }
        }
    }
})