# Real-Time Data Synchronization Guide

## Overview
The EcoTrekk Admin frontend now supports real-time data synchronization using WebSocket + STOMP messaging. When data is updated on the backend, admin clients are automatically notified and their data is refreshed without manual intervention.

## Architecture

### Backend (Spring Boot)
- **WebSocket Config**: `config/WebSocketConfig.java`
  - Enables STOMP messaging over WebSocket
  - Endpoint: `/api/ws/realtime`
  - Message broker: Simple in-memory broker

- **Real-Time Event Publisher**: `service/RealtimeEventPublisher.java`
  - Service interface for broadcasting events
  - Used by services to notify connected clients
  - Topics: `orders`, `gears`, `users`, `hubs`, `metrics`

- **Event DTOs**: `dto/websocket/RealtimeEvent.java`
  - Generic event wrapper with `eventType`, `timestamp`, and `data`

### Frontend (React)
- **WebSocket Hook**: `src/hooks/useWebSocket.ts`
  - Manages STOMP client configuration
  - Handles connection/reconnection logic
  - Provides `subscribe()` method for topic subscriptions

- **Real-Time Sync Hook**: `src/hooks/useRealtimeSync.ts`
  - Subscribes to WebSocket topics
  - Automatically invalidates React Query cache on events
  - Triggers data refetch automatically

- **WebSocket Provider**: `src/context/WebSocketProvider.tsx`
  - Wraps application with WebSocket connection lifecycle
  - Ensures connection is established on app load

## How It Works

1. **Backend Event Publishing**
   ```java
   // In any service when data is updated:
   realtimeEventPublisher.publishOrderEvent("ORDER_UPDATED", updatedOrder);
   ```

2. **Frontend Event Reception**
   - WebSocket connection is established when app loads
   - Event topics are subscribed to in AdminLayout
   - When an event is received, React Query queries are invalidated
   - Components automatically refetch fresh data

3. **Automatic UI Updates**
   - Components using React Query hooks re-render with fresh data
   - No manual refresh or polling needed
   - Updates happen silently in the background

## Implementation Checklist

### ✅ Completed
- [x] Backend WebSocket configuration
- [x] Real-time event publisher service
- [x] Order service integration
- [x] Frontend WebSocket hook
- [x] React Query integration hook
- [x] WebSocket provider
- [x] AdminLayout integration
- [x] App.tsx integration

### 📋 To Complete (Optional Enhancements)
- [ ] Add event publishing to GearService
- [ ] Add event publishing to UserService (AdminService)
- [ ] Add event publishing to HubService
- [ ] Add event publishing to MetricsService
- [ ] Add browser notifications on critical events
- [ ] Add event history/audit log features
- [ ] Add throttling to prevent excessive updates

## Adding Real-Time Events to Other Services

### Example: GearService

```java
// src/main/java/com/akigen/ecotrekk_backend/service/impl/GearServiceImpl.java

@Service
@RequiredArgsConstructor
public class GearServiceImpl implements GearService {
    private final GearRepository gearRepository;
    private final RealtimeEventPublisher realtimeEventPublisher;

    public Gear createGear(CreateGearRequest request) {
        Gear gear = new Gear();
        // ... populate gear ...
        gear = gearRepository.save(gear);
        
        // Publish real-time event
        realtimeEventPublisher.publishGearEvent("GEAR_CREATED", gear);
        return gear;
    }

    public Gear updateGear(Long id, UpdateGearRequest request) {
        Gear gear = gearRepository.findById(id).orElseThrow();
        // ... update gear ...
        gear = gearRepository.save(gear);
        
        // Publish real-time event
        realtimeEventPublisher.publishGearEvent("GEAR_UPDATED", gear);
        return gear;
    }

    public void deleteGear(Long id) {
        Gear gear = gearRepository.findById(id).orElseThrow();
        gearRepository.delete(gear);
        
        // Publish real-time event
        realtimeEventPublisher.publishGearEvent("GEAR_DELETED", gear);
    }
}
```

## Frontend Usage Examples

### Using Real-Time Sync (Automatic)
```tsx
// In components using React Query, events are handled automatically:
const { data: orders } = useQuery({
    queryKey: ['orders'],
    queryFn: getOrders,
});

// Data automatically updates when ORDERS topic emits events
```

### Listening to Specific Events
```tsx
// If you need to react to specific events:
import { useWebSocket } from '../hooks/useWebSocket';

export const OrderMonitor = () => {
    const { subscribe } = useWebSocket();

    useEffect(() => {
        const unsubscribe = subscribe('orders', (event) => {
            if (event.eventType === 'ORDER_UPDATED') {
                console.log('Order updated:', event.data);
                // Handle custom logic
            }
        });

        return unsubscribe;
    }, [subscribe]);

    return <div>Monitoring orders...</div>;
};
```

## Event Types

### Orders Topic
- `ORDER_CREATED`: New order placed
- `ORDER_UPDATED`: Order status changed
- `ORDER_CANCELLED`: Order cancelled

### Gears Topic
- `GEAR_CREATED`: New gear added
- `GEAR_UPDATED`: Gear details modified
- `GEAR_DELETED`: Gear removed

### Users Topic
- `USER_CREATED`: New admin user
- `USER_UPDATED`: User details changed
- `USER_STATUS_CHANGED`: User activated/deactivated

### Hubs Topic
- `HUB_CREATED`: New hub created
- `HUB_UPDATED`: Hub details changed
- `HUB_DELETED`: Hub removed

### Metrics Topic
- `METRICS_UPDATED`: Dashboard metrics changed

## Debugging

### Enable Console Logging
The WebSocket hook includes console logs for debugging:
```
✓ WebSocket connected
✗ WebSocket disconnected
📦 Order update: {...}
🎒 Gear update: {...}
👥 User update: {...}
🏢 Hub update: {...}
📊 Metrics update: {...}
```

### Check Connection Status
```tsx
// In browser DevTools console:
// Check connection status in network tab under "WS" protocol
```

### Common Issues

**WebSocket not connecting?**
- Check CORS configuration in backend
- Verify WebSocket endpoint URL
- Check token in Authorization header

**Updates not appearing?**
- Check React Query staleTime (default: 30s)
- Verify event publishing in backend
- Check browser console for WebSocket errors

## Performance Considerations

1. **Reduce Unnecessary Updates**
   - React Query cache invalidation is smart—only affected queries refetch
   - Use selective query key patterns to avoid over-invalidation

2. **Handle High-Frequency Updates**
   - For metrics that update frequently, consider adding client-side caching
   - Use `refetchInterval` with longer intervals if too many updates

3. **Network Optimization**
   - SockJS fallback ensures compatibility with restrictive networks
   - Automatic reconnection with exponential backoff

## Production Deployment

1. **Update WebSocket Endpoint URL**
   ```bash
   # .env
   VITE_API_BASE_URL=https://api.example.com
   ```

2. **Enable HTTPS/WSS**
   - WebSocket automatically converts to `wss://` for HTTPS

3. **Configure Backend CORS**
   - Update allowed origins in `WebSocketConfig.java`

4. **Monitor WebSocket Connections**
   - Track active connections on backend
   - Monitor message throughput

## Future Enhancements

1. **Message Throttling**
   - Prevent overwhelming clients with too many updates

2. **Broadcast Filtering**
   - Only send updates to users who have access to that data

3. **Event Persistence**
   - Store events for audit/replay capabilities

4. **Selective Subscriptions**
   - Allow clients to subscribe only to relevant topics

5. **Client-Side Notifications**
   - Toast notifications for critical events
   - Browser notifications for important updates
