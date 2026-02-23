# EcoTrekk Admin Frontend Specification
## Complete Developer Handoff Document

---

## 1. Overview

The EcoTrekk Admin Frontend is a secure web-based administrative dashboard designed to manage and operate the EcoTrekk outdoor gear rental platform. This system connects to the Spring Boot backend via REST APIs and enables authorized staff to control inventory, monitor orders, manage users, and configure operational settings.

The application must be production-ready, scalable, and maintainable.

---

## 2. Target Users & Role-Based Access Control (RBAC)

### Roles

**SUPER_ADMIN**
- Full access to all modules
- Manage roles and permissions
- Access system settings

**INVENTORY_MANAGER**
- Manage gear inventory
- Manage rental hubs

**ORDER_MANAGER**
- Manage order lifecycle
- Update order statuses

**SUPPORT_AGENT**
- View users
- View orders
- Limited modification rights

Role-based access must be enforced:
- On frontend route level
- On backend authorization level

---

## 3. Recommended Technology Stack

- React (TypeScript recommended)
- Vite (Build tool)
- React Router
- Axios (API client)
- TanStack React Query (API caching & mutations)
- React Hook Form + Zod (Form validation)
- Tailwind CSS or MUI / Ant Design (UI framework)
- Day.js (Date handling)
- Recharts (Analytics charts, optional)
- ESLint + Prettier

---

## 4. Project Structure
src/
├── api/
├── components/
├── layouts/
├── pages/
│ ├── dashboard/
│ ├── inventory/
│ ├── orders/
│ ├── users/
│ └── settings/
├── routes/
├── hooks/
├── context/
└── utils/

---

## 5. Authentication & Security

### Authentication Options

**Option A: Firebase Authentication**
- Admin logs in via Firebase
- Firebase ID token sent in `Authorization: Bearer <token>`
- Backend verifies token and role

**Option B: Backend JWT Authentication**
- Login endpoint returns JWT
- Prefer HttpOnly cookies for security

### Security Requirements

- Protected routes
- Role-based UI visibility
- 401 response interceptor
- Auto logout on token expiration
- No sensitive tokens in localStorage (prefer HttpOnly cookies)
- Global Axios interceptor for token injection

---

## 6. Layout Requirements

### Main Layout

- Collapsible sidebar navigation
- Top header (profile dropdown + logout)
- Main content area
- Responsive layout for tablet and desktop
- Drawer menu for mobile

### Navigation Routes
/login
/dashboard
/inventory/gears
/inventory/gears/new
/inventory/gears/:id/edit
/inventory/hubs
/orders
/orders/:id
/users
/reports
/settings

---

## 7. Module Specifications

---

# A. Dashboard

### Purpose
Provide operational overview.

### Widgets
- Total orders (today)
- Pending orders count
- Low stock items count
- Revenue summary (if applicable)
- Quick action buttons

### API
GET /api/admin/metrics/summary

---

# B. Gear Inventory Management

## B1. Gear List Page

### Columns
- Image thumbnail
- Name
- Category
- Price per day
- Stock
- Status
- Last updated
- Actions (Edit/Delete)

### Features
- Search by name
- Filter by category and status
- Pagination (server-side)
- Sorting
- Bulk activate/deactivate
- Highlight low stock items

### APIs
GET /api/admin/gears
PATCH /api/admin/gears/:id/status
DELETE /api/admin/gears/:id

---

## B2. Gear Create / Edit Page

### Form Fields
- Name (required)
- Category (required)
- Description
- Price per day (> 0 required)
- Stock (>= 0 required)
- Status (active/inactive)
- Image upload or URL
- Video URL (optional)

### Validation Rules
- Name length >= 3
- Price positive
- Stock non-negative
- Category required

### APIs
POST /api/admin/gears
PUT /api/admin/gears/:id

---

# C. Rental Hub Management

## C1. Hub List Page

### Columns
- Name
- Address
- Phone
- Latitude
- Longitude
- Actions

### Features
- Add/Edit/Delete hubs
- Map preview modal (Google Maps embed)
- Lat/Lng validation

### APIs
GET /api/admin/hubs
POST /api/admin/hubs
PUT /api/admin/hubs/:id
DELETE /api/admin/hubs/:id

---

# D. Order Management

## D1. Orders List Page

### Columns
- Order ID
- Customer
- Hub
- Start date
- End date
- Status
- Created at
- Actions

### Features
- Filter by status
- Date range filter
- Search by order ID or customer
- Pagination
- Status badges with color coding

### API
GET /api/admin/orders

---

## D2. Order Details Page

### Sections
- Order summary
- Customer information
- Item list
- Timeline/status history
- Action panel

### Status Transitions

| Current Status | Allowed Actions |
|---------------|----------------|
| PENDING | Confirm / Cancel |
| CONFIRMED | Mark Picked Up |
| PICKED_UP | Mark Returned |
| CANCELLED | Read-only |

### APIs
GET /api/admin/orders/:id
PATCH /api/admin/orders/:id/status

---

# E. User Management

## E1. User List Page

### Columns
- User ID
- Name
- Email
- Status
- Created at
- Actions

### Features
- Search by email/name
- Filter by active/inactive
- Activate/deactivate user

### APIs
GET /api/admin/users
PATCH /api/admin/users/:id/status

---

# F. Reports & Analytics (Optional)

### Metrics
- Orders per week/month
- Revenue trends
- Top rented items
- Hub performance

### Visualizations
- Line charts
- Bar charts
- Pie charts

---

# G. Settings (Optional)

- Notification templates
- Low stock threshold
- Business hours
- Support contact information

---

## 8. API Integration Standards

- Centralized Axios instance
- Base URL from environment variable
- Global request interceptor
- Global error interceptor
- Toast notifications for success/error
- Skeleton loaders during API fetch
- Retry option on failure
- Server-side pagination

---

## 9. UI/UX Requirements

- Clean and consistent design system
- Confirmation modals for destructive actions
- Toast notifications
- Clear loading states
- Clear empty states
- Accessible forms
- Responsive layout
- Minimal re-renders (use React Query)

---

## 10. Non-Functional Requirements

- Page load under 2 seconds
- Secure token handling
- Server-side pagination
- Clean component architecture
- Modular and reusable components
- Maintainable folder structure

---

## 11. Reusable Components

- DataTable
- ConfirmDialog
- StatusBadge
- FormInput
- FormSelect
- ImageUploader
- ProtectedRoute
- PageHeader

---

## 12. Environment Variables
VITE_API_BASE_URL=https://192.168.1.10:3000/ecotrekk-api/

VITE_FIREBASE_CONFIG=...

---

## 13. Acceptance Criteria

- Admin login works
- Role-based route protection enforced
- Gear CRUD fully functional
- Hub CRUD functional
- Order lifecycle updates correctly
- User activation/deactivation works
- Proper loading and error states handled
- Responsive UI

---

## 14. Deliverables

1. Fully functional React Admin application
2. Clean and documented codebase
3. Integrated with Spring Boot backend
4. Role-based authentication implemented
5. Production-ready build configuration

---

## 15. Future Enhancements

- Real-time order updates via WebSockets
- Audit log viewer
- Export reports (CSV/PDF)
- Dark mode
- Multi-language support
- Advanced analytics dashboard

---

End of Document  
EcoTrekk Admin Frontend – Full Technical Specification