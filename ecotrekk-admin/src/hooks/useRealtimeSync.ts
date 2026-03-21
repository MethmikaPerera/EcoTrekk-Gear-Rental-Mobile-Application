import { useEffect, useRef } from 'react';
import { useQueryClient } from '@tanstack/react-query';
import { useWebSocket, RealtimeEvent } from './useWebSocket';

/**
 * Hook to automatically sync React Query cache with WebSocket real-time events
 * Invalidates relevant queries when events are received
 */
export const useRealtimeSync = () => {
    const queryClient = useQueryClient();
    const { isConnected, subscribe, connect } = useWebSocket();
    const subscribedRef = useRef(false);

    useEffect(() => {
        // Ensure WebSocket is connected
        if (!isConnected) {
            connect();
            return;
        }

        // Only subscribe once per mount
        if (subscribedRef.current) {
            return;
        }

        subscribedRef.current = true;

        // Subscribe to order updates
        const unsubOrders = subscribe('orders', (event: RealtimeEvent) => {
            console.log('📦 Order update:', event);
            // Invalidate order-related queries
            queryClient.invalidateQueries({ queryKey: ['dashboard-recent-orders'] });
            queryClient.invalidateQueries({ queryKey: ['orders', 'list'] });
            queryClient.invalidateQueries({ queryKey: ['orders'] });
            queryClient.invalidateQueries({ queryKey: ['dashboard-metrics'] });
        });

        // Subscribe to gear updates
        const unsubGears = subscribe('gears', (event: RealtimeEvent) => {
            console.log('🎒 Gear update:', event);
            queryClient.invalidateQueries({ queryKey: ['gears', 'list'] });
            queryClient.invalidateQueries({ queryKey: ['gears'] });
            queryClient.invalidateQueries({ queryKey: ['dashboard-metrics'] });
        });

        // Subscribe to user updates
        const unsubUsers = subscribe('users', (event: RealtimeEvent) => {
            console.log('👥 User update:', event);
            queryClient.invalidateQueries({ queryKey: ['users', 'list'] });
            queryClient.invalidateQueries({ queryKey: ['users'] });
            queryClient.invalidateQueries({ queryKey: ['admins'] });
        });

        // Subscribe to hub updates
        const unsubHubs = subscribe('hubs', (event: RealtimeEvent) => {
            console.log('🏢 Hub update:', event);
            queryClient.invalidateQueries({ queryKey: ['hubs', 'list'] });
            queryClient.invalidateQueries({ queryKey: ['hubs'] });
            queryClient.invalidateQueries({ queryKey: ['my-hub'] });
        });

        // Subscribe to metrics updates
        const unsubMetrics = subscribe('metrics', (event: RealtimeEvent) => {
            console.log('📊 Metrics update:', event);
            queryClient.invalidateQueries({ queryKey: ['dashboard-metrics'] });
            queryClient.invalidateQueries({ queryKey: ['reports'] });
        });

        // Cleanup subscriptions on unmount
        return () => {
            unsubOrders();
            unsubGears();
            unsubUsers();
            unsubHubs();
            unsubMetrics();
            subscribedRef.current = false;
        };
    }, [isConnected, subscribe, connect, queryClient]);

    return { isConnected };
};
