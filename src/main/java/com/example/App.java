
package com.example;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.*;

public class App {

    public static void main(String[] args) {

        try (Connection conn = DatabaseManager.getConnection()) {
            System.out.println("Подключение к базе данных установлено.");


            conn.setAutoCommit(false);

            try {

                runManualMigration(conn);


                performCrudOperations(conn);


                conn.commit();
                System.out.println("\nТранзакция успешно зафиксирована (commit).");

            } catch (SQLException e) {

                System.err.println("Произошла ошибка SQL. Откат транзакции...");
                e.printStackTrace();
                conn.rollback();
                System.err.println("Транзакция отменена (rollback).");
            } catch (IOException e) {
                System.err.println("Ошибка чтения файла миграции.");
                e.printStackTrace();
                conn.rollback();
            }

        } catch (SQLException e) {
            System.err.println("Не удалось подключиться к базе данных.");
            e.printStackTrace();
        }
    }

    private static void runManualMigration(Connection conn) throws IOException, SQLException {
        System.out.println("\n--- Запуск ручной миграции (schema.sql) ---");
        String sqlScript;

        try (InputStream in = App.class.getClassLoader().getResourceAsStream("schema.sql")) {
            if (in == null) {
                throw new IOException("Файл schema.sql не найден в ресурсах!");
            }
            sqlScript = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        }


        String[] statements = sqlScript.split(";");

        try (Statement stmt = conn.createStatement()) {
            for (String statement : statements) {
                if (!statement.trim().isEmpty()) {
                    stmt.execute(statement);
                }
            }
        }
        System.out.println("Миграция успешно завершена.");
    }

    //Демонстрация CRUD-операций
    private static void performCrudOperations(Connection conn) throws SQLException {
        System.out.println("\n--- Демонстрация CRUD операций ---");

        // ---- CREATE ----
        System.out.println("\n1. Создание нового товара и покупателя:");
        long newProductId = createProduct(conn, "Игровой джойстик", 5500.00, 75, "Аксессуары");
        long newCustomerId = createCustomer(conn, "Семён", "Горбунков", "+79261112233", "gorbunkov@email.com");

        System.out.println("\n2. Создание нового заказа:");
        createOrder(conn, newCustomerId, newProductId, 2);

        // ---- READ ----
        System.out.println("\n3. Чтение и вывод последних 5 заказов:");
        readLastFiveOrders(conn);

        // ---- UPDATE ----
        System.out.println("\n4. Обновление цены и количества товара:");
        updateProduct(conn, newProductId, 5250.99, 73); // цена - 5500, было 75, купили 2, стало 73

        // ---- DELETE ----
        System.out.println("\n5. Удаление тестовых записей:");
        deleteTestData(conn, newCustomerId, newProductId);
    }

    private static long createProduct(Connection conn, String description, double price, int quantity, String category) throws SQLException {
        String sql = "INSERT INTO product (description, price, quantity, category) VALUES (?, ?, ?, ?) RETURNING id;";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, description);
            pstmt.setDouble(2, price);
            pstmt.setInt(3, quantity);
            pstmt.setString(4, category);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                long id = rs.getLong(1);
                System.out.printf("  - Создан товар '%s' с ID = %d%n", description, id);
                return id;
            }
        }
        throw new SQLException("Не удалось создать товар.");
    }

    private static long createCustomer(Connection conn, String firstName, String lastName, String phone, String email) throws SQLException {
        String sql = "INSERT INTO customer (first_name, last_name, phone, email) VALUES (?, ?, ?, ?) RETURNING id;";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, firstName);
            pstmt.setString(2, lastName);
            pstmt.setString(3, phone);
            pstmt.setString(4, email);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                long id = rs.getLong(1);
                System.out.printf("  - Создан покупатель '%s %s' с ID = %d%n", firstName, lastName, id);
                return id;
            }
        }
        throw new SQLException("Не удалось создать покупателя.");
    }

    private static void createOrder(Connection conn, long customerId, long productId, int quantity) throws SQLException {

        String updateProductSql = "UPDATE product SET quantity = quantity - ? WHERE id = ? AND quantity >= ?;";
        try (PreparedStatement pstmt = conn.prepareStatement(updateProductSql)) {
            pstmt.setInt(1, quantity);
            pstmt.setLong(2, productId);
            pstmt.setInt(3, quantity);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("Недостаточно товара на складе для создания заказа.");
            }
        }


        String createOrderSql = "INSERT INTO orders (customer_id, product_id, status_id, quantity) VALUES (?, ?, (SELECT id FROM order_status WHERE status_name = 'Новый'), ?);";
        try (PreparedStatement pstmt = conn.prepareStatement(createOrderSql)) {
            pstmt.setLong(1, customerId);
            pstmt.setLong(2, productId);
            pstmt.setInt(3, quantity);
            pstmt.executeUpdate();
            System.out.printf("  - Создан заказ для покупателя ID=%d на товар ID=%d в количестве %d шт.%n", customerId, productId, quantity);
        }
    }

    private static void readLastFiveOrders(Connection conn) throws SQLException {
        String sql = """
            SELECT 
                o.id AS order_id, 
                o.order_date,
                CONCAT(c.first_name, ' ', c.last_name) AS customer_name,
                p.description AS product,
                o.quantity,
                os.status_name
            FROM orders o
            JOIN customer c ON o.customer_id = c.id
            JOIN product p ON o.product_id = p.id
            JOIN order_status os ON o.status_id = os.id
            ORDER BY o.order_date DESC
            LIMIT 5;
            """;
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            System.out.println("  ID | Дата заказа             | Покупатель       | Товар                      | Кол-во | Статус");
            System.out.println("  ---|-------------------------|------------------|----------------------------|--------|---------");
            while (rs.next()) {
                System.out.printf(
                        "  %-2d | %-23s | %-16s | %-26s | %-6d | %s%n",
                        rs.getInt("order_id"),
                        rs.getTimestamp("order_date").toLocalDateTime().toString().replace("T", " "),
                        rs.getString("customer_name"),
                        rs.getString("product"),
                        rs.getInt("quantity"),
                        rs.getString("status_name")
                );
            }
        }
    }

    private static void updateProduct(Connection conn, long productId, double newPrice, int newQuantity) throws SQLException {
        String sql = "UPDATE product SET price = ?, quantity = ? WHERE id = ?;";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, newPrice);
            pstmt.setInt(2, newQuantity);
            pstmt.setLong(3, productId);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.printf("  - Данные товара с ID=%d обновлены. Новая цена: %.2f, новое количество: %d%n", productId, newPrice, newQuantity);
            }
        }
    }

    private static void deleteTestData(Connection conn, long customerId, long productId) throws SQLException {

        String deleteOrdersSql = "DELETE FROM orders WHERE customer_id = ? OR product_id = ?;";
        try (PreparedStatement pstmt = conn.prepareStatement(deleteOrdersSql)) {
            pstmt.setLong(1, customerId);
            pstmt.setLong(2, productId);
            int rows = pstmt.executeUpdate();
            System.out.printf("  - Удалено %d связанных заказов.%n", rows);
        }


        String deleteCustomerSql = "DELETE FROM customer WHERE id = ?;";
        try (PreparedStatement pstmt = conn.prepareStatement(deleteCustomerSql)) {
            pstmt.setLong(1, customerId);
            pstmt.executeUpdate();
            System.out.printf("  - Удален тестовый покупатель с ID=%d.%n", customerId);
        }

        String deleteProductSql = "DELETE FROM product WHERE id = ?;";
        try (PreparedStatement pstmt = conn.prepareStatement(deleteProductSql)) {
            pstmt.setLong(1, productId);
            pstmt.executeUpdate();
            System.out.printf("  - Удален тестовый товар с ID=%d.%n", productId);
        }
    }
}