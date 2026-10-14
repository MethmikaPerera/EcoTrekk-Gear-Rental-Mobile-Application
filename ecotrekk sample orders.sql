-- ============================================================================
-- EcoTrekk MORE sample orders (development / demo only)  -  generated 2026-10-14
--
-- Adds 38 orders (invoices + invoice_items + notifications) spread from mid-June to
-- 14 Oct 2026, so the admin dashboard, reports and the app's order history have plenty
-- of data: mostly COMPLETED, some CANCELLED, plus active orders this week
-- (PENDING, APPROVED, READY, PICKED_UP).
--
-- Only INSERTS into invoices, invoice_items and notifications. It does NOT touch
-- customers, admin_users, gears, gear_categories, gear_images or inventory.
-- Active orders are stored with inventory_allocated = 0 so completing/cancelling them
-- later changes no stock (the backend only restores stock for allocated orders).
-- Safe to run more than once (INSERT IGNORE on invoice_code, NOT EXISTS on items).
-- Requires the earlier "ecotrekk sample data.sql" (it uses the five hubs created there).
-- Customers used by id: 1 Methmika, 2 Thisath, 3 Sasandara (inactive), 4 Waidurya.
-- ============================================================================

USE `ecotrekk`;
SET NAMES utf8mb4 COLLATE utf8mb4_unicode_ci;

START TRANSACTION;

INSERT IGNORE INTO invoices
    (invoice_code, customer_id, collection_hub_id, status, payment_status, payment_method,
     subtotal, deposit_total, total_amount, inventory_allocated, created_at, updated_at)
SELECT s.code, s.customer_id, h.id, s.status, s.payment_status, s.payment_method,
       0, 0, 0, b'0', s.created_at, s.updated_at
FROM (
    SELECT 'INV-0F585D4E' AS code, 1 AS customer_id, 'Colombo Hub' AS hub_name, 'COMPLETED' AS status, 'PAID' AS payment_status, 'CARD' AS payment_method, CAST('2026-06-15 18:34:00' AS DATETIME) AS created_at, CAST('2026-06-19 17:15:00' AS DATETIME) AS updated_at
    UNION ALL SELECT 'INV-19057A38', 2, 'Nuwara Eliya Hub', 'COMPLETED', 'PAID', 'CARD', '2026-06-20 18:53:00', '2026-06-24 15:24:00'
    UNION ALL SELECT 'INV-F2A3955A', 3, 'Colombo Hub', 'COMPLETED', 'PAID', 'PAY_AT_HUB', '2026-06-26 07:54:00', '2026-06-28 16:00:00'
    UNION ALL SELECT 'INV-C2C53E82', 3, 'Nuwara Eliya Hub', 'COMPLETED', 'PAID', 'CARD', '2026-07-04 11:33:00', '2026-07-08 15:07:00'
    UNION ALL SELECT 'INV-9612D37C', 3, 'Colombo Hub', 'COMPLETED', 'PAID', 'PAY_AT_HUB', '2026-07-12 16:30:00', '2026-07-15 20:37:00'
    UNION ALL SELECT 'INV-9CB99A89', 1, 'Ella Hub', 'COMPLETED', 'PAID', 'CARD', '2026-07-18 10:15:00', '2026-07-19 18:30:00'
    UNION ALL SELECT 'INV-7E5E6394', 4, 'Ella Hub', 'COMPLETED', 'PAID', 'PAY_AT_HUB', '2026-07-26 14:42:00', '2026-07-29 18:26:00'
    UNION ALL SELECT 'INV-0CBB8BFE', 2, 'Kandy Hub', 'COMPLETED', 'PAID', 'CARD', '2026-08-02 11:31:00', '2026-08-08 09:24:00'
    UNION ALL SELECT 'INV-DD5ED57B', 2, 'Colombo Hub', 'COMPLETED', 'PAID', 'CARD', '2026-08-13 09:54:00', '2026-08-15 10:57:00'
    UNION ALL SELECT 'INV-86F319A2', 1, 'Ella Hub', 'COMPLETED', 'PAID', 'PAY_AT_HUB', '2026-08-17 11:46:00', '2026-08-18 17:07:00'
    UNION ALL SELECT 'INV-A17202A7', 4, 'Colombo Hub', 'COMPLETED', 'PAID', 'CARD', '2026-08-21 16:50:00', '2026-08-25 17:10:00'
    UNION ALL SELECT 'INV-A2A20468', 4, 'Colombo Hub', 'COMPLETED', 'PAID', 'CARD', '2026-08-26 13:12:00', '2026-08-28 10:48:00'
    UNION ALL SELECT 'INV-1A7FFACC', 1, 'Kandy Hub', 'CANCELLED', 'REFUNDED', 'CARD', '2026-08-30 17:11:00', '2026-08-31 03:18:00'
    UNION ALL SELECT 'INV-6E01C0C2', 1, 'Kandy Hub', 'COMPLETED', 'PAID', 'CARD', '2026-09-03 08:37:00', '2026-09-08 10:42:00'
    UNION ALL SELECT 'INV-33E6D259', 4, 'Colombo Hub', 'COMPLETED', 'PAID', 'CARD', '2026-09-08 17:26:00', '2026-09-12 17:10:00'
    UNION ALL SELECT 'INV-2FDB9F6E', 4, 'Nuwara Eliya Hub', 'COMPLETED', 'PAID', 'CARD', '2026-09-13 09:43:00', '2026-09-14 18:47:00'
    UNION ALL SELECT 'INV-7D5E14F2', 2, 'Colombo Hub', 'COMPLETED', 'PAID', 'PAY_AT_HUB', '2026-09-15 18:47:00', '2026-09-20 16:36:00'
    UNION ALL SELECT 'INV-47F05293', 1, 'Kandy Hub', 'COMPLETED', 'PAID', 'CARD', '2026-09-17 14:22:00', '2026-09-19 09:20:00'
    UNION ALL SELECT 'INV-E439B0EA', 1, 'Galle Hub', 'CANCELLED', 'REFUNDED', 'CARD', '2026-09-20 19:05:00', '2026-09-21 08:23:00'
    UNION ALL SELECT 'INV-5AF02C51', 2, 'Ella Hub', 'COMPLETED', 'PAID', 'PAY_AT_HUB', '2026-09-23 12:27:00', '2026-09-29 11:54:00'
    UNION ALL SELECT 'INV-01671BB3', 1, 'Colombo Hub', 'COMPLETED', 'PAID', 'CARD', '2026-09-26 18:14:00', '2026-09-29 17:56:00'
    UNION ALL SELECT 'INV-64C017E2', 2, 'Colombo Hub', 'COMPLETED', 'PAID', 'CARD', '2026-09-29 07:44:00', '2026-10-02 19:06:00'
    UNION ALL SELECT 'INV-B355460D', 1, 'Kandy Hub', 'CANCELLED', 'REFUNDED', 'CARD', '2026-10-02 13:07:00', '2026-10-03 03:45:00'
    UNION ALL SELECT 'INV-ECAEBE7E', 4, 'Colombo Hub', 'COMPLETED', 'PAID', 'PAY_AT_HUB', '2026-10-03 15:54:00', '2026-10-06 15:29:00'
    UNION ALL SELECT 'INV-54020B16', 4, 'Kandy Hub', 'COMPLETED', 'PAID', 'PAY_AT_HUB', '2026-10-05 13:06:00', '2026-10-10 20:49:00'
    UNION ALL SELECT 'INV-E5780C52', 2, 'Galle Hub', 'CANCELLED', 'REFUNDED', 'CARD', '2026-10-06 13:34:00', '2026-10-06 18:52:00'
    UNION ALL SELECT 'INV-2C9615F6', 2, 'Galle Hub', 'COMPLETED', 'PAID', 'CARD', '2026-10-08 20:57:00', '2026-10-10 18:41:00'
    UNION ALL SELECT 'INV-1C2BD223', 4, 'Nuwara Eliya Hub', 'COMPLETED', 'PAID', 'CARD', '2026-10-09 10:29:00', '2026-10-11 15:31:00'
    UNION ALL SELECT 'INV-2FB12F6A', 2, 'Galle Hub', 'CANCELLED', 'REFUNDED', 'CARD', '2026-10-09 14:36:00', '2026-10-09 19:22:00'
    UNION ALL SELECT 'INV-F51FE6DB', 1, 'Kandy Hub', 'COMPLETED', 'PAID', 'CARD', '2026-10-10 08:50:00', '2026-10-11 18:04:00'
    UNION ALL SELECT 'INV-0BE4BA15', 2, 'Colombo Hub', 'PICKED_UP', 'PAID', 'PAY_AT_HUB', '2026-10-11 12:28:00', '2026-10-13 09:37:00'
    UNION ALL SELECT 'INV-A7A064CE', 1, 'Galle Hub', 'APPROVED', 'PAID', 'CARD', '2026-10-12 11:06:00', '2026-10-12 16:06:00'
    UNION ALL SELECT 'INV-F1981168', 4, 'Ella Hub', 'READY', 'UNPAID', 'PAY_AT_HUB', '2026-10-12 18:09:00', '2026-10-13 00:09:00'
    UNION ALL SELECT 'INV-004C5309', 1, 'Galle Hub', 'APPROVED', 'PAID', 'CARD', '2026-10-13 11:28:00', '2026-10-13 15:28:00'
    UNION ALL SELECT 'INV-ECED61BC', 1, 'Colombo Hub', 'PICKED_UP', 'PAID', 'CARD', '2026-10-13 13:40:00', '2026-10-14 08:17:00'
    UNION ALL SELECT 'INV-4685DB73', 4, 'Kandy Hub', 'PENDING', 'PAID', 'CARD', '2026-10-13 17:40:00', '2026-10-13 17:40:00'
    UNION ALL SELECT 'INV-322B6441', 4, 'Ella Hub', 'PENDING', 'PAID', 'CARD', '2026-10-14 07:32:00', '2026-10-14 07:32:00'
    UNION ALL SELECT 'INV-ED821B8A', 2, 'Nuwara Eliya Hub', 'PENDING', 'UNPAID', 'PAY_AT_HUB', '2026-10-14 08:18:00', '2026-10-14 08:18:00'
) s
JOIN hubs h ON h.name = s.hub_name;

-- Items: price and deposit are copied from the gear; line_total = qty * daily price * rental days (inclusive)
INSERT INTO invoice_items
    (invoice_id, gear_id, qty, start_date, end_date, daily_rent_price, deposit_amount, line_total, deposit_total)
SELECT inv.id, g.id, s.qty, s.start_date, s.end_date,
       g.daily_rent_price, g.deposit_amount,
       g.daily_rent_price * s.qty * (DATEDIFF(s.end_date, s.start_date) + 1),
       g.deposit_amount * s.qty
FROM (
    SELECT 'INV-0F585D4E' AS code, 'Mountain Bike' AS gear_name, 2 AS qty, CAST('2026-06-18' AS DATE) AS start_date, CAST('2026-06-19' AS DATE) AS end_date
    UNION ALL SELECT 'INV-0F585D4E', 'Trekking Backpack 50L', 1, '2026-06-18', '2026-06-19'
    UNION ALL SELECT 'INV-19057A38', 'Mountain Bike', 2, '2026-06-23', '2026-06-24'
    UNION ALL SELECT 'INV-19057A38', 'Portable Camping Stove', 1, '2026-06-23', '2026-06-24'
    UNION ALL SELECT 'INV-F2A3955A', 'Action Camera Tripod', 1, '2026-06-27', '2026-06-28'
    UNION ALL SELECT 'INV-F2A3955A', '2 Person Camping Tent', 2, '2026-06-27', '2026-06-28'
    UNION ALL SELECT 'INV-F2A3955A', 'Adventure Drone with 4K Camera', 1, '2026-06-27', '2026-06-28'
    UNION ALL SELECT 'INV-C2C53E82', 'Head lamp', 3, '2026-07-06', '2026-07-08'
    UNION ALL SELECT 'INV-9612D37C', 'Trekking Backpack 50L', 1, '2026-07-14', '2026-07-15'
    UNION ALL SELECT 'INV-9CB99A89', '6 Person Camping Tent', 1, '2026-07-19', '2026-07-19'
    UNION ALL SELECT 'INV-7E5E6394', 'Portable Camping Stove', 1, '2026-07-28', '2026-07-29'
    UNION ALL SELECT 'INV-7E5E6394', 'Sleeping Bag', 1, '2026-07-28', '2026-07-29'
    UNION ALL SELECT 'INV-0CBB8BFE', 'Trekking Backpack 50L', 1, '2026-08-05', '2026-08-07'
    UNION ALL SELECT 'INV-0CBB8BFE', 'Snorkeling Set', 2, '2026-08-05', '2026-08-07'
    UNION ALL SELECT 'INV-DD5ED57B', '2 Person Camping Tent', 2, '2026-08-14', '2026-08-14'
    UNION ALL SELECT 'INV-DD5ED57B', 'Head lamp', 3, '2026-08-14', '2026-08-14'
    UNION ALL SELECT 'INV-86F319A2', 'Adventure Drone with 4K Camera', 1, '2026-08-18', '2026-08-18'
    UNION ALL SELECT 'INV-A17202A7', 'Mountain Bike', 1, '2026-08-24', '2026-08-25'
    UNION ALL SELECT 'INV-A2A20468', 'Cycling Gloves', 1, '2026-08-27', '2026-08-27'
    UNION ALL SELECT 'INV-A2A20468', 'Trekking Backpack 75L', 1, '2026-08-27', '2026-08-27'
    UNION ALL SELECT 'INV-A2A20468', '2 Person Camping Tent', 2, '2026-08-27', '2026-08-27'
    UNION ALL SELECT 'INV-1A7FFACC', 'Hiking Poles', 2, '2026-08-31', '2026-09-01'
    UNION ALL SELECT 'INV-1A7FFACC', 'Portable Camping Stove', 1, '2026-08-31', '2026-09-01'
    UNION ALL SELECT 'INV-6E01C0C2', 'Trekking Backpack 75L', 1, '2026-09-06', '2026-09-07'
    UNION ALL SELECT 'INV-6E01C0C2', 'Trekking Backpack 50L', 1, '2026-09-06', '2026-09-07'
    UNION ALL SELECT 'INV-6E01C0C2', '360° Action Camera', 1, '2026-09-06', '2026-09-07'
    UNION ALL SELECT 'INV-33E6D259', '2 Person Camping Tent', 2, '2026-09-11', '2026-09-12'
    UNION ALL SELECT 'INV-2FDB9F6E', 'Sleeping Bag', 1, '2026-09-14', '2026-09-14'
    UNION ALL SELECT 'INV-2FDB9F6E', 'Life Jacket', 1, '2026-09-14', '2026-09-14'
    UNION ALL SELECT 'INV-7D5E14F2', 'GoPro Action Camera', 1, '2026-09-18', '2026-09-20'
    UNION ALL SELECT 'INV-7D5E14F2', 'Head lamp', 3, '2026-09-18', '2026-09-20'
    UNION ALL SELECT 'INV-47F05293', 'Trekking Backpack 50L', 1, '2026-09-18', '2026-09-18'
    UNION ALL SELECT 'INV-47F05293', '2 Person Camping Tent', 2, '2026-09-18', '2026-09-18'
    UNION ALL SELECT 'INV-E439B0EA', 'Portable Camping Stove', 1, '2026-09-21', '2026-09-21'
    UNION ALL SELECT 'INV-E439B0EA', 'Sleeping Bag', 2, '2026-09-21', '2026-09-21'
    UNION ALL SELECT 'INV-5AF02C51', 'Trekking Backpack 75L', 1, '2026-09-25', '2026-09-28'
    UNION ALL SELECT 'INV-5AF02C51', 'Trekking Backpack 50L', 1, '2026-09-25', '2026-09-28'
    UNION ALL SELECT 'INV-5AF02C51', 'Hiking Poles', 1, '2026-09-25', '2026-09-28'
    UNION ALL SELECT 'INV-01671BB3', 'Action Camera Tripod', 1, '2026-09-28', '2026-09-29'
    UNION ALL SELECT 'INV-64C017E2', 'Trekking Backpack 50L', 1, '2026-10-02', '2026-10-02'
    UNION ALL SELECT 'INV-B355460D', 'Sleeping Bag', 2, '2026-10-05', '2026-10-06'
    UNION ALL SELECT 'INV-B355460D', 'Hiking Poles', 1, '2026-10-05', '2026-10-06'
    UNION ALL SELECT 'INV-ECAEBE7E', '2 Person Camping Tent', 2, '2026-10-05', '2026-10-06'
    UNION ALL SELECT 'INV-ECAEBE7E', 'Inflatable Kayak', 1, '2026-10-05', '2026-10-06'
    UNION ALL SELECT 'INV-ECAEBE7E', 'Mountain Bike', 2, '2026-10-05', '2026-10-06'
    UNION ALL SELECT 'INV-54020B16', 'Hydration Pack', 1, '2026-10-08', '2026-10-10'
    UNION ALL SELECT 'INV-54020B16', '2 Person Camping Tent', 2, '2026-10-08', '2026-10-10'
    UNION ALL SELECT 'INV-54020B16', 'Portable Camping Stove', 1, '2026-10-08', '2026-10-10'
    UNION ALL SELECT 'INV-E5780C52', 'Head lamp', 2, '2026-10-07', '2026-10-08'
    UNION ALL SELECT 'INV-E5780C52', 'Mountain Bike', 2, '2026-10-07', '2026-10-08'
    UNION ALL SELECT 'INV-2C9615F6', 'Head lamp', 1, '2026-10-09', '2026-10-10'
    UNION ALL SELECT 'INV-1C2BD223', 'Portable Camping Stove', 1, '2026-10-10', '2026-10-11'
    UNION ALL SELECT 'INV-1C2BD223', '6 Person Camping Tent', 1, '2026-10-10', '2026-10-11'
    UNION ALL SELECT 'INV-2FB12F6A', 'GoPro Action Camera', 1, '2026-10-11', '2026-10-12'
    UNION ALL SELECT 'INV-2FB12F6A', 'Adventure Drone with 4K Camera', 1, '2026-10-11', '2026-10-12'
    UNION ALL SELECT 'INV-2FB12F6A', '360° Action Camera', 1, '2026-10-11', '2026-10-12'
    UNION ALL SELECT 'INV-F51FE6DB', 'Trekking Backpack 50L', 1, '2026-10-11', '2026-10-11'
    UNION ALL SELECT 'INV-0BE4BA15', 'Life Jacket', 3, '2026-10-13', '2026-10-16'
    UNION ALL SELECT 'INV-0BE4BA15', '2 Person Camping Tent', 2, '2026-10-13', '2026-10-16'
    UNION ALL SELECT 'INV-A7A064CE', 'Head lamp', 3, '2026-10-16', '2026-10-17'
    UNION ALL SELECT 'INV-A7A064CE', 'Snorkeling Set', 1, '2026-10-16', '2026-10-17'
    UNION ALL SELECT 'INV-F1981168', 'Cycling Helmet', 1, '2026-10-15', '2026-10-17'
    UNION ALL SELECT 'INV-F1981168', 'Hiking Poles', 1, '2026-10-15', '2026-10-17'
    UNION ALL SELECT 'INV-004C5309', 'Cycling Gloves', 1, '2026-10-17', '2026-10-19'
    UNION ALL SELECT 'INV-004C5309', 'Sleeping Bag', 3, '2026-10-17', '2026-10-19'
    UNION ALL SELECT 'INV-ECED61BC', 'Life Jacket', 3, '2026-10-14', '2026-10-16'
    UNION ALL SELECT 'INV-ECED61BC', 'Trekking Backpack 75L', 1, '2026-10-14', '2026-10-16'
    UNION ALL SELECT 'INV-4685DB73', 'GoPro Action Camera', 1, '2026-10-16', '2026-10-17'
    UNION ALL SELECT 'INV-4685DB73', 'Head lamp', 1, '2026-10-16', '2026-10-17'
    UNION ALL SELECT 'INV-322B6441', '2 Person Camping Tent', 1, '2026-10-18', '2026-10-20'
    UNION ALL SELECT 'INV-ED821B8A', 'Head lamp', 3, '2026-10-19', '2026-10-20'
) s
JOIN invoices inv ON inv.invoice_code = s.code
JOIN gears g ON g.name = s.gear_name
WHERE NOT EXISTS (SELECT 1 FROM invoice_items x WHERE x.invoice_id = inv.id AND x.gear_id = g.id);

-- Fill invoice totals from the items (these sample invoices only; updated_at is kept as set above)
UPDATE invoices i
JOIN (SELECT invoice_id, SUM(line_total) AS sub, SUM(deposit_total) AS dep FROM invoice_items GROUP BY invoice_id) t
  ON t.invoice_id = i.id
SET i.subtotal = t.sub, i.deposit_total = t.dep, i.total_amount = t.sub + t.dep, i.updated_at = i.updated_at
WHERE i.invoice_code IN (
'INV-0F585D4E', 'INV-19057A38', 'INV-F2A3955A', 'INV-C2C53E82', 'INV-9612D37C', 'INV-9CB99A89',
    'INV-7E5E6394', 'INV-0CBB8BFE', 'INV-DD5ED57B', 'INV-86F319A2', 'INV-A17202A7', 'INV-A2A20468',
    'INV-1A7FFACC', 'INV-6E01C0C2', 'INV-33E6D259', 'INV-2FDB9F6E', 'INV-7D5E14F2', 'INV-47F05293',
    'INV-E439B0EA', 'INV-5AF02C51', 'INV-01671BB3', 'INV-64C017E2', 'INV-B355460D', 'INV-ECAEBE7E',
    'INV-54020B16', 'INV-E5780C52', 'INV-2C9615F6', 'INV-1C2BD223', 'INV-2FB12F6A', 'INV-F51FE6DB',
    'INV-0BE4BA15', 'INV-A7A064CE', 'INV-F1981168', 'INV-004C5309', 'INV-ECED61BC', 'INV-4685DB73',
    'INV-322B6441', 'INV-ED821B8A'
);

-- Notifications for this week's orders
INSERT INTO notifications (customer_id, invoice_id, type, title, body, channel, status, created_at)
SELECT s.customer_id, inv.id, s.type, s.title, s.body, s.channel, s.status, s.created_at
FROM (
    SELECT 2 AS customer_id, 'INV-2C9615F6' AS code, 'ORDER_STATUS' AS type, 'Order Completed' AS title, 'Thanks for renting with EcoTrekk! Order INV-2C9615F6 is complete.' AS body, 'FCM' AS channel, 'SENT' AS status, CAST('2026-10-10 18:41:00' AS DATETIME) AS created_at
    UNION ALL SELECT 4, 'INV-1C2BD223', 'ORDER_STATUS', 'Order Completed', 'Thanks for renting with EcoTrekk! Order INV-1C2BD223 is complete.', 'FCM', 'SENT', '2026-10-11 15:31:00'
    UNION ALL SELECT 2, 'INV-2FB12F6A', 'ORDER_STATUS', 'Order Cancelled', 'Order INV-2FB12F6A was cancelled.', 'EMAIL', 'SENT', '2026-10-09 19:22:00'
    UNION ALL SELECT 1, 'INV-F51FE6DB', 'ORDER_STATUS', 'Order Completed', 'Thanks for renting with EcoTrekk! Order INV-F51FE6DB is complete.', 'FCM', 'SENT', '2026-10-11 18:04:00'
    UNION ALL SELECT 2, 'INV-0BE4BA15', 'ORDER_STATUS', 'Order Picked Up', 'Enjoy your gear! Order INV-0BE4BA15 was picked up at Colombo Hub.', 'FCM', 'SENT', '2026-10-13 09:37:00'
    UNION ALL SELECT 1, 'INV-A7A064CE', 'ORDER_STATUS', 'Order Approved', 'Your booking INV-A7A064CE has been approved.', 'FCM', 'SENT', '2026-10-12 16:06:00'
    UNION ALL SELECT 4, 'INV-F1981168', 'ORDER_STATUS', 'Order Ready for Pickup', 'Your gear for INV-F1981168 is ready at Ella Hub.', 'FCM', 'SENT', '2026-10-13 00:09:00'
    UNION ALL SELECT 1, 'INV-004C5309', 'ORDER_STATUS', 'Order Approved', 'Your booking INV-004C5309 has been approved.', 'FCM', 'SENT', '2026-10-13 15:28:00'
    UNION ALL SELECT 1, 'INV-ECED61BC', 'ORDER_STATUS', 'Order Picked Up', 'Enjoy your gear! Order INV-ECED61BC was picked up at Colombo Hub.', 'FCM', 'SENT', '2026-10-14 08:17:00'
    UNION ALL SELECT 4, 'INV-4685DB73', 'BOOKING_CONFIRMATION', 'Booking Received', 'We received your booking INV-4685DB73. We will confirm it shortly.', 'FCM', 'QUEUED', '2026-10-13 17:40:00'
    UNION ALL SELECT 4, 'INV-322B6441', 'BOOKING_CONFIRMATION', 'Booking Received', 'We received your booking INV-322B6441. We will confirm it shortly.', 'FCM', 'QUEUED', '2026-10-14 07:32:00'
    UNION ALL SELECT 2, 'INV-ED821B8A', 'BOOKING_CONFIRMATION', 'Booking Received', 'We received your booking INV-ED821B8A. We will confirm it shortly.', 'FCM', 'QUEUED', '2026-10-14 08:18:00'
) s
JOIN invoices inv ON inv.invoice_code = s.code
WHERE NOT EXISTS (
    SELECT 1 FROM notifications n
    WHERE n.customer_id = s.customer_id AND n.invoice_id = inv.id AND n.title = s.title AND n.type = s.type
);

COMMIT;
