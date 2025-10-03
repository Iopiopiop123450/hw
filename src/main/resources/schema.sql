DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS customer;
DROP TABLE IF EXISTS product;
DROP TABLE IF EXISTS order_status;


CREATE TABLE IF NOT EXISTS order_status (
                                            id SERIAL PRIMARY KEY,
                                            status_name VARCHAR(50) UNIQUE NOT NULL -- Имя статуса (e.g., 'Новый', 'В обработке', 'Завершен', 'Отменен')
    );
COMMENT ON TABLE order_status IS 'Справочник возможных статусов заказа';
COMMENT ON COLUMN order_status.status_name IS 'Наименование статуса';


CREATE TABLE IF NOT EXISTS customer (
                                        id SERIAL PRIMARY KEY,
                                        first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) UNIQUE,
    email VARCHAR(100) UNIQUE NOT NULL
    );
COMMENT ON TABLE customer IS 'Данные о покупателях';
COMMENT ON COLUMN customer.id IS 'Уникальный идентификатор покупателя';
COMMENT ON COLUMN customer.email IS 'Электронная почта (уникальна)';


CREATE TABLE IF NOT EXISTS product (
                                       id SERIAL PRIMARY KEY,
                                       description TEXT NOT NULL,
                                       price NUMERIC(10, 2) NOT NULL CHECK (price >= 0), -- Цена с проверкой на неотрицательность
    quantity INT NOT NULL CHECK (quantity >= 0), -- Количество на складе с проверкой
    category VARCHAR(100)
    );
COMMENT ON TABLE product IS 'Каталог товаров';
COMMENT ON COLUMN product.price IS 'Цена товара, не может быть отрицательной';
COMMENT ON COLUMN product.quantity IS 'Количество товара на складе, не может быть отрицательным';


CREATE TABLE IF NOT EXISTS orders (
                                      id SERIAL PRIMARY KEY,
                                      customer_id INT, -- FK на customer
                                      order_date TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP, -- Дата и время заказа
                                      status_id INT, -- FK на order_status

                                      FOREIGN KEY (customer_id) REFERENCES customer(id) ON DELETE SET NULL, -- Если покупатель удален, заказ остается
    FOREIGN KEY (status_id) REFERENCES order_status(id)
    );
COMMENT ON TABLE orders IS 'Информация о заказах';
COMMENT ON COLUMN orders.customer_id IS 'Ссылка на покупателя';
COMMENT ON COLUMN orders.status_id IS 'Ссылка на статус заказа';
COMMENT ON COLUMN orders.order_date IS 'Дата и время создания заказа';


ALTER TABLE orders ADD COLUMN product_id INT;
ALTER TABLE orders ADD FOREIGN KEY (product_id) REFERENCES product(id) ON DELETE SET NULL;
ALTER TABLE orders ADD COLUMN quantity INT NOT NULL CHECK (quantity > 0);


CREATE INDEX IF NOT EXISTS idx_orders_customer_id ON orders(customer_id);
CREATE INDEX IF NOT EXISTS idx_orders_product_id ON orders(product_id);
CREATE INDEX IF NOT EXISTS idx_orders_status_id ON orders(status_id);
CREATE INDEX IF NOT EXISTS idx_orders_order_date ON orders(order_date);


-- ====== ЗАПОЛНЕНИЕ ТЕСТОВЫМИ ДАННЫМИ ======

INSERT INTO order_status (status_name) VALUES
                                           ('Новый'), ('Подтвержден'), ('В сборке'), ('В доставке'), ('Выполнен'), ('Отменен');


INSERT INTO customer (first_name, last_name, phone, email) VALUES
                                                               ('Иван', 'Иванов', '+79991234567', 'ivan.ivanov@email.com'),
                                                               ('Петр', 'Петров', '+79992345678', 'petr.petrov@email.com'),
                                                               ('Анна', 'Сидорова', '+79993456789', 'anna.sidorova@email.com'),
                                                               ('Мария', 'Кузнецова', '+79994567890', 'maria.k@email.com'),
                                                               ('Алексей', 'Смирнов', '+79995678901', 'alex.smirnov@email.com'),
                                                               ('Елена', 'Васильева', '+79996789012', 'elena.v@email.com'),
                                                               ('Дмитрий', 'Попов', '+79997890123', 'dmitry.popov@email.com'),
                                                               ('Ольга', 'Лебедева', '+79998901234', 'olga.lebedeva@email.com'),
                                                               ('Сергей', 'Козлов', '+79999012345', 'sergey.kozlov@email.com'),
                                                               ('Наталья', 'Новикова', '+79990123456', 'natalia.n@email.com');


INSERT INTO product (description, price, quantity, category) VALUES
                                                                 ('Ноутбук Pro 15', 150000.00, 50, 'Электроника'),
                                                                 ('Смартфон Galaxy S23', 85000.00, 120, 'Электроника'),
                                                                 ('Беспроводные наушники Air Buds', 15000.00, 300, 'Аксессуары'),
                                                                 ('Книга "Java. Полное руководство"', 3500.50, 80, 'Книги'),
                                                                 ('Кресло офисное "Ergo"', 25000.00, 40, 'Мебель'),
                                                                 ('Кофемашина "Deluxe"', 45000.00, 25, 'Бытовая техника'),
                                                                 ('Чайник электрический "Aqua"', 3000.00, 200, 'Бытовая техника'),
                                                                 ('Рюкзак городской "City"', 4500.00, 150, 'Аксессуары'),
                                                                 ('Монитор 27" 4K', 38000.00, 60, 'Электроника'),
                                                                 ('Книга "Чистый код"', 2800.00, 110, 'Книги');

INSERT INTO orders (customer_id, product_id, status_id, quantity, order_date) VALUES
                                                                                  (1, 1, 5, 1, NOW() - INTERVAL '10 day'), -- Иванов, Ноутбук, Выполнен
                                                                                  (2, 2, 5, 1, NOW() - INTERVAL '8 day'),  -- Петров, Смартфон, Выполнен
                                                                                  (3, 4, 3, 2, NOW() - INTERVAL '5 day'),  -- Сидорова, Книга Java, В сборке
                                                                                  (1, 3, 4, 1, NOW() - INTERVAL '3 day'),  -- Иванов, Наушники, В доставке
                                                                                  (4, 7, 2, 1, NOW() - INTERVAL '2 day'),  -- Кузнецова, Чайник, Подтвержден
                                                                                  (5, 10, 1, 1, NOW() - INTERVAL '1 day'), -- Смирнов, Книга Чистый код, Новый
                                                                                  (6, 6, 6, 1, NOW() - INTERVAL '15 day'), -- Васильева, Кофемашина, Отменен
                                                                                  (7, 8, 5, 1, NOW() - INTERVAL '1 month'),-- Попов, Рюкзак, Выполнен
                                                                                  (8, 5, 5, 2, NOW() - INTERVAL '20 day'), -- Лебедева, Кресло, Выполнен
                                                                                  (2, 9, 1, 1, NOW());                      -- Петров, Монитор, Новый