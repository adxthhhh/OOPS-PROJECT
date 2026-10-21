package com.foodapp.order.service;

import com.foodapp.auth.Customer;
import com.foodapp.auth.CustomerDAO;
import com.foodapp.mealplan.dao.FoodItemDAO;
import com.foodapp.mealplan.model.FoodItem;
import com.foodapp.order.dao.OrderDAO;
import com.foodapp.order.dao.OrderItemDAO;
import com.foodapp.order.dao.OrderStatusHistoryDAO;
import com.foodapp.order.model.Order;
import com.foodapp.order.model.OrderItem;
import com.foodapp.order.model.OrderStatus;
import com.foodapp.order.model.OrderStatusHistory;
import com.foodapp.payment.PaymentRecord;
import com.foodapp.payment.PaymentStatus;
import com.foodapp.payment.dao.PaymentDAO;
import com.foodapp.util.DBConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

/**
 * OrderService - Coordinates the transactional checkout workflow and order queries.
 * Enforces server-authoritative calculations, snapshot immutability, and atomic commit/rollback.
 */
public class OrderService {

    private final OrderDAO orderDAO;
    private final OrderItemDAO orderItemDAO;
    private final OrderStatusHistoryDAO statusHistoryDAO;
    private final PaymentDAO paymentDAO;
    private final FoodItemDAO foodItemDAO;
    private final CustomerDAO customerDAO;

    public OrderService() {
        this.orderDAO = new OrderDAO();
        this.orderItemDAO = new OrderItemDAO();
        this.statusHistoryDAO = new OrderStatusHistoryDAO();
        this.paymentDAO = new PaymentDAO();
        this.foodItemDAO = new FoodItemDAO();
        this.customerDAO = new CustomerDAO();
    }

    /**
     * Executes the complete checkout workflow inside a single database transaction.
     *
     * @param customerId ID of the authenticated purchasing customer
     * @param itemQuantities Map of food item IDs to requested purchase quantities
     * @param deliveryAddress Destination delivery address
     * @param contactPhone Customer contact phone number
     * @param paymentMethod Method chosen (UPI, CARD, WALLET, COD)
     * @return Generated and persisted Order entity
     * @throws SQLException if a database error occurs or transaction is rolled back
     * @throws IllegalArgumentException if validation fails
     */
    public Order checkout(int customerId, Map<Integer, Integer> itemQuantities,
                         String deliveryAddress, String contactPhone, String paymentMethod)
            throws SQLException, IllegalArgumentException {

        // 1. Validate Customer
        if (customerId <= 0) {
            throw new IllegalArgumentException("Invalid customer ID.");
        }
        Customer customer = customerDAO.findById(customerId);
        if (customer == null || !customer.isActive()) {
            throw new IllegalArgumentException("Customer does not exist or account is inactive.");
        }

        // 2. Validate Items & Quantities
        if (itemQuantities == null || itemQuantities.isEmpty()) {
            throw new IllegalArgumentException("Cart cannot be empty for checkout.");
        }
        if (deliveryAddress == null || deliveryAddress.trim().isEmpty()) {
            throw new IllegalArgumentException("Delivery address is required.");
        }
        if (contactPhone == null || contactPhone.trim().isEmpty()) {
            throw new IllegalArgumentException("Contact phone number is required.");
        }

        // 3. Retrieve Authoritative Prices from Database & Compute Totals
        double subtotal = 0.0;
        List<OrderItem> orderItems = new ArrayList<>();

        for (Map.Entry<Integer, Integer> entry : itemQuantities.entrySet()) {
            int itemId = entry.getKey();
            int qty = entry.getValue();

            if (qty <= 0) {
                throw new IllegalArgumentException("Item quantity must be at least 1.");
            }

            FoodItem foodItem = foodItemDAO.getItemById(itemId);
            if (foodItem == null) {
                throw new IllegalArgumentException("Food item ID " + itemId + " not found in catalogue.");
            }
            if (!foodItem.isAvailable()) {
                throw new IllegalArgumentException("Food item '" + foodItem.getName() + "' is currently unavailable.");
            }

            double itemSubtotal = foodItem.getPrice() * qty;
            subtotal += itemSubtotal;

            // Prepare snapshot line item
            OrderItem lineItem = new OrderItem(
                    foodItem.getItemId(),
                    foodItem.getName(),
                    qty,
                    foodItem.getPrice()
            );
            orderItems.add(lineItem);
        }

        // 4. Calculate Server-Side Delivery Fee
        // Free delivery above Rs. 300, else Rs. 40 flat fee
        double deliveryFee = (subtotal >= 300.0) ? 0.00 : 40.00;
        double totalAmount = subtotal + deliveryFee;

        // 5. Begin Transaction
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                // Step A: Insert Order
                Order order = new Order(
                        customerId,
                        subtotal,
                        deliveryFee,
                        totalAmount,
                        deliveryAddress.trim(),
                        contactPhone.trim()
                );
                order.setEstimatedDeliveryTime("25-35 mins");
                order.setOrderStatus(OrderStatus.PLACED);

                boolean orderCreated = orderDAO.createOrder(order, con);
                if (!orderCreated || order.getOrderId() <= 0) {
                    throw new SQLException("Failed to insert order record.");
                }

                // Step B: Insert Order Items with snapshots
                for (OrderItem oi : orderItems) {
                    oi.setOrderId(order.getOrderId());
                }
                boolean itemsInserted = orderItemDAO.insertOrderItems(orderItems, con);
                if (!itemsInserted) {
                    throw new SQLException("Failed to insert order line items.");
                }
                order.setItems(orderItems);

                // Step C: Create Initial Payment Record
                String method = (paymentMethod != null && !paymentMethod.trim().isEmpty())
                        ? paymentMethod.trim().toUpperCase()
                        : "UPI";
                PaymentRecord payRecord = new PaymentRecord(
                        order.getOrderId(),
                        method,
                        totalAmount,
                        "COD".equalsIgnoreCase(method) ? PaymentStatus.PENDING : PaymentStatus.PENDING,
                        "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                        "Initial checkout payment record"
                );
                paymentDAO.createPaymentRecord(payRecord, con);

                // Step D: Insert Initial Order Status History
                OrderStatusHistory history = new OrderStatusHistory(
                        order.getOrderId(),
                        null,
                        OrderStatus.PLACED,
                        "Order placed by customer via checkout."
                );
                statusHistoryDAO.addHistoryEntry(history, con);

                // Step E: Commit Transaction
                con.commit();
                return order;

            } catch (Exception e) {
                con.rollback();
                throw new SQLException("Checkout transaction aborted and rolled back: " + e.getMessage(), e);
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    public Order getOrderDetails(int orderId) throws SQLException {
        if (orderId <= 0) return null;
        return orderDAO.getOrderById(orderId);
    }

    public List<Order> getCustomerOrders(int customerId) throws SQLException {
        if (customerId <= 0) return Collections.emptyList();
        return orderDAO.getOrdersByCustomer(customerId);
    }
}
