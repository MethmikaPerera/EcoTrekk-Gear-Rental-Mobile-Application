import React, { useEffect } from 'react';
import { useWebSocket } from '../hooks/useWebSocket';

interface WebSocketProviderProps {
    children: React.ReactNode;
}

/**
 * Provider component that manages WebSocket connection lifecycle
 * Wraps the entire application to provide real-time capabilities
 */
export const WebSocketProvider: React.FC<WebSocketProviderProps> = ({ children }) => {
    const { connect, disconnect } = useWebSocket();

    useEffect(() => {
        // Connect to WebSocket when provider mounts
        connect();

        // Cleanup on unmount
        return () => {
            disconnect();
        };
    }, [connect, disconnect]);

    // Render children immediately (connection happens in background)
    return <>{children}</>;
};

