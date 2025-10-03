-- ===== 5 ЗАПРОСОВ НА ЧТЕНИЕ =====

-- 1. Список всех заказов за последние 7 дней с именем покупателя и описанием товара.
SELECT
    o.id AS order_id,
    o.order_date,
    CONCAT(c.first_name, ' ', c.last_name) AS customer_name,
    p.description AS product_description,
    os.status_name
FROM orders o
         JOIN customer c ON o.customer_id = c.id
         JOIN product p ON o.product_id = p.id
         JOIN order_status os ON o.status_id = os.id
WHERE o.order_date >= (CURRENT_DATE - INTERVAL '7 day')
ORDER BY o.order_date DESC;

-- 2. Топ-3 самых популярных товара (по количеству заказов).
SELECT
    p.description,
    p.category,
    COUNT(o.id) AS orders_count
FROM product p
         JOIN orders o ON p.id = o.product_id
GROUP BY p.id, p.description, p.category
ORDER BY orders_count DESC
    LIMIT 3;

-- 3. Покупатели, которые не сделали ни одного заказа.
SELECT
    c.id,
    c.first_name,
    c.last_name,
    c.email
FROM customer c
         LEFT JOIN orders o ON c.id = o.customer_id
WHERE o.id IS NULL;

-- 4. Суммарная стоимость всех выполненных заказов для каждого покупателя.
SELECT
    c.first_name,
    c.last_name,
    SUM(p.price * o.quantity) AS total_spent
FROM orders o
         JOIN customer c ON o.customer_id = c.id
         JOIN product p ON o.product_id = p.id
         JOIN order_status os ON o.status_id = os.id
WHERE os.status_name = 'Выполнен'
GROUP BY c.id, c.first_name, c.last_name
ORDER BY total_spent DESC;

-- 5. Количество товаров на складе в каждой категории.
SELECT
    category,
    SUM(quantity) AS total_quantity
FROM product
GROUP BY category
ORDER BY total_quantity DESC;


-- ===== 3 ЗАПРОСА НА ИЗМЕНЕНИЕ (UPDATE) =====

-- 1. Обновление статуса заказа (например, с "Новый" на "Подтвержден").
UPDATE orders
SET status_id = (SELECT id FROM order_status WHERE status_name = 'Подтвержден')
WHERE id = 6; -- Обновляем заказ с ID = 6

-- 2. Уменьшение количества товара на складе при создании заказа.
-- (этот запрос должен выполняться в транзакции с созданием заказа)
UPDATE product
SET quantity = quantity - 1
WHERE id = 2; -- Уменьшаем количество Смартфона Galaxy S23 на 1

-- 3. Дать 10% скидку на все товары из категории "Книги".
UPDATE product
SET price = price * 0.90
WHERE category = 'Книги';

-- Проверка скидки
-- SELECT * FROM product WHERE category = 'Книги';

-- ===== 2 ЗАПРОСА НА УДАЛЕНИЕ (DELETE) =====

-- 1. Удаление клиентов, у которых нет заказов (используя подзапрос).
DELETE FROM customer
WHERE id IN (
    SELECT c.id
    FROM customer c
             LEFT JOIN orders o ON c.id = o.customer_id
    WHERE o.id IS NULL
);

-- 2. Удаление отмененных заказов, которые были созданы более года назад.
DELETE FROM orders
WHERE status_id = (SELECT id FROM order_status WHERE status_name = 'Отменен')
  AND order_date < (CURRENT_DATE - INTERVAL '1 year');