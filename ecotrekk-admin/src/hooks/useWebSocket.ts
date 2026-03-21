import { useCallback, useEffect, useState } from 'react';
import { Client, IMessage } from '@stomp/stompjs';

export interface RealtimeEvent {
    eventType: string;
    timestamp: string;
    data: Record<string, unknown>;
}

type EventCallback = (event: RealtimeEvent) => void;

type SubscriptionMap = Map<string, () => void>;
type ConnectionListener = (connected: boolean) => void;

// Global WebSocket client instance and subscriptions to avoid duplicate sockets
let globalWebSocketClient: Client | null = null;
const globalSubscriptions: SubscriptionMap = new Map();
const connectionListeners = new Set<ConnectionListener>();
let globalIsConnected = false;

const notifyConnectionListeners = (connected: boolean) => {
    globalIsConnected = connected;
    connectionListeners.forEach((listener) => listener(connected));
};

const buildWebSocketUrl = () => {
    const apiBaseUrl = (import.meta.env.VITE_API_BASE_URL || 'http://localhost:8081').trim();
    const normalizedApiBase = apiBaseUrl.replace(/\/+$/, '').endsWith('/api')
        ? apiBaseUrl.replace(/\/+$/, '')
        : `${apiBaseUrl.replace(/\/+$/, '')}/api`;
    return normalizedApiBase.replace(/^http/i, 'ws') + '/ws/realtime';
};

/**
 * WebSocket hook that manages STOMP connection and subscriptions
 * Automatically reconnects on disconnect
 */
export const useWebSocket = () => {
    const [client, setClient] = useState<Client | null>(globalWebSocketClient);
    const [isConnected, setIsConnected] = useState<boolean>(globalIsConnected);

    // Keep all hook instances in sync with shared websocket connection state.
    useEffect(() => {
        const listener: ConnectionListener = (connected) => setIsConnected(connected);
        connectionListeners.add(listener);
        return () => connectionListeners.delete(listener);
    }, []);
    
    const connect = useCallback(() => {
        // Use global instance to prevent multiple connections
        if (globalWebSocketClient?.connected) {
            setClient(globalWebSocketClient);
            notifyConnectionListeners(true);
            return;
        }

        if (globalWebSocketClient) {
            globalWebSocketClient.activate();
            setClient(globalWebSocketClient);
            return;
        }

        const webSocketUrl = buildWebSocketUrl();

        const token = sessionStorage.getItem('ecotrekk_token');

        const client = new Client({
            brokerURL: webSocketUrl,
            connectHeaders: {
                Authorization: token ? `Bearer ${token}` : '',
            },
            onConnect: () => {
                console.log('✓ WebSocket connected');
                notifyConnectionListeners(true);
            },
            onDisconnect: () => {
                console.log('✗ WebSocket disconnected');
                notifyConnectionListeners(false);
            },
            onStompError: (frame) => {
                console.error('STOMP error:', frame.body);
            },
            onWebSocketError: (evt) => {
                console.error('WebSocket error:', evt);
            },
            reconnectDelay: 5000,
            heartbeatIncoming: 4000,
            heartbeatOutgoing: 4000,
            forceBinaryWSFrames: true,
            appendMissingNULLonIncoming: true,
        });

        globalWebSocketClient = client;
        setClient(client);
        client.activate();
    }, []);

    const subscribe = useCallback((topic: string, callback: EventCallback) => {
        const client = globalWebSocketClient;
        if (!client?.connected) {
            console.warn(`WebSocket not connected, cannot subscribe to ${topic}`);
            connect();
            return () => {};
        }

        const unsubscribe = client.subscribe(`/topic/${topic}`, (message: IMessage) => {
            try {
                const event = JSON.parse(message.body) as RealtimeEvent;
                callback(event);
            } catch (error) {
                console.error(`Error parsing WebSocket message from ${topic}:`, error);
            }
        });

        const unsubscribeFunc = () => {
            unsubscribe.unsubscribe();
        };

        globalSubscriptions.set(topic, unsubscribeFunc);

        return unsubscribeFunc;
    }, [connect]);

    const disconnect = useCallback(() => {
        globalSubscriptions.forEach((unsubscribe) => {
            unsubscribe();
        });
        globalSubscriptions.clear();

        if (globalWebSocketClient?.connected) {
            globalWebSocketClient.deactivate();
        }
        notifyConnectionListeners(false);
    }, []);

    return {
        client,
        isConnected,
        connect,
        subscribe,
        disconnect,
    };
};

