-- ============================================================================
-- EcoTrekk sample data (development / demo only)
--
-- Fills: hubs, carts, cart_items, wishlists, wishlist_items, invoices,
--        invoice_items, notifications
-- Does NOT touch: customers, admin_users (users) or gears, gear_categories,
--        gear_images, inventory (gears). It only REFERENCES existing rows.
-- Skipped on purpose: hub_managers (needs staff admin accounts, i.e. users),
--        customer_device_tokens (fake FCM tokens would make push sends fail).
--
-- Safe to run more than once: every insert is guarded (NOT EXISTS / INSERT IGNORE
-- on a unique key), and everything runs in one transaction.
--
-- Existing customers used (by id):  1 Methmika Perera, 2 Thisath Sandeep,
--                                   3 Sasandara de Zoysa (inactive), 4 Waidurya Wijeratne
-- Hubs 1 (Colombo) and 2 (Ella) already exist; three more are added below.
--
-- Stock note: the backend deducts inventory when an order is created and returns it
-- only on COMPLETED / CANCELLED. To avoid editing the inventory table, active sample
-- orders (PENDING..PICKED_UP) are stored with inventory_allocated = 0, so completing
-- or cancelling them later changes no stock. Finished orders are consistent already.
-- ============================================================================

USE `ecotrekk`;
SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;

START TRANSACTION;

-- ---------------------------------------------------------------------------
-- 1. Hubs (3 extra pickup locations; images reuse the existing hub photos)
-- ---------------------------------------------------------------------------
INSERT INTO hubs (name, address, phone, latitude, longitude, map_location_url, image_url, is_active)
SELECT s.name, s.address, s.phone, s.latitude, s.longitude, s.map_location_url, src.image_url, 1
FROM (
    SELECT 'Kandy Hub' AS name, 'No 12, Peradeniya Road, Kandy' AS address, '0812223344' AS phone,
           CAST(7.2906000 AS DECIMAL(10,7)) AS latitude, CAST(80.6337000 AS DECIMAL(10,7)) AS longitude,
           'https://www.google.com/maps?q=7.2906,80.6337' AS map_location_url, 'Ella Hub' AS img_from
    UNION ALL
    SELECT 'Galle Hub', '45, Lighthouse Street, Galle Fort, Galle', '0912234455',
           6.0329000, 80.2170000, 'https://www.google.com/maps?q=6.0329,80.2170', 'Colombo Hub'
    UNION ALL
    SELECT 'Nuwara Eliya Hub', '28, Lake Drive, Nuwara Eliya', '0522222333',
           6.9497000, 80.7891000, 'https://www.google.com/maps?q=6.9497,80.7891', 'Ella Hub'
) s
JOIN hubs src ON src.name = s.img_from
WHERE NOT EXISTS (SELECT 1 FROM hubs h WHERE h.name = s.name);

-- ---------------------------------------------------------------------------
-- 2. Carts and cart items (customers 2 and 4; customer 1's cart is left empty for demos)
-- ---------------------------------------------------------------------------
INSERT IGNORE INTO carts (customer_id, start_date, end_date) VALUES
    (2, '2026-10-12', '2026-10-14'),
    (4, '2026-10-15', '2026-10-17');

INSERT INTO cart_items (cart_id, hub_id, gear_id, qty)
SELECT ct.id, h.id, g.id, s.qty
FROM (
    SELECT 2 AS customer_id, 'Colombo Hub' AS hub_name, 'Portable Camping Stove' AS gear_name, 1 AS qty
    UNION ALL SELECT 2, 'Colombo Hub', 'Sleeping Bag', 2
    UNION ALL SELECT 4, 'Ella Hub', 'Trekking Backpack 75L', 1
    UNION ALL SELECT 4, 'Ella Hub', 'Hiking Poles', 2
) s
JOIN carts ct ON ct.customer_id = s.customer_id
JOIN hubs  h  ON h.name = s.hub_name
JOIN gears g  ON g.name = s.gear_name
WHERE NOT EXISTS (SELECT 1 FROM cart_items x WHERE x.cart_id = ct.id AND x.gear_id = g.id);

-- ---------------------------------------------------------------------------
-- 3. Wishlists and wishlist items (customers 1, 2, 4)
-- ---------------------------------------------------------------------------
INSERT IGNORE INTO wishlists (customer_id) VALUES (1), (2), (4);

INSERT INTO wishlist_items (wishlist_id, gear_id)
SELECT w.id, g.id
FROM (
    SELECT 1 AS customer_id, 'Trekking Backpack 50L' AS gear_name
    UNION ALL SELECT 1, 'Mountain Bike'
    UNION ALL SELECT 1, 'Adventure Drone with 4K Camera'
    UNION ALL SELECT 2, 'Inflatable Kayak'
    UNION ALL SELECT 2, '2 Person Camping Tent'
    UNION ALL SELECT 2, 'GoPro Action Camera'
    UNION ALL SELECT 4, '6 Person Camping Tent'
    UNION ALL SELECT 4, 'Snorkeling Set'
    UNION ALL SELECT 4, 'Hydration Pack'
) s
JOIN wishlists w ON w.customer_id = s.customer_id
JOIN gears g ON g.name = s.gear_name
WHERE NOT EXISTS (SELECT 1 FROM wishlist_items x WHERE x.wishlist_id = w.id AND x.gear_id = g.id);

-- ---------------------------------------------------------------------------
-- 4. Invoices (orders). Totals are filled in from the items in step 5.
--    Spread over Aug - Oct 2026 so the admin dashboard and reports have data.
-- ---------------------------------------------------------------------------
INSERT IGNORE INTO invoices
    (invoice_code, customer_id, collection_hub_id, status, payment_status, payment_method,
     subtotal, deposit_total, total_amount, inventory_allocated, created_at, updated_at)
SELECT s.code, s.customer_id, h.id, s.status, s.payment_status, s.payment_method,
       0, 0, 0, b'0', s.created_at, s.updated_at
FROM (
    SELECT 'INV-7F3A91C2' AS code, 3 AS customer_id, 'Colombo Hub' AS hub_name, 'COMPLETED' AS status, 'PAID' AS payment_status, 'CARD' AS payment_method,
           CAST('2026-08-12 09:15:00' AS DATETIME) AS created_at, CAST('2026-08-17 16:40:00' AS DATETIME) AS updated_at
    UNION ALL SELECT 'INV-2D84E6B0', 2, 'Ella Hub',         'COMPLETED', 'PAID',     'PAY_AT_HUB', '2026-08-20 11:05:00', '2026-08-25 18:10:00'
    UNION ALL SELECT 'INV-91C5D07A', 4, 'Kandy Hub',        'COMPLETED', 'PAID',     'CARD',       '2026-08-28 14:30:00', '2026-09-01 12:00:00'
    UNION ALL SELECT 'INV-5B0E3F18', 1, 'Galle Hub',        'COMPLETED', 'PAID',     'CARD',       '2026-09-05 08:45:00', '2026-09-09 17:20:00'
    UNION ALL SELECT 'INV-C46A82E5', 2, 'Colombo Hub',      'CANCELLED', 'REFUNDED', 'CARD',       '2026-09-10 19:20:00', '2026-09-10 20:05:00'
    UNION ALL SELECT 'INV-08E7B4D3', 4, 'Nuwara Eliya Hub', 'COMPLETED', 'PAID',     'PAY_AT_HUB', '2026-09-14 10:10:00', '2026-09-18 15:35:00'
    UNION ALL SELECT 'INV-E19D5A66', 1, 'Ella Hub',         'COMPLETED', 'PAID',     'CARD',       '2026-09-20 07:55:00', '2026-09-24 13:00:00'
    UNION ALL SELECT 'INV-34B8F0C9', 2, 'Kandy Hub',        'CANCELLED', 'UNPAID',   'PAY_AT_HUB', '2026-09-25 16:40:00', '2026-09-26 09:15:00'
    UNION ALL SELECT 'INV-A7625DE1', 4, 'Colombo Hub',      'COMPLETED', 'PAID',     'CARD',       '2026-10-01 12:25:00', '2026-10-04 11:45:00'
    UNION ALL SELECT 'INV-6C0B93F4', 2, 'Ella Hub',         'PICKED_UP', 'PAID',     'CARD',       '2026-10-05 09:30:00', '2026-10-06 10:00:00'
    UNION ALL SELECT 'INV-D2F51A87', 4, 'Galle Hub',        'READY',     'UNPAID',   'PAY_AT_HUB', '2026-10-06 15:05:00', '2026-10-07 09:20:00'
    UNION ALL SELECT 'INV-B3E40C59', 1, 'Kandy Hub',        'APPROVED',  'PAID',     'CARD',       '2026-10-07 18:40:00', '2026-10-07 19:10:00'
    UNION ALL SELECT 'INV-4F9A17E2', 2, 'Nuwara Eliya Hub','PENDING',   'UNPAID',   'PAY_AT_HUB', '2026-10-08 08:50:00', '2026-10-08 08:50:00'
    UNION ALL SELECT 'INV-80D6C3BA', 4, 'Colombo Hub',      'PENDING',   'PAID',     'CARD',       '2026-10-08 10:15:00', '2026-10-08 10:15:00'
) s
JOIN hubs h ON h.name = s.hub_name;

-- ---------------------------------------------------------------------------
-- 5. Invoice items (price and deposit are copied from the gear at insert time;
--    line_total = qty * daily price * rental days, days counted inclusively)
-- ---------------------------------------------------------------------------
INSERT INTO invoice_items
    (invoice_id, gear_id, qty, start_date, end_date, daily_rent_price, deposit_amount, line_total, deposit_total)
SELECT inv.id, g.id, s.qty, s.start_date, s.end_date,
       g.daily_rent_price, g.deposit_amount,
       g.daily_rent_price * s.qty * (DATEDIFF(s.end_date, s.start_date) + 1),
       g.deposit_amount * s.qty
FROM (
    SELECT 'INV-7F3A91C2' AS code, '6 Person Camping Tent' AS gear_name, 1 AS qty, CAST('2026-08-14' AS DATE) AS start_date, CAST('2026-08-16' AS DATE) AS end_date
    UNION ALL SELECT 'INV-7F3A91C2', 'Sleeping Bag',            2, '2026-08-14', '2026-08-16'
    UNION ALL SELECT 'INV-2D84E6B0', 'Trekking Backpack 50L',   1, '2026-08-22', '2026-08-24'
    UNION ALL SELECT 'INV-2D84E6B0', 'Hiking Poles',            2, '2026-08-22', '2026-08-24'
    UNION ALL SELECT 'INV-91C5D07A', 'Mountain Bike',           1, '2026-08-30', '2026-09-01'
    UNION ALL SELECT 'INV-91C5D07A', 'Cycling Helmet',          1, '2026-08-30', '2026-09-01'
    UNION ALL SELECT 'INV-5B0E3F18', 'Inflatable Kayak',        1, '2026-09-07', '2026-09-08'
    UNION ALL SELECT 'INV-5B0E3F18', 'Life Jacket',             2, '2026-09-07', '2026-09-08'
    UNION ALL SELECT 'INV-C46A82E5', 'Adventure Drone with 4K Camera', 1, '2026-09-12', '2026-09-13'
    UNION ALL SELECT 'INV-08E7B4D3', '2 Person Camping Tent',   1, '2026-09-15', '2026-09-17'
    UNION ALL SELECT 'INV-08E7B4D3', 'Portable Camping Stove',  1, '2026-09-15', '2026-09-17'
    UNION ALL SELECT 'INV-08E7B4D3', 'Head lamp',               2, '2026-09-15', '2026-09-17'
    UNION ALL SELECT 'INV-E19D5A66', 'GoPro Action Camera',     1, '2026-09-21', '2026-09-23'
    UNION ALL SELECT 'INV-E19D5A66', 'Action Camera Tripod',    1, '2026-09-21', '2026-09-23'
    UNION ALL SELECT 'INV-34B8F0C9', 'Sleeping Bag',            1, '2026-09-27', '2026-09-28'
    UNION ALL SELECT 'INV-A7625DE1', 'Trekking Backpack 75L',   1, '2026-10-02', '2026-10-04'
    UNION ALL SELECT 'INV-A7625DE1', 'Hydration Pack',          1, '2026-10-02', '2026-10-04'
    UNION ALL SELECT 'INV-6C0B93F4', 'Portable Camping Stove',  1, '2026-10-06', '2026-10-09'
    UNION ALL SELECT 'INV-6C0B93F4', 'Sleeping Bag',            2, '2026-10-06', '2026-10-09'
    UNION ALL SELECT 'INV-D2F51A87', 'Snorkeling Set',          2, '2026-10-09', '2026-10-10'
    UNION ALL SELECT 'INV-D2F51A87', 'Life Jacket',             2, '2026-10-09', '2026-10-10'
    UNION ALL SELECT 'INV-B3E40C59', 'Mountain Bike',           1, '2026-10-10', '2026-10-12'
    UNION ALL SELECT 'INV-B3E40C59', 'Cycling Gloves',          1, '2026-10-10', '2026-10-12'
    UNION ALL SELECT 'INV-4F9A17E2', '6 Person Camping Tent',   1, '2026-10-12', '2026-10-14'
    UNION ALL SELECT 'INV-80D6C3BA', 'Head lamp',               2, '2026-10-11', '2026-10-12'
    UNION ALL SELECT 'INV-80D6C3BA', 'Hiking Poles',            2, '2026-10-11', '2026-10-12'
) s
JOIN invoices inv ON inv.invoice_code = s.code
JOIN gears g ON g.name = s.gear_name
WHERE NOT EXISTS (SELECT 1 FROM invoice_items x WHERE x.invoice_id = inv.id AND x.gear_id = g.id);

-- Fill invoice totals from the items (sample invoices only; updated_at is kept as set above)
UPDATE invoices i
JOIN (
    SELECT invoice_id, SUM(line_total) AS sub, SUM(deposit_total) AS dep
    FROM invoice_items
    GROUP BY invoice_id
) t ON t.invoice_id = i.id
SET i.subtotal = t.sub,
    i.deposit_total = t.dep,
    i.total_amount = t.sub + t.dep,
    i.updated_at = i.updated_at
WHERE i.invoice_code IN (
    'INV-7F3A91C2','INV-2D84E6B0','INV-91C5D07A','INV-5B0E3F18','INV-C46A82E5','INV-08E7B4D3','INV-E19D5A66',
    'INV-34B8F0C9','INV-A7625DE1','INV-6C0B93F4','INV-D2F51A87','INV-B3E40C59','INV-4F9A17E2','INV-80D6C3BA'
);

-- ---------------------------------------------------------------------------
-- 6. Notifications (order updates, reminders and promos)
-- ---------------------------------------------------------------------------
INSERT INTO notifications (customer_id, invoice_id, type, title, body, channel, status, created_at)
SELECT s.customer_id, inv.id, s.type, s.title, s.body, s.channel, s.status, s.created_at
FROM (
    SELECT 1 AS customer_id, 'INV-B3E40C59' AS code, 'BOOKING_CONFIRMATION' AS type, 'Booking Confirmed' AS title,
           'We received your booking INV-B3E40C59 for 10 Oct - 12 Oct.' AS body, 'FCM' AS channel, 'SENT' AS status,
           CAST('2026-10-07 18:41:00' AS DATETIME) AS created_at
    UNION ALL SELECT 1, 'INV-B3E40C59', 'ORDER_STATUS', 'Order Approved',
           'Your booking INV-B3E40C59 has been approved. See you at Kandy Hub!', 'FCM', 'SENT', '2026-10-07 19:10:00'
    UNION ALL SELECT 2, 'INV-6C0B93F4', 'ORDER_STATUS', 'Order Picked Up',
           'Enjoy your gear! Order INV-6C0B93F4 was picked up at Ella Hub.', 'FCM', 'SENT', '2026-10-06 10:00:00'
    UNION ALL SELECT 4, 'INV-D2F51A87', 'ORDER_STATUS', 'Order Ready for Pickup',
           'Your gear for INV-D2F51A87 is ready at Galle Hub.', 'FCM', 'SENT', '2026-10-07 09:20:00'
    UNION ALL SELECT 4, 'INV-D2F51A87', 'RENTAL_REMINDER', 'Pickup Reminder',
           'Reminder: collect your gear at Galle Hub on 9 Oct.', 'FCM', 'QUEUED', '2026-10-08 08:00:00'
    UNION ALL SELECT 2, 'INV-C46A82E5', 'ORDER_STATUS', 'Order Cancelled',
           'Order INV-C46A82E5 was cancelled and your card payment has been refunded.', 'EMAIL', 'SENT', '2026-09-10 20:05:00'
    UNION ALL SELECT 2, 'INV-34B8F0C9', 'ORDER_STATUS', 'Order Cancelled',
           'Order INV-34B8F0C9 was cancelled.', 'EMAIL', 'FAILED', '2026-09-26 09:15:00'
    UNION ALL SELECT 4, 'INV-A7625DE1', 'ORDER_STATUS', 'Order Completed',
           'Thanks for renting with EcoTrekk! Order INV-A7625DE1 is complete.', 'FCM', 'SENT', '2026-10-04 11:45:00'
    UNION ALL SELECT 1, NULL, 'PROMO', 'Weekend Escape',
           'Rent tents and backpacks this weekend and save 10%.', 'FCM', 'SENT', '2026-10-02 09:00:00'
    UNION ALL SELECT 2, NULL, 'PROMO', 'Weekend Escape',
           'Rent tents and backpacks this weekend and save 10%.', 'FCM', 'SENT', '2026-10-02 09:00:00'
    UNION ALL SELECT 4, NULL, 'PROMO', 'Weekend Escape',
           'Rent tents and backpacks this weekend and save 10%.', 'FCM', 'SENT', '2026-10-02 09:00:00'
) s
LEFT JOIN invoices inv ON inv.invoice_code = s.code
WHERE NOT EXISTS (
    SELECT 1 FROM notifications n
    WHERE n.customer_id = s.customer_id
      AND n.title = s.title
      AND n.type = s.type
      AND (n.invoice_id <=> inv.id)
);

COMMIT;
