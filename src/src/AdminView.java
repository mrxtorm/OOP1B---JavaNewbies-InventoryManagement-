package src;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.CategoryChartBuilder;
import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.style.Styler;

public final class AdminView extends javax.swing.JFrame {
    
    
    public AdminView() {
        initComponents();
        
        setLocationRelativeTo(null);
        
        
        loadData();
        
        refreshCategoryComboBoxes();
        
        // Style the sales period toggle buttons
        styleSalesToggleButton(btnYesterday);
        styleSalesToggleButton(btnToday);
        styleSalesToggleButton(btnThisWeek);
        styleSalesToggleButton(btnThisMonth);
        styleSalesToggleButton(btnThisYear);

        // Add action listeners to time period buttons
        btnYesterday.addActionListener(e -> updateSalesDisplay("Yesterday"));
        btnToday.addActionListener(e -> updateSalesDisplay("Today"));
        btnThisWeek.addActionListener(e -> updateSalesDisplay("This Week"));
        btnThisMonth.addActionListener(e -> updateSalesDisplay("This Month"));
        btnThisYear.addActionListener(e -> updateSalesDisplay("This Year"));

        // Set default to Today
        updateSalesDisplay("Today");
        
        styleToggleButton(btnHome);
        styleToggleButton(btnInventory);
        styleToggleButton(btnNotifications);
        styleToggleButton(btnPurchase);
        styleToggleButton(btnSettings);
        
        btnHome.setSelected(true);
        
        buttonGroup();
        
        loadNotifications();
        
        JButton btnMarkAllRead = new JButton("✓ Mark All as Read");
        btnMarkAllRead.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnMarkAllRead.setBackground(Color.WHITE);
        btnMarkAllRead.setFocusable(false);

        btnMarkAllRead.addActionListener(e -> markAllNotificationsAsRead());
        
        // Add button to the notification toolbar area (MidPanel)
        MidPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 10));
        MidPanel.add(btnMarkAllRead);

        JScrollPane notificationScroll = new JScrollPane(jPanel1);
        notificationScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        notificationScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        notificationScroll.getVerticalScrollBar().setUnitIncrement(16);

        REDUNDANCY3.setLayout(new BorderLayout());
        REDUNDANCY3.add(notificationScroll, BorderLayout.CENTER);
        
        initPurchasePage();
        
        initSettingsPage();
    }

//LEFT PANEL COMPONENTS/METHODS
    private void styleToggleButton(JToggleButton button) {
        button.setOpaque(true);
        button.setContentAreaFilled(true);

        button.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                if (button.isSelected()) {
                    button.setBackground(new Color(0, 0, 51));         // Dark Blue (Selected)
                    button.setForeground(new Color(255, 255, 255));         
                } else {
                    button.setBackground(new Color(255, 255, 255));    // White (Unselected)
                    button.setForeground(new Color(0, 0, 0));
                }
            }
        });
    }
    
    private void buttonGroup() {
        ButtonGroup toggleGroup = new ButtonGroup();
    
        toggleGroup.add(btnHome);
        toggleGroup.add(btnInventory);
        toggleGroup.add(btnNotifications);
        toggleGroup.add(btnPurchase);
        toggleGroup.add(btnSettings);
        toggleGroup.add(btnLogout);
    }
//END LEFT PANEL COMPONENTS/METHODS    

//HOMEPAGE COMPONENTS/METHODS
    // Helper method to get sales summary data
    private Map<String, Double> getSalesSummary(String timePeriod) {
        Map<String, Double> summary = new HashMap<>();
        summary.put("totalSales", 0.0);
        summary.put("itemsSold", 0.0);

        String query = "SELECT SUM(quantity_sold) as items_sold, SUM(sale_amount) as total_sales " +
                       "FROM salerecords WHERE ";

        switch (timePeriod) {
            case "Yesterday":
                query += "DATE(sale_datetime) = DATE_SUB(CURDATE(), INTERVAL 1 DAY)";
                break;
            case "Today":
                query += "DATE(sale_datetime) = CURDATE()";
                break;
            case "This Week":
                query += "YEARWEEK(sale_datetime, 1) = YEARWEEK(CURDATE(), 1)";
                break;
            case "This Month":
                query += "YEAR(sale_datetime) = YEAR(CURDATE()) AND MONTH(sale_datetime) = MONTH(CURDATE())";
                break;
            case "This Year":
                query += "YEAR(sale_datetime) = YEAR(CURDATE())";
                break;
            default:
                query += "1=1"; // All time if no period matches
        }

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/storvendb", "root", "");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            if (rs.next()) {
                summary.put("itemsSold", rs.getDouble("items_sold"));
                summary.put("totalSales", rs.getDouble("total_sales"));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error loading sales data: " + e.getMessage(), 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
        }

        return summary;
    }

    // Method to update both sales display and chart
    private void updateSalesDisplay(String period) {
        // Update sales summary
        Map<String, Double> salesData = getSalesSummary(period);

        // Update the labels
        labelTotalSold.setText(String.valueOf((int)Math.round(salesData.get("itemsSold"))));
        labelTotalAmount.setText("₱" + String.format("%.2f", salesData.get("totalSales")));

        // Highlight the selected button
        btnYesterday.setSelected("Yesterday".equals(period));
        btnToday.setSelected("Today".equals(period));
        btnThisWeek.setSelected("This Week".equals(period));
        btnThisMonth.setSelected("This Month".equals(period));
        btnThisYear.setSelected("This Year".equals(period));

        // Update the chart
        initChart(period);
    }

    // Style the time period toggle buttons
    private void styleSalesToggleButton(JToggleButton button) {
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setFocusPainted(false);

        button.addChangeListener(e -> {
            if (button.isSelected()) {
                button.setBackground(new Color(0, 51, 102)); // Dark blue when selected
                button.setForeground(Color.WHITE);
            } else {
                button.setBackground(new Color(220, 220, 220)); // Light gray when not selected
                button.setForeground(Color.BLACK);
            }
        });
    }
    
    private String shortenName(String name, int maxLength) {
        if (name == null) return "";
        if (name.length() <= maxLength) return name;
        return name.substring(0, maxLength - 3) + "...";
    }
    
    private void initChart(String timePeriod) {
        // Get sales data from database for the specified time period
        Map<String, Integer> categorySales = getCategorySales(timePeriod);

        // Check if we have data to display
        if (categorySales.isEmpty()) {
            GraphPanel.removeAll();

            JLabel noDataLabel = new JLabel("No sales data available for " + timePeriod, SwingConstants.CENTER);
            noDataLabel.setFont(new Font("Arial", Font.BOLD, 16));
            noDataLabel.setForeground(Color.GRAY);
            noDataLabel.setHorizontalTextPosition(SwingConstants.CENTER);
            noDataLabel.setVerticalTextPosition(SwingConstants.BOTTOM);

            GraphPanel.setLayout(new BorderLayout());
            GraphPanel.add(noDataLabel, BorderLayout.CENTER);

            GraphPanel.revalidate();
            GraphPanel.repaint();
            return;
        }

        // Create Chart
        CategoryChart chart = new CategoryChartBuilder()
                .width(800)
                .height(400)
                .title("Sales by Category - " + timePeriod)
                .xAxisTitle("Category")
                .yAxisTitle("Total Items Sold")
                .build();

        // Customize Chart
        chart.getStyler().setLegendPosition(Styler.LegendPosition.InsideNW);
        chart.getStyler().setLegendVisible(false);
        chart.getStyler().setAvailableSpaceFill(0.8);
        chart.getStyler().setXAxisLabelRotation(45);
        chart.getStyler().setAxisTickLabelsFont(new Font("Arial", Font.BOLD, 10));
        chart.getStyler().setPlotMargin(10);
        chart.getStyler().setPlotContentSize(0.98);

        // Sort categories by sales quantity (descending)
        List<Map.Entry<String, Integer>> sortedEntries = new ArrayList<>(categorySales.entrySet());
        sortedEntries.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));

        // Extract sorted category names and quantities
        List<String> categoryNames = new ArrayList<>();
        List<Integer> quantities = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : sortedEntries) {
            categoryNames.add(shortenName(entry.getKey(), 15));
            quantities.add(entry.getValue());
        }

        // Add Series
        chart.addSeries("Items Sold", categoryNames, quantities);

        // Convert to Swing component
        JPanel chartPanel = new XChartPanel<>(chart);
        GraphPanel.removeAll();
        GraphPanel.add(chartPanel, BorderLayout.CENTER);
        GraphPanel.revalidate();
    }

    private Map<String, Integer> getCategorySales(String timePeriod) {
        Map<String, Integer> sales = new HashMap<>();

        String query = "SELECT i.category, SUM(s.quantity_sold) as total " +
                     "FROM salerecords s " +
                     "JOIN inventory i ON s.product_name = i.product_name " +
                     "WHERE ";

        switch (timePeriod) {
            case "Yesterday":
                query += "DATE(s.sale_datetime) = DATE_SUB(CURDATE(), INTERVAL 1 DAY)";
                break;
            case "Today":
                query += "DATE(s.sale_datetime) = CURDATE()";
                break;
            case "This Week":
                query += "YEARWEEK(s.sale_datetime, 1) = YEARWEEK(CURDATE(), 1)";
                break;
            case "This Month":
                query += "YEAR(s.sale_datetime) = YEAR(CURDATE()) AND MONTH(s.sale_datetime) = MONTH(CURDATE())";
                break;
            case "This Year":
                query += "YEAR(s.sale_datetime) = YEAR(CURDATE())";
                break;
            default:
                query += "1=1"; // All time if no period matches
        }

        query += " GROUP BY i.category";

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/storvendb", "root", "");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                sales.put(rs.getString("category"), rs.getInt("total"));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error loading sales data: " + e.getMessage(), 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
        }

        return sales.isEmpty() ? Collections.emptyMap() : sales;
    }
//END OF HOMEPAGE COMPONENTS
    
//INVENTORY PAGE COMPONENTS/METHODS  
    public void loadData() {
        // Load data into tableAllProducts
        DefaultTableModel model1 = (DefaultTableModel) tableAllProducts.getModel();
        model1.setRowCount(0);
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/storvendb", "root", "");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM inventory")) {
            while (rs.next()) {
                model1.addRow(new Object[]{
                    rs.getInt("product_id"),
                    rs.getString("product_name"),
                    rs.getString("description"),
                    rs.getString("category"),
                    rs.getInt("stock_level"),
                    rs.getInt("reorder_level"),
                    "₱" + rs.getString("price")
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading inventory for All Products: " + e.getMessage());
        }

        // Load data into tableProductExpiration
        DefaultTableModel model2 = (DefaultTableModel) tableProductExpiration.getModel();
        model2.setRowCount(0);
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/storvendb", "root", "");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM inventory")) {
            while (rs.next()) {
                model2.addRow(new Object[]{
                    rs.getString("product_name"),
                    rs.getString("description"),
                    rs.getString("category"),
                    rs.getString("expiration_date")
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading inventory for Product Expiration: " + e.getMessage());
        }
    }
        
    public void refreshProductTable() {
        DefaultTableModel model = (DefaultTableModel) tableAllProducts.getModel();
        model.setRowCount(0); // Clear existing data

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/storvendb", "root", "");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT product_id, product_name, category, stock_level, reorder_level, price FROM inventory")) {

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("product_id"),
                    rs.getString("product_name"),
                    rs.getString("category"),
                    rs.getInt("stock_level"),
                    rs.getInt("reorder_level"),
                    "₱ " + rs.getString("price")
                    // Note: Not including expiration_date in the table display
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error refreshing product table: " + e.getMessage(), 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private String getExpirationDateFromDB(int productId) {
        String expirationDate = "";
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/storvendb", "root", "");
            PreparedStatement stmt = conn.prepareStatement("SELECT expiration_date FROM inventory WHERE product_id = ?")) {
            stmt.setInt(1, productId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                expirationDate = rs.getString("expiration_date");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error loading expiration date: " + e.getMessage(), 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
        }
        return expirationDate;
    }
    
    private String getPriceFromDB(int productId) {
        String expirationDate = "";
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/storvendb", "root", "");
            PreparedStatement stmt = conn.prepareStatement("SELECT price FROM inventory WHERE product_id = ?")) {
            stmt.setInt(1, productId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                expirationDate = rs.getString("price");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error loading expiration date: " + e.getMessage(), 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
        }
        return expirationDate;
    }
    
    private void searchInventoryProducts(String keyword){
        DefaultTableModel model1 = (DefaultTableModel) tableAllProducts.getModel();
        model1.setRowCount(0);
        DefaultTableModel model2 = (DefaultTableModel) tableProductExpiration.getModel();
        model2.setRowCount(0);
        
        String query = "SELECT * FROM inventory";

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/storvendb", "root", "")) {
            PreparedStatement pstmt;

            if (keyword.isEmpty()) {
                pstmt = conn.prepareStatement(query);
            } else {
                query += " WHERE product_name LIKE ?";
                pstmt = conn.prepareStatement(query);
                pstmt.setString(1, "%" + keyword + "%");
            }

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                model1.addRow(new Object[]{
                    rs.getInt("product_id"),
                    rs.getString("product_name"),
                    rs.getString("description"),
                    rs.getString("category"),
                    rs.getInt("stock_level"),
                    rs.getInt("reorder_level"),
                    "₱" + rs.getString("price")
                });
                 model2.addRow(new Object[]{
                    rs.getString("product_name"),
                    rs.getString("description"),
                    rs.getString("category"),
                    rs.getString("expiration_date")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error searching products: " + e.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }
//END OF INVENTORY PAGE COMPONENTS/METHODS
    
//NOTIICATION PAGE COMPONENTS/METHODS    
    private void saveNotificationToDB(String message, String type, String productName) {
        // First check if a similar notification already exists
        String checkQuery = "SELECT COUNT(*) FROM notifications WHERE type = ? AND product_name = ? AND is_read = 0";
        String insertQuery = "INSERT INTO notifications (message, type, product_name, is_read) VALUES (?, ?, ?, 0)";

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/storvendb", "root", "")) {
            // Check for existing notification
            PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
            checkStmt.setString(1, type);
            checkStmt.setString(2, productName);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next() && rs.getInt(1) == 0) {
                // Only insert if no similar unread notification exists
                PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
                insertStmt.setString(1, message);
                insertStmt.setString(2, type);
                insertStmt.setString(3, productName);
                insertStmt.executeUpdate();
            }
        } catch (Exception e) {
            System.err.println("Error saving notification: " + e.getMessage());
        }
    }
    
    private void markNotificationAsRead(int notificationId) {
        String query = "UPDATE notifications SET is_read = 1 WHERE notification_id = ?";

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/storvendb", "root", "");
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, notificationId);
            stmt.executeUpdate();

        } catch (Exception e) {
            System.err.println("Error marking notification as read: " + e.getMessage());
        }
    }
    
    private void deleteNotificationFromDB(int notificationId) {
        String query = "DELETE FROM notifications WHERE notification_id = ?";

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/storvendb", "root", "");
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, notificationId);
            stmt.executeUpdate();

        } catch (Exception e) {
            System.err.println("Error deleting notification: " + e.getMessage());
        }
    }
    
    private void checkForNewNotifications() {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/storvendb", "root", "")) {
            // First, clear resolved notifications (where the condition no longer exists)
            clearResolvedNotifications(conn);

            // Then check for new notifications
            checkExpiringProducts(conn);
            checkOutOfStockProducts(conn);
            checkLowStockProducts(conn);
            checkSlowMovingProducts(conn);
            checkBestSellingProduct(conn);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error checking for new notifications: " + e.getMessage(), 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearResolvedNotifications(Connection conn) throws SQLException {
        // Expired products that are no longer expiring soon
        String expiringQuery = "DELETE n FROM notifications n " +
                             "LEFT JOIN inventory i ON n.product_name = i.product_name " +
                             "WHERE n.type = 'EXPIRING' AND " +
                             "(i.expiration_date IS NULL OR i.expiration_date > DATE_ADD(CURDATE(), INTERVAL 10 DAY))";

        // Out of stock products that now have stock
        String outOfStockQuery = "DELETE n FROM notifications n " +
                               "LEFT JOIN inventory i ON n.product_name = i.product_name " +
                               "WHERE n.type = 'OUT_OF_STOCK' AND " +
                               "i.stock_level > 0";

        // Low stock products that now have sufficient stock
        String lowStockQuery = "DELETE n FROM notifications n " +
                             "LEFT JOIN inventory i ON n.product_name = i.product_name " +
                             "WHERE n.type = 'LOW_STOCK' AND " +
                             "i.stock_level > i.reorder_level";

        // Slow moving products that have new sales
        String slowMovingQuery = "DELETE n FROM notifications n " +
                               "WHERE n.type = 'SLOW_MOVING' AND " +
                               "EXISTS (SELECT 1 FROM salerecords s " +
                               "WHERE s.product_name = n.product_name " +
                               "AND s.sale_datetime >= DATE_SUB(CURDATE(), INTERVAL 14 DAY))";

        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(expiringQuery);
            stmt.executeUpdate(outOfStockQuery);
            stmt.executeUpdate(lowStockQuery);
            stmt.executeUpdate(slowMovingQuery);
        }
    }
    
    private void checkExpiringProducts(Connection conn) throws SQLException {
        String query = "SELECT i.product_name, i.expiration_date " +
                     "FROM inventory i " +
                     "WHERE i.expiration_date IS NOT NULL " +
                     "AND i.expiration_date <= DATE_ADD(CURDATE(), INTERVAL 10 DAY)";

        try (PreparedStatement stmt = conn.prepareStatement(query); 
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String name = rs.getString("product_name");
                String expDate = rs.getString("expiration_date");
                saveNotificationToDB(
                    name + " is about to expire on " + expDate,
                    "EXPIRING",
                    name
                );
            }
        }
    }

    private void checkOutOfStockProducts(Connection conn) throws SQLException {
        String query = """
            SELECT i.product_name 
            FROM inventory i
            WHERE i.stock_level <= 0
            """;

        try (PreparedStatement stmt = conn.prepareStatement(query); 
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String name = rs.getString("product_name");
                saveNotificationToDB(
                    name + " is out of stock!",
                    "OUT_OF_STOCK",
                    name
                );
            }
        }
    }

    private void checkLowStockProducts(Connection conn) throws SQLException {
        String query = """
            SELECT i.product_name, i.stock_level 
            FROM inventory i
            WHERE i.stock_level > 0 
            AND i.stock_level <= i.reorder_level
            """;

        try (PreparedStatement stmt = conn.prepareStatement(query); 
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String name = rs.getString("product_name");
                int stock = rs.getInt("stock_level");
                saveNotificationToDB(
                    name + " has only " + stock + " left in stock.",
                    "LOW_STOCK",
                    name
                );
            }
        }
    }

    private void checkSlowMovingProducts(Connection conn) throws SQLException {
        String query = """
            SELECT i.product_name 
            FROM inventory i
            LEFT JOIN (
                SELECT product_name FROM salerecords
                WHERE sale_datetime >= DATE_SUB(CURDATE(), INTERVAL 14 DAY)
            ) recent_sales ON i.product_name = recent_sales.product_name
            WHERE recent_sales.product_name IS NULL
            """;

        try (PreparedStatement stmt = conn.prepareStatement(query); 
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String name = rs.getString("product_name");
                saveNotificationToDB(
                    name + " had no sales in the past 14 days.",
                    "SLOW_MOVING",
                    name
                );
            }
        }
    }

    private void checkBestSellingProduct(Connection conn) throws SQLException {
        // Always delete previous best seller before adding new one
        String deleteQuery = "DELETE FROM notifications WHERE type = 'BEST_SELLER'";
        try (PreparedStatement stmt = conn.prepareStatement(deleteQuery)) {
            stmt.executeUpdate();
        }

        String query = "SELECT product_name, SUM(quantity_sold) AS total " +
                     "FROM salerecords " +
                     "WHERE sale_datetime >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) " +
                     "GROUP BY product_name " +
                     "ORDER BY total DESC " +
                     "LIMIT 1";

        try (PreparedStatement stmt = conn.prepareStatement(query); 
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                String name = rs.getString("product_name");
                int total = rs.getInt("total");
                saveNotificationToDB(
                    name + " sold " + total + " units in the last 7 days.",
                    "BEST_SELLER",
                    name
                );
            }
        }
    }
    
    private void loadNotificationsFromDB() {
        jPanel1.removeAll();
        jPanel1.setLayout(new BoxLayout(jPanel1, BoxLayout.Y_AXIS));

        String query = "SELECT * FROM notifications ORDER BY created_at DESC";

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/storvendb", "root", "");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm a");

            while (rs.next()) {
                int notificationId = rs.getInt("notification_id");
                String message = rs.getString("message");
                boolean isRead = rs.getBoolean("is_read");

                // Format timestamp directly here
                java.sql.Timestamp timestamp = rs.getTimestamp("created_at");
                String createdAt = (timestamp != null) ? 
                    dateFormat.format(timestamp) : "N/A";

                
                    Color bgColor = isRead ? new Color(240,240,240) : getColorForType(rs.getString("type"));
                

                JPanel card = createNotificationCard(notificationId, message, createdAt, bgColor, isRead);
                jPanel1.add(card);
                jPanel1.add(Box.createVerticalStrut(8));
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error loading notifications: " + e.getMessage(), 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
        }

        jPanel1.revalidate();
        jPanel1.repaint();
    }
    
    private Color getColorForType(String type) {
        switch (type) {
            case "EXPIRING":
                return new Color(255, 99, 71); // Tomato red
            case "OUT_OF_STOCK":
                return new Color(220, 20, 60); // Crimson red
            case "LOW_STOCK":
                return new Color(50, 205, 50); // Lime green
            case "SLOW_MOVING":
                return new Color(255, 69, 0); // Red-orange
            case "BEST_SELLER":
                return new Color(147, 112, 219); // Medium purple
            default:
                return Color.WHITE;
        }
    }
    
    private void loadNotifications() {
        jPanel1.removeAll();
        jPanel1.setLayout(new BoxLayout(jPanel1, BoxLayout.Y_AXIS));

        // Do NOT call checkForNewNotifications() here!
        // This keeps notifications persistent and prevents recreation on every open

        // Step 2: Load from DB and display
        loadNotificationsFromDB();

        jPanel1.revalidate();
        jPanel1.repaint();
    }
    
    private void markAllNotificationsAsRead() {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/storvendb", "root", "");
             Statement stmt = conn.createStatement()) {

            int updated = stmt.executeUpdate("UPDATE notifications SET is_read = 1 WHERE is_read = 0");

            if (updated > 0) {
                // Update the UI without reloading
                Component[] components = jPanel1.getComponents();
                for (Component component : components) {
                    if (component instanceof JPanel) {
                        JPanel card = (JPanel) component;
                        card.setBackground(new Color(220, 220, 220)); // Light gray for read notifications

                        // Also disable the "Mark as Read" button if it exists
                        Component[] cardComponents = card.getComponents();
                        for (Component cardComponent : cardComponents) {
                            if (cardComponent instanceof JPanel) {
                                JPanel bottomPanel = (JPanel) cardComponent;
                                Component[] bottomComponents = bottomPanel.getComponents();
                                for (Component bottomComponent : bottomComponents) {
                                    if (bottomComponent instanceof JPanel) {
                                        JPanel buttonPanel = (JPanel) bottomComponent;
                                        Component[] buttons = buttonPanel.getComponents();
                                        for (Component button : buttons) {
                                            if (button instanceof JButton && ((JButton) button).getText().equals("✓ Read")) {
                                                ((JButton) button).setEnabled(false);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                JOptionPane.showMessageDialog(this, "All notifications marked as read.");
            } else {
                JOptionPane.showMessageDialog(this, "No unread notifications.");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Error marking all as read: " + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private JPanel createNotificationCard(int notificationId, String message, String dateTime, Color bgColor, boolean isRead) {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout());
        card.setBackground(bgColor);
        card.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10),
            javax.swing.BorderFactory.createLineBorder(Color.GRAY)));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Main message
        JLabel lblMessage = new JLabel(message);
        lblMessage.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblMessage.setForeground(Color.BLACK);

        // Bottom right: time
        JLabel lblTime = new JLabel(dateTime);
        lblTime.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTime.setForeground(Color.DARK_GRAY);

        // Action buttons
        JButton btnMarkAsRead = new JButton("✓ Read");
        JButton btnDelete = new JButton("🗑 Delete");
        btnMarkAsRead.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnDelete.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnMarkAsRead.setFocusable(false);
        btnDelete.setFocusable(false);

        if (isRead) {
            btnMarkAsRead.setEnabled(false);
        }

        // Action listeners
        btnMarkAsRead.addActionListener(e -> {
            markNotificationAsRead(notificationId);
            card.setBackground(new Color(220, 220, 220));
            btnMarkAsRead.setEnabled(false);
        });

        btnDelete.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(AdminView.this,
                "Are you sure you want to delete this notification?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);
                
            if (confirm == JOptionPane.YES_OPTION) {
            deleteNotificationFromDB(notificationId);
            jPanel1.remove(card);
            jPanel1.revalidate();
            jPanel1.repaint();
            }
        });
        
        btnMarkAsRead.addActionListener(e -> {
            markNotificationAsRead(notificationId);
            card.setBackground(new Color(220, 220, 220));
            btnMarkAsRead.setEnabled(false);
        });

        // Button panel (left)
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        leftPanel.setOpaque(false);
        leftPanel.add(btnMarkAsRead);
        leftPanel.add(btnDelete);

        // Time label (right)
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        rightPanel.setOpaque(false);
        rightPanel.add(lblTime);

        // Wrap buttons and time in bottom panel
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.add(leftPanel, BorderLayout.WEST);
        bottomPanel.add(rightPanel, BorderLayout.EAST);

        // Final layout
        card.add(lblMessage, BorderLayout.CENTER);
        card.add(bottomPanel, BorderLayout.SOUTH);

        return card;
    }
//END NOTIICATION PAGE COMPONENTS/METHODS    

//PURCHASE PAGE COMPONENTS/METHODS  
    private void initPurchasePage() {
        PurchaseParent.setLayout(new BorderLayout());
        
        initializeCategoryComboBoxes();

        // --- Form Panel with Border ---
        JPanel purchaseFormPanel = new JPanel(new GridBagLayout());
        purchaseFormPanel.setBackground(Color.WHITE);
        purchaseFormPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(0, 51, 102)), 
            "Purchase Details", 
            TitledBorder.LEFT, TitledBorder.TOP, 
            new Font("Segoe UI", Font.BOLD, 14), 
            new Color(0, 51, 102)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.anchor = GridBagConstraints.WEST;

        // Product Name
        gbc.gridx = 0; gbc.gridy = 0;
        purchaseFormPanel.add(new JLabel("Product Name:"), gbc);

        gbc.gridx = 1;
        txtPurchaseProductName = new JTextField(20);
        purchaseFormPanel.add(txtPurchaseProductName, gbc);

        // Description (moved up)
        gbc.gridx = 0; gbc.gridy = 1;
        purchaseFormPanel.add(new JLabel("Description:"), gbc);

        gbc.gridx = 1;
        txtPurchaseDescription = new JTextField(20);
        purchaseFormPanel.add(txtPurchaseDescription, gbc);

        // Category
        gbc.gridx = 0; gbc.gridy = 2;
        purchaseFormPanel.add(new JLabel("Category:"), gbc);

        gbc.gridx = 1;
        cmbPurchaseCategory = new JComboBox<>(new String[] {});
        cmbPurchaseCategory.setPreferredSize(new Dimension(231, 30));
        purchaseFormPanel.add(cmbPurchaseCategory, gbc);

        // Quantity
        gbc.gridx = 0; gbc.gridy = 3;
        purchaseFormPanel.add(new JLabel("Quantity:"), gbc);

        gbc.gridx = 1;
        txtPurchaseQuantity = new JTextField(20);
        purchaseFormPanel.add(txtPurchaseQuantity, gbc);

        // Price
        gbc.gridx = 0; gbc.gridy = 4;
        purchaseFormPanel.add(new JLabel("Price per Unit:"), gbc);

        gbc.gridx = 1;
        txtPurchasePrice = new JTextField(20);
        purchaseFormPanel.add(txtPurchasePrice, gbc);

        // Buttons
        gbc.gridx = 0; gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(Color.WHITE);

        btnAddPurchase = new JButton("➕ Add Purchase");
        btnAddPurchase.setBackground(new Color(0, 51, 102));
        btnAddPurchase.setForeground(Color.WHITE);
        btnAddPurchase.addActionListener(this::btnAddPurchaseActionPerformed);

        btnRefreshPurchases = new JButton("🔄 Refresh");
        btnRefreshPurchases.setBackground(new Color(0, 51, 102));
        btnRefreshPurchases.setForeground(Color.WHITE);
        btnRefreshPurchases.addActionListener(this::btnRefreshPurchaseHistoryActionPerformed);

        buttonPanel.add(btnAddPurchase);
        buttonPanel.add(btnRefreshPurchases);
        purchaseFormPanel.add(buttonPanel, gbc);

        // --- Purchase History Table ---
        tablePurchasePageHistory = new JTable();
        scrollpPurchaseHistory = new JScrollPane(tablePurchasePageHistory);
        scrollpPurchaseHistory.setPreferredSize(new Dimension(650, 250));

        // --- Parent Layout ---
        JPanel wrapper = new JPanel(new BorderLayout(10, 10));
        wrapper.setBorder(new EmptyBorder(15, 15, 15, 15));
        wrapper.setBackground(Color.WHITE);
        wrapper.add(purchaseFormPanel, BorderLayout.NORTH);
        wrapper.add(scrollpPurchaseHistory, BorderLayout.CENTER);

        PurchaseParent.add(wrapper, BorderLayout.CENTER);

        // Initial Load
        loadPurchaseHistory();
    }

    private void loadPurchaseHistory() {
        String[] columnNames = {"Purchase ID", "Product Name", "Description", "Category", "Quantity", "Unit Price", "Total", "Date"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/storvendb", "root", "");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM purchases ORDER BY purchase_date DESC")) {

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("purchase_id"),
                    rs.getString("product_name"),
                    rs.getString("description"),
                    rs.getString("category"),
                    rs.getInt("quantity"),
                    "₱" + String.format("%.2f", rs.getDouble("unit_price")),
                    "₱" + String.format("%.2f", rs.getDouble("quantity") * rs.getDouble("unit_price")),
                    rs.getTimestamp("purchase_date")
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error loading purchase history: " + e.getMessage(), 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
        }

        tablePurchasePageHistory.setModel(model);
    }

    private void btnAddPurchaseActionPerformed(java.awt.event.ActionEvent evt) {
        // Validate inputs
        String productName = txtPurchaseProductName.getText().trim();
        String category = (String) cmbPurchaseCategory.getSelectedItem();
        String quantityStr = txtPurchaseQuantity.getText().trim();
        String priceStr = txtPurchasePrice.getText().trim();

        if (productName.isEmpty() || quantityStr.isEmpty() || priceStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "Please fill in all fields", 
                "Validation Error", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int quantity = Integer.parseInt(quantityStr);
            double price = Double.parseDouble(priceStr);

            if (quantity <= 0 || price <= 0) {
                throw new NumberFormatException();
            }

            // Insert into database
            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/storvendb", "root", "");
                 PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO purchases (product_name, category, quantity, unit_price, description) VALUES (?, ?, ?, ?, ?)")) {

                stmt.setString(1, productName);
                stmt.setString(2, category);
                stmt.setInt(3, quantity);
                stmt.setDouble(4, price);
                stmt.setString(5, txtPurchaseDescription.getText());

                int rowsAffected = stmt.executeUpdate();

                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Purchase recorded successfully!");

                    // Update inventory
                    updateInventory(productName, quantity);

                    // Clear form
                    txtPurchaseProductName.setText("");
                    txtPurchaseQuantity.setText("");
                    txtPurchasePrice.setText("");
                    txtPurchaseDescription.setText("");

                    // Refresh table
                    loadPurchaseHistory();
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, 
                    "Error saving purchase: " + e.getMessage(), 
                    "Database Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, 
                "Please enter valid positive numbers for quantity and price", 
                "Validation Error", 
                JOptionPane.WARNING_MESSAGE);
        }
    }

    private void updateInventory(String productName, int quantity) {
        
        
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/storvendb", "root", "");
             PreparedStatement stmt = conn.prepareStatement(
                 "UPDATE inventory SET stock_level = stock_level + ? WHERE product_name = ?")) {

            stmt.setInt(1, quantity);
            stmt.setString(2, productName);
            stmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error updating inventory: " + e.getMessage());
        }
    }   
    
    private void btnRefreshPurchaseHistoryActionPerformed(java.awt.event.ActionEvent evt) {
    loadPurchaseHistory();
    JOptionPane.showMessageDialog(this, 
        "Purchase history refreshed successfully!", 
        "Refresh", 
        JOptionPane.INFORMATION_MESSAGE);
}
    
//END PURCHASE PAGE COMPONENTS/METHODS
    
//SETTINGS PAGE COMPONENTS/METHODS
    private void initSettingsPage() {
        SettingsParent.setLayout(new BorderLayout());

        // Create sidebar for navigation
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(new Color(240, 240, 240));
        sidebar.setPreferredSize(new Dimension(180, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));

        JButton btnUsers = new JButton("👤 Manage Users");
        JButton btnCategories = new JButton("📂 Item Categories");
        JButton btnBackup = new JButton("💾 Backup Database");

        // Style buttons
        for (JButton btn : new JButton[]{btnUsers, btnCategories, btnBackup}) {
            btn.setFocusPainted(false);
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        }

        sidebar.add(btnUsers);
        sidebar.add(Box.createVerticalStrut(15));
        sidebar.add(btnCategories);
        sidebar.add(Box.createVerticalStrut(15));
        sidebar.add(btnBackup);

        // Create the card panel
        JPanel cardPanel = new JPanel(new CardLayout());

        // Create each settings page
        JPanel panelUsers = createUserManagementPanel();
        JPanel panelCategories = createCategorySetupPanel();
        JPanel panelBackup = createBackupPanel();

        cardPanel.add(panelUsers, "Users");
        cardPanel.add(panelCategories, "Categories");
        cardPanel.add(panelBackup, "Backup");

        // Button actions
        btnUsers.addActionListener(e -> showSettingsCard(cardPanel, "Users"));
        btnCategories.addActionListener(e -> showSettingsCard(cardPanel, "Categories"));
        btnBackup.addActionListener(e -> showSettingsCard(cardPanel, "Backup"));

        // Add components to parent
        SettingsParent.removeAll();
        SettingsParent.add(sidebar, BorderLayout.WEST);
        SettingsParent.add(cardPanel, BorderLayout.CENTER);
        SettingsParent.revalidate();
        SettingsParent.repaint();

        // Show default panel
        showSettingsCard(cardPanel, "Users");
    }

    private void showSettingsCard(JPanel cardPanel, String cardName) {
        CardLayout cl = (CardLayout) cardPanel.getLayout();
        cl.show(cardPanel, cardName);
    }

    private JPanel createUserManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel title = new JLabel("Manage Users");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        panel.add(title, BorderLayout.NORTH);

        JTable userTable = new JTable();
        JScrollPane scrollPane = new JScrollPane(userTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnAdd = new JButton("Add User");
        JButton btnUpdate = new JButton("Update User");
        JButton btnDelete = new JButton("Delete Selected User");
        btnPanel.add(btnAdd);
        btnPanel.add(btnUpdate);
        btnPanel.add(btnDelete);
        panel.add(btnPanel, BorderLayout.SOUTH);

        // Load users
        loadUsersIntoTable(userTable);

        btnAdd.addActionListener(e -> addUserDialog(userTable));
        btnUpdate.addActionListener(e -> updateUserDialog(userTable));
        btnDelete.addActionListener(e -> deleteSelectedUser(userTable));

        return panel;
    }

    private void loadUsersIntoTable(JTable table) {
        DefaultTableModel model = new DefaultTableModel(new String[]{"ID", "Firstname", "Lastname", "Username", "Password", "Role"}, 0);
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/storvendb", "root", "");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT account_id, fname, lname, username, password, role FROM accounts")) {
            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("account_id"),
                    rs.getString("fname"),
                    rs.getString("lname"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("role")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading users: " + e.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
        }
        table.setModel(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }

    private void addUserDialog(JTable table) {
        JTextField firstnameField = new JTextField();
        JTextField lastnameField = new JTextField();
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JComboBox<String> roleBox = new JComboBox<>(new String[]{"Admin", "User"});

        JPanel panel = new JPanel(new GridLayout(0, 1));
        
        panel.add(new JLabel("Firstname:"));
        panel.add(firstnameField);
        panel.add(new JLabel("Lastname:"));
        panel.add(lastnameField);
        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(new JLabel("Password:"));
        panel.add(passwordField);
        panel.add(new JLabel("Role:"));
        panel.add(roleBox);

        int result = JOptionPane.showConfirmDialog(this, panel, "Add New User",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();
            String role = (String) roleBox.getSelectedItem();

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields are required!", "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try (Connection conn = DriverManager.getConnection
                ("jdbc:mysql://localhost:3306/storvendb", "root", "");
                PreparedStatement stmt = conn.prepareStatement
                ("INSERT INTO accounts(fname, lname, username, password, role) VALUES (?, ?, ?, ?, ?)")) {

                stmt.setString(1, firstnameField.getText().trim());
                stmt.setString(2, lastnameField.getText().trim());
                stmt.setString(3, usernameField.getText().trim());
                stmt.setString(4, new String(passwordField.getPassword()));
                stmt.setString(5, role);  // assuming 'role' is already declared elsewhere

                stmt.executeUpdate();

                JOptionPane.showMessageDialog(this, "User added successfully!");
                loadUsersIntoTable(table);  // refresh the JTable

            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error adding user: " + e.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
            }

        }
    }
    
    private void updateUserDialog(JTable table) {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, 
                "Please select a user to update.", 
                "No Selection", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        int userId = (int) table.getValueAt(row, 0);

        // Create input fields
        JTextField firstnameField = new JTextField();
        JTextField lastnameField = new JTextField();
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JComboBox<String> roleBox = new JComboBox<>(new String[]{"Admin", "User"});

        // Pre-fill with current user data
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/storvendb", "root", "");
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT fname, lname, username, role FROM accounts WHERE account_id = ?")) {

            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                firstnameField.setText(rs.getString("fname"));
                lastnameField.setText(rs.getString("lname"));
                usernameField.setText(rs.getString("username"));
                roleBox.setSelectedItem(rs.getString("role"));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error loading user data: " + e.getMessage(), 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Create the input panel
        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("First Name:"));
        panel.add(firstnameField);
        panel.add(new JLabel("Last Name:"));
        panel.add(lastnameField);
        panel.add(new JLabel("Username:"));
        panel.add(usernameField);
        panel.add(new JLabel("Password (leave blank to keep current):"));
        panel.add(passwordField);
        panel.add(new JLabel("Role:"));
        panel.add(roleBox);

        int result = JOptionPane.showConfirmDialog(
            this, 
            panel, 
            "Update User", 
            JOptionPane.OK_CANCEL_OPTION, 
            JOptionPane.PLAIN_MESSAGE
        );

        if (result == JOptionPane.OK_OPTION) {
            // Validate inputs
            if (firstnameField.getText().trim().isEmpty() || 
                lastnameField.getText().trim().isEmpty() || 
                usernameField.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "First name, last name and username are required!", 
                    "Validation Error", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }

            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/storvendb", "root", "")) {
                String sql;
                PreparedStatement stmt;

                // Check if password was changed (not empty)
                if (passwordField.getPassword().length > 0) {
                    sql = "UPDATE accounts SET fname=?, lname=?, username=?, password=?, role=? WHERE account_id=?";
                    stmt = conn.prepareStatement(sql);
                    stmt.setString(1, firstnameField.getText().trim());
                    stmt.setString(2, lastnameField.getText().trim());
                    stmt.setString(3, usernameField.getText().trim());
                    stmt.setString(4, new String(passwordField.getPassword())); // Store plain text (not recommended)
                    stmt.setString(5, roleBox.getSelectedItem().toString());
                    stmt.setInt(6, userId);
                } else {
                    // Don't update password if field was left blank
                    sql = "UPDATE accounts SET fname=?, lname=?, username=?, role=? WHERE account_id=?";
                    stmt = conn.prepareStatement(sql);
                    stmt.setString(1, firstnameField.getText().trim());
                    stmt.setString(2, lastnameField.getText().trim());
                    stmt.setString(3, usernameField.getText().trim());
                    stmt.setString(4, roleBox.getSelectedItem().toString());
                    stmt.setInt(5, userId);
                }

                int rowsAffected = stmt.executeUpdate();

                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "User updated successfully!");
                    loadUsersIntoTable(table);
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "No user was updated.", 
                        "Update Failed", 
                        JOptionPane.WARNING_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                    "Update error: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteSelectedUser(JTable table) {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int userId = (int) table.getValueAt(row, 0);
        String username = table.getValueAt(row, 1).toString();

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete user '" + username + "'?",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/storvendb", "root", "");
                 PreparedStatement stmt = conn.prepareStatement("DELETE FROM accounts WHERE account_id = ?")) {
                stmt.setInt(1, userId);
                stmt.executeUpdate();

                JOptionPane.showMessageDialog(this, "User deleted.");
                loadUsersIntoTable(table);
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Error deleting user: " + e.getMessage(), "DB Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private JPanel createCategorySetupPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Title
        JLabel title = new JLabel("Item Category Setup");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        panel.add(title, BorderLayout.NORTH);

        // Create table model with columns matching your database
        DefaultTableModel model = new DefaultTableModel() {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table non-editable
            }
        };
        model.setColumnIdentifiers(new String[]{"Category ID", "Category Name"});

        // Create table and configure it
        JTable table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setReorderingAllowed(false);

        // Set preferred column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(100); // ID column
        table.getColumnModel().getColumn(1).setPreferredWidth(200); // Name column

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Existing Categories"));
        panel.add(scrollPane, BorderLayout.CENTER);

        // --- Input Panel ---
        JPanel inputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));

        JTextField txtNewCategory = new JTextField(20);
        JButton btnAdd = new JButton("➕ Add Category");
        JButton btnEdit = new JButton("✏️ Edit Selected");
        JButton btnDelete = new JButton("🗑 Delete Selected");

        // Style buttons
        for (JButton btn : new JButton[]{btnAdd, btnEdit, btnDelete}) {
            btn.setBackground(new Color(0, 51, 102));
            btn.setForeground(Color.WHITE);
            btn.setFocusPainted(false);
        }

        inputPanel.add(new JLabel("Category Name:"));
        inputPanel.add(txtNewCategory);
        inputPanel.add(btnAdd);
        inputPanel.add(btnEdit);
        inputPanel.add(btnDelete);

        JPanel buttonContainer = new JPanel(new BorderLayout());
        buttonContainer.add(inputPanel, BorderLayout.WEST);
        panel.add(buttonContainer, BorderLayout.SOUTH);

        // --- Load categories from database ---
        loadCategoriesFromDatabase(model);

        // --- Add Button Action ---
        btnAdd.addActionListener(e -> {
            String newCategory = txtNewCategory.getText().trim();

            if (newCategory.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "Category name cannot be empty", 
                    "Input Error", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Check if category already exists
            if (categoryExists(newCategory)) {
                JOptionPane.showMessageDialog(this, 
                    "Category '" + newCategory + "' already exists", 
                    "Duplicate Category", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }

            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/storvendb", "root", "");
                 PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO categories (category_name) VALUES (?)", 
                     Statement.RETURN_GENERATED_KEYS)) {

                stmt.setString(1, newCategory);
                int rowsAffected = stmt.executeUpdate();

                if (rowsAffected > 0) {
                    // Get the generated ID
                    ResultSet rs = stmt.getGeneratedKeys();
                    if (rs.next()) {
                        int newId = rs.getInt(1);
                        model.addRow(new Object[]{newId, newCategory});
                    }

                    txtNewCategory.setText("");
                    JOptionPane.showMessageDialog(this, 
                        "Category added successfully!", 
                        "Success", 
                        JOptionPane.INFORMATION_MESSAGE);
                    refreshCategoryComboBoxes();
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, 
                    "Error adding category: " + ex.getMessage(), 
                    "Database Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });

        // --- Edit Button Action ---
        btnEdit.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();

            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, 
                    "Please select a category to edit", 
                    "No Selection", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }

            int categoryId = (int) model.getValueAt(selectedRow, 0);
            String currentName = (String) model.getValueAt(selectedRow, 1);

            // Show input dialog for new name
            String newName = (String) JOptionPane.showInputDialog(
                this,
                "Enter new category name:",
                "Edit Category",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                currentName
            );

            if (newName == null || newName.trim().isEmpty() || newName.equals(currentName)) {
                return; // User cancelled or entered empty/unchanged name
            }

            // Check if new name already exists
            if (categoryExists(newName)) {
                JOptionPane.showMessageDialog(this, 
                    "Category '" + newName + "' already exists", 
                    "Duplicate Category", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }

            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/storvendb", "root", "");
                 PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE categories SET category_name = ? WHERE category_id = ?")) {

                stmt.setString(1, newName);
                stmt.setInt(2, categoryId);
                int rowsAffected = stmt.executeUpdate();

                if (rowsAffected > 0) {
                    model.setValueAt(newName, selectedRow, 1);
                    JOptionPane.showMessageDialog(this, 
                        "Category updated successfully!", 
                        "Success", 
                        JOptionPane.INFORMATION_MESSAGE);
                        refreshCategoryComboBoxes();
                    // Update any inventory items using this category
                    updateInventoryCategories(currentName, newName);
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, 
                    "Error updating category: " + ex.getMessage(), 
                    "Database Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        });

        // --- Delete Button Action ---
        btnDelete.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();

            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, 
                    "Please select a category to delete", 
                    "No Selection", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }

            int categoryId = (int) model.getValueAt(selectedRow, 0);
            String categoryName = (String) model.getValueAt(selectedRow, 1);

            // Check if category is in use
            if (isCategoryInUse(categoryName)) {
                JOptionPane.showMessageDialog(this, 
                    "Cannot delete category '" + categoryName + "' because it is being used by products.\n" +
                    "Please reassign or delete those products first.", 
                    "Category In Use", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(
                this, 
                "Are you sure you want to delete category '" + categoryName + "'?",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );

            if (confirm == JOptionPane.YES_OPTION) {
                try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/storvendb", "root", "");
                     PreparedStatement stmt = conn.prepareStatement(
                         "DELETE FROM categories WHERE category_id = ?")) {

                    stmt.setInt(1, categoryId);
                    int rowsAffected = stmt.executeUpdate();

                    if (rowsAffected > 0) {
                        model.removeRow(selectedRow);
                        JOptionPane.showMessageDialog(this, 
                            "Category deleted successfully!", 
                            "Success", 
                            JOptionPane.INFORMATION_MESSAGE);
                            refreshCategoryComboBoxes();
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(this, 
                        "Error deleting category: " + ex.getMessage(), 
                        "Database Error", 
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        return panel;
    }
    
    private void initializeCategoryComboBoxes() {
        // Initialize purchase category combo box
        cmbPurchaseCategory = new JComboBox<>(getCategoriesFromDatabase());
        cmbPurchaseCategory.setPreferredSize(new Dimension(231, 30));

        // Refresh all combo boxes
        refreshCategoryComboBoxes();
    }
    
    public void refreshCategoryComboBoxes() {
        String[] categories = getCategoriesFromDatabase();

        // For inventory page filter combo box (includes "All" option)
        if (cmbCategory != null) {
            String[] categoriesWithAll = new String[categories.length + 1];
            categoriesWithAll[0] = "All";
            System.arraycopy(categories, 0, categoriesWithAll, 1, categories.length);
            cmbCategory.setModel(new javax.swing.DefaultComboBoxModel<>(categoriesWithAll));
        }
        if (cmbPurchaseCategory != null) {
            String[] categoriesWithAll = new String[categories.length + 1];
            categoriesWithAll[0] = "";
            System.arraycopy(categories, 0, categoriesWithAll, 1, categories.length);
            cmbPurchaseCategory.setModel(new javax.swing.DefaultComboBoxModel<>(categoriesWithAll));
        }
    }
    
    // Add this method to AdminView.java
    public String[] getCategoriesFromDatabase() {
        List<String> categories = new ArrayList<>();
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/storvendb", "root", "");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT category_name FROM categories ORDER BY category_name")) {

            while (rs.next()) {
                categories.add(rs.getString("category_name"));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error loading categories: " + e.getMessage(), 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
        }
        return categories.toArray(new String[0]);
    }
    
    // Helper method to check if category exists
    private boolean categoryExists(String categoryName) {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/storvendb", "root", "");
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT COUNT(*) FROM categories WHERE category_name = ?")) {

            stmt.setString(1, categoryName);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Helper method to check if category is being used by products
    private boolean isCategoryInUse(String categoryName) {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/storvendb", "root", "");
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT COUNT(*) FROM inventory WHERE category = ?")) {

            stmt.setString(1, categoryName);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Helper method to update inventory when category name changes
    private void updateInventoryCategories(String oldName, String newName) {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/storvendb", "root", "");
             PreparedStatement stmt = conn.prepareStatement(
                 "UPDATE inventory SET category = ? WHERE category = ?")) {

            stmt.setString(1, newName);
            stmt.setString(2, oldName);
            stmt.executeUpdate();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error updating product categories: " + e.getMessage(), 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    // Method to load categories into table (unchanged from your original)
    private void loadCategoriesFromDatabase(DefaultTableModel model) {
        model.setRowCount(0); // Clear existing data

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/storvendb", "root", "");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM categories ORDER BY category_name")) {

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("category_id"),
                    rs.getString("category_name")
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error loading categories: " + e.getMessage(), 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private JPanel createBackupPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(30, 30, 30, 30));

        JLabel title = new JLabel("Backup MySQL Database");
        title.setFont(new Font("Segoe UI", Font.BOLD, 16));
        title.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton btnBackup = new JButton("Create Backup Now");
        btnBackup.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnBackup.setBackground(new Color(0, 102, 0));
        btnBackup.setForeground(Color.WHITE);
        btnBackup.setFocusPainted(false);
        btnBackup.setPreferredSize(new Dimension(200, 35));
        btnBackup.addActionListener(e -> backupDatabase());

        panel.add(title);
        panel.add(Box.createVerticalStrut(20));
        panel.add(btnBackup);

        return panel;
    }
    
    private void backupDatabase() {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to create a Backup for your database?", "Create", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                String backupFileName = "storvendb" + ".sql";

                String executeCmd = "C:\\xampp\\mysql\\bin\\mysqldump -u root storvendb -r " + backupFileName;

                Process runtimeProcess = Runtime.getRuntime().exec(executeCmd);
                int processComplete = runtimeProcess.waitFor();

                if (processComplete == 0) {
                    JOptionPane.showMessageDialog(this, "Backup created successfully:\n" + 
                        new File(backupFileName).getAbsolutePath());
                } else {
                    JOptionPane.showMessageDialog(this, "Backup failed. Please check XAMPP and MySQL.");
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Backup Error: " + e.getMessage());
            }
        } else {
                
        }
    }
    
    
//END SETTINGS PAGE COMPONENTS/METHODS       

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        LeftPanel = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        btnHome = new javax.swing.JToggleButton();
        btnInventory = new javax.swing.JToggleButton();
        btnNotifications = new javax.swing.JToggleButton();
        btnPurchase = new javax.swing.JToggleButton();
        btnSettings = new javax.swing.JToggleButton();
        btnLogout = new javax.swing.JToggleButton();
        AdminParent = new javax.swing.JPanel();
        HomePage = new javax.swing.JPanel();
        TopPanel1 = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        TotalSalesPanel = new javax.swing.JPanel();
        btnYesterday = new javax.swing.JToggleButton();
        btnToday = new javax.swing.JToggleButton();
        btnThisWeek = new javax.swing.JToggleButton();
        btnThisMonth = new javax.swing.JToggleButton();
        btnThisYear = new javax.swing.JToggleButton();
        labelSoldItems = new javax.swing.JLabel();
        labelTotalSold = new javax.swing.JLabel();
        labelTotalSales = new javax.swing.JLabel();
        labelTotalAmount = new javax.swing.JLabel();
        REDUNDANCY1 = new javax.swing.JPanel();
        GraphPanel = new javax.swing.JPanel();
        InventoryPage = new javax.swing.JPanel();
        TopPanel2 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        btnProductExpiration = new javax.swing.JToggleButton();
        cmbCategory = new javax.swing.JComboBox<>();
        txtSearchProduct = new javax.swing.JTextField();
        btnSearchProduct = new javax.swing.JButton();
        REDUNDANCY2 = new javax.swing.JPanel();
        TableParent = new javax.swing.JPanel();
        scrollpAllProducts = new javax.swing.JScrollPane();
        tableAllProducts = new javax.swing.JTable();
        scrollpProductExpiration = new javax.swing.JScrollPane();
        tableProductExpiration = new javax.swing.JTable();
        btnAddProduct = new javax.swing.JButton();
        btnUpdateProduct = new javax.swing.JButton();
        btnDeleteProduct = new javax.swing.JButton();
        NotificationsPage = new javax.swing.JPanel();
        TopPanel3 = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        MidPanel = new javax.swing.JPanel();
        REDUNDANCY3 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        PurchasePage = new javax.swing.JPanel();
        TopPanel4 = new javax.swing.JPanel();
        jLabel12 = new javax.swing.JLabel();
        PurchaseParent = new javax.swing.JPanel();
        SettingsPage = new javax.swing.JPanel();
        TopPanel5 = new javax.swing.JPanel();
        jLabel13 = new javax.swing.JLabel();
        SettingsParent = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        LeftPanel.setBackground(new java.awt.Color(255, 255, 255));
        LeftPanel.setPreferredSize(new java.awt.Dimension(0, 0));

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/Logo 120x120.png"))); // NOI18N
        jLabel1.setText("jLabel1");
        jLabel1.setPreferredSize(new java.awt.Dimension(0, 0));

        btnHome.setBackground(new java.awt.Color(255, 255, 255));
        btnHome.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnHome.setForeground(new java.awt.Color(0, 0, 51));
        btnHome.setText("    Home");
        btnHome.setBorder(null);
        btnHome.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnHome.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        btnHome.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnHomeActionPerformed(evt);
            }
        });

        btnInventory.setBackground(new java.awt.Color(255, 255, 255));
        btnInventory.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnInventory.setForeground(new java.awt.Color(0, 0, 51));
        btnInventory.setText("    Inventory");
        btnInventory.setBorder(null);
        btnInventory.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnInventory.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        btnInventory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnInventoryActionPerformed(evt);
            }
        });

        btnNotifications.setBackground(new java.awt.Color(255, 255, 255));
        btnNotifications.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnNotifications.setForeground(new java.awt.Color(0, 0, 51));
        btnNotifications.setText("    Notifications");
        btnNotifications.setBorder(null);
        btnNotifications.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnNotifications.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        btnNotifications.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNotificationsActionPerformed(evt);
            }
        });

        btnPurchase.setBackground(new java.awt.Color(255, 255, 255));
        btnPurchase.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnPurchase.setForeground(new java.awt.Color(0, 0, 51));
        btnPurchase.setText("    Purchase");
        btnPurchase.setBorder(null);
        btnPurchase.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnPurchase.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        btnPurchase.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPurchaseActionPerformed(evt);
            }
        });

        btnSettings.setBackground(new java.awt.Color(255, 255, 255));
        btnSettings.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnSettings.setForeground(new java.awt.Color(0, 0, 51));
        btnSettings.setText("    Settings");
        btnSettings.setBorder(null);
        btnSettings.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnSettings.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        btnSettings.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSettingsActionPerformed(evt);
            }
        });

        btnLogout.setBackground(new java.awt.Color(255, 255, 255));
        btnLogout.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnLogout.setForeground(new java.awt.Color(0, 0, 51));
        btnLogout.setText("    Logout");
        btnLogout.setBorder(null);
        btnLogout.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnLogout.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        btnLogout.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLogoutActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout LeftPanelLayout = new javax.swing.GroupLayout(LeftPanel);
        LeftPanel.setLayout(LeftPanelLayout);
        LeftPanelLayout.setHorizontalGroup(
            LeftPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(LeftPanelLayout.createSequentialGroup()
                .addGroup(LeftPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(LeftPanelLayout.createSequentialGroup()
                        .addGap(5, 5, 5)
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(LeftPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(LeftPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(btnPurchase, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnNotifications, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnInventory, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnHome, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(LeftPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(LeftPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(btnSettings, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnLogout, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 110, Short.MAX_VALUE))))
                .addGap(5, 5, 5))
        );
        LeftPanelLayout.setVerticalGroup(
            LeftPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(LeftPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnHome, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnInventory, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnNotifications, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnPurchase, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnSettings, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnLogout, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(23, 23, 23))
        );

        AdminParent.setBackground(new java.awt.Color(255, 255, 255));
        AdminParent.setPreferredSize(new java.awt.Dimension(0, 0));
        AdminParent.setLayout(new java.awt.CardLayout());

        HomePage.setBackground(new java.awt.Color(0, 0, 51));
        HomePage.setPreferredSize(new java.awt.Dimension(670, 460));

        TopPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jLabel10.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(0, 0, 51));
        jLabel10.setText("Admin Home Page");

        javax.swing.GroupLayout TopPanel1Layout = new javax.swing.GroupLayout(TopPanel1);
        TopPanel1.setLayout(TopPanel1Layout);
        TopPanel1Layout.setHorizontalGroup(
            TopPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(TopPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel10)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        TopPanel1Layout.setVerticalGroup(
            TopPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(TopPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        TotalSalesPanel.setBackground(new java.awt.Color(255, 255, 255));

        btnYesterday.setText("Yesterday");

        btnToday.setText("Today");

        btnThisWeek.setText("This Week");

        btnThisMonth.setText("This Month");

        btnThisYear.setText("This Year");

        labelSoldItems.setFont(new java.awt.Font("Segoe UI Black", 1, 24)); // NOI18N
        labelSoldItems.setForeground(new java.awt.Color(0, 0, 0));
        labelSoldItems.setText("Sold Items:");

        labelTotalSold.setFont(new java.awt.Font("Segoe UI Black", 1, 24)); // NOI18N
        labelTotalSold.setForeground(new java.awt.Color(0, 0, 0));
        labelTotalSold.setText("0");

        labelTotalSales.setFont(new java.awt.Font("Segoe UI Black", 1, 24)); // NOI18N
        labelTotalSales.setForeground(new java.awt.Color(0, 0, 0));
        labelTotalSales.setText("Total Sales:");

        labelTotalAmount.setFont(new java.awt.Font("Segoe UI Black", 1, 24)); // NOI18N
        labelTotalAmount.setForeground(new java.awt.Color(0, 0, 0));
        labelTotalAmount.setText("₱0.00");

        javax.swing.GroupLayout TotalSalesPanelLayout = new javax.swing.GroupLayout(TotalSalesPanel);
        TotalSalesPanel.setLayout(TotalSalesPanelLayout);
        TotalSalesPanelLayout.setHorizontalGroup(
            TotalSalesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(TotalSalesPanelLayout.createSequentialGroup()
                .addGap(32, 32, 32)
                .addGroup(TotalSalesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(TotalSalesPanelLayout.createSequentialGroup()
                        .addGap(15, 15, 15)
                        .addComponent(labelSoldItems)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(labelTotalSold)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(labelTotalSales)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(labelTotalAmount)
                        .addGap(15, 15, 15))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, TotalSalesPanelLayout.createSequentialGroup()
                        .addComponent(btnYesterday, javax.swing.GroupLayout.DEFAULT_SIZE, 130, Short.MAX_VALUE)
                        .addGap(35, 35, 35)
                        .addComponent(btnToday, javax.swing.GroupLayout.DEFAULT_SIZE, 130, Short.MAX_VALUE)
                        .addGap(35, 35, 35)
                        .addComponent(btnThisWeek, javax.swing.GroupLayout.DEFAULT_SIZE, 130, Short.MAX_VALUE)
                        .addGap(35, 35, 35)
                        .addComponent(btnThisMonth, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(35, 35, 35)
                        .addComponent(btnThisYear, javax.swing.GroupLayout.DEFAULT_SIZE, 130, Short.MAX_VALUE)))
                .addGap(36, 36, 36))
        );
        TotalSalesPanelLayout.setVerticalGroup(
            TotalSalesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(TotalSalesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(TotalSalesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnYesterday, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnToday, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnThisWeek, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnThisMonth, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnThisYear, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(16, 16, 16)
                .addGroup(TotalSalesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(labelTotalSales, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelTotalAmount, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(labelSoldItems, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(labelTotalSold, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        GraphPanel.setLayout(new java.awt.CardLayout());

        javax.swing.GroupLayout REDUNDANCY1Layout = new javax.swing.GroupLayout(REDUNDANCY1);
        REDUNDANCY1.setLayout(REDUNDANCY1Layout);
        REDUNDANCY1Layout.setHorizontalGroup(
            REDUNDANCY1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
            .addGroup(REDUNDANCY1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(REDUNDANCY1Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(GraphPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 646, Short.MAX_VALUE)
                    .addContainerGap()))
        );
        REDUNDANCY1Layout.setVerticalGroup(
            REDUNDANCY1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 434, Short.MAX_VALUE)
            .addGroup(REDUNDANCY1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(REDUNDANCY1Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(GraphPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 268, Short.MAX_VALUE)
                    .addContainerGap()))
        );

        javax.swing.GroupLayout HomePageLayout = new javax.swing.GroupLayout(HomePage);
        HomePage.setLayout(HomePageLayout);
        HomePageLayout.setHorizontalGroup(
            HomePageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(TopPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(HomePageLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(HomePageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(REDUNDANCY1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(TotalSalesPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        HomePageLayout.setVerticalGroup(
            HomePageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(HomePageLayout.createSequentialGroup()
                .addComponent(TopPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(TotalSalesPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(REDUNDANCY1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        AdminParent.add(HomePage, "card2");

        InventoryPage.setBackground(new java.awt.Color(0, 0, 51));
        InventoryPage.setPreferredSize(new java.awt.Dimension(670, 460));

        TopPanel2.setBackground(new java.awt.Color(255, 255, 255));

        jLabel8.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(0, 0, 51));
        jLabel8.setText("Inventory");

        javax.swing.GroupLayout TopPanel2Layout = new javax.swing.GroupLayout(TopPanel2);
        TopPanel2.setLayout(TopPanel2Layout);
        TopPanel2Layout.setHorizontalGroup(
            TopPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(TopPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel8)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        TopPanel2Layout.setVerticalGroup(
            TopPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(TopPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel8, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        btnProductExpiration.setBackground(new java.awt.Color(0, 51, 102));
        btnProductExpiration.setForeground(new java.awt.Color(255, 255, 255));
        btnProductExpiration.setText("Product Expiration");
        btnProductExpiration.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnProductExpirationActionPerformed(evt);
            }
        });

        cmbCategory.setBackground(new java.awt.Color(0, 51, 102));
        cmbCategory.setForeground(new java.awt.Color(255, 255, 255));
        cmbCategory.setToolTipText("Category");
        cmbCategory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbCategoryActionPerformed(evt);
            }
        });

        txtSearchProduct.setBackground(new java.awt.Color(204, 204, 204));
        txtSearchProduct.setForeground(new java.awt.Color(0, 0, 0));
        txtSearchProduct.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtSearchProductActionPerformed(evt);
            }
        });

        btnSearchProduct.setBackground(new java.awt.Color(0, 51, 102));
        btnSearchProduct.setForeground(new java.awt.Color(255, 255, 255));
        btnSearchProduct.setText("Search");
        btnSearchProduct.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSearchProductActionPerformed(evt);
            }
        });

        TableParent.setBackground(new java.awt.Color(255, 255, 255));
        TableParent.setLayout(new java.awt.CardLayout());

        scrollpAllProducts.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        scrollpAllProducts.setPreferredSize(new java.awt.Dimension(658, 298));

        tableAllProducts.setBackground(new java.awt.Color(255, 255, 255));
        tableAllProducts.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null}
            },
            new String [] {
                "Product ID", "Product Name", "Description", "Category", "Stock Level", "Reorder Level", "Price (Pesos)"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Double.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        tableAllProducts.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        tableAllProducts.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        tableAllProducts.setMaximumSize(new java.awt.Dimension(214, 140));
        tableAllProducts.setRowHeight(30);
        tableAllProducts.setRowMargin(5);
        tableAllProducts.setShowHorizontalLines(true);
        scrollpAllProducts.setViewportView(tableAllProducts);

        TableParent.add(scrollpAllProducts, "card2");

        scrollpProductExpiration.setViewportBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        scrollpProductExpiration.setColumnHeaderView(null);
        scrollpProductExpiration.setPreferredSize(new java.awt.Dimension(658, 298));

        tableProductExpiration.setBackground(new java.awt.Color(255, 255, 255));
        tableProductExpiration.setForeground(new java.awt.Color(0, 0, 0));
        tableProductExpiration.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Product Name", "Description", "Category", "Expiration Date"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        tableProductExpiration.setRowHeight(30);
        tableProductExpiration.setRowMargin(5);
        tableProductExpiration.setShowHorizontalLines(true);
        scrollpProductExpiration.setViewportView(tableProductExpiration);

        TableParent.add(scrollpProductExpiration, "card3");

        javax.swing.GroupLayout REDUNDANCY2Layout = new javax.swing.GroupLayout(REDUNDANCY2);
        REDUNDANCY2.setLayout(REDUNDANCY2Layout);
        REDUNDANCY2Layout.setHorizontalGroup(
            REDUNDANCY2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(REDUNDANCY2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(TableParent, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addContainerGap())
        );
        REDUNDANCY2Layout.setVerticalGroup(
            REDUNDANCY2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(REDUNDANCY2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(TableParent, javax.swing.GroupLayout.DEFAULT_SIZE, 401, Short.MAX_VALUE)
                .addContainerGap())
        );

        btnAddProduct.setBackground(new java.awt.Color(0, 51, 102));
        btnAddProduct.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnAddProduct.setForeground(new java.awt.Color(255, 255, 255));
        btnAddProduct.setText("Add Product");
        btnAddProduct.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddProductActionPerformed(evt);
            }
        });

        btnUpdateProduct.setBackground(new java.awt.Color(0, 51, 102));
        btnUpdateProduct.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnUpdateProduct.setForeground(new java.awt.Color(255, 255, 255));
        btnUpdateProduct.setText("Update Product");
        btnUpdateProduct.setPreferredSize(new java.awt.Dimension(107, 27));
        btnUpdateProduct.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpdateProductActionPerformed(evt);
            }
        });

        btnDeleteProduct.setBackground(new java.awt.Color(0, 51, 102));
        btnDeleteProduct.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnDeleteProduct.setForeground(new java.awt.Color(255, 255, 255));
        btnDeleteProduct.setText("Delete Product");
        btnDeleteProduct.setPreferredSize(new java.awt.Dimension(107, 27));
        btnDeleteProduct.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteProductActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout InventoryPageLayout = new javax.swing.GroupLayout(InventoryPage);
        InventoryPage.setLayout(InventoryPageLayout);
        InventoryPageLayout.setHorizontalGroup(
            InventoryPageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(TopPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, InventoryPageLayout.createSequentialGroup()
                .addGap(58, 58, 58)
                .addComponent(btnAddProduct, javax.swing.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE)
                .addGap(75, 75, 75)
                .addComponent(btnUpdateProduct, javax.swing.GroupLayout.DEFAULT_SIZE, 208, Short.MAX_VALUE)
                .addGap(75, 75, 75)
                .addComponent(btnDeleteProduct, javax.swing.GroupLayout.DEFAULT_SIZE, 202, Short.MAX_VALUE)
                .addGap(62, 62, 62))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, InventoryPageLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(InventoryPageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(REDUNDANCY2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(InventoryPageLayout.createSequentialGroup()
                        .addComponent(cmbCategory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnProductExpiration)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(txtSearchProduct, javax.swing.GroupLayout.PREFERRED_SIZE, 224, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnSearchProduct, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        InventoryPageLayout.setVerticalGroup(
            InventoryPageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, InventoryPageLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(TopPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(12, 12, 12)
                .addGroup(InventoryPageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnProductExpiration, javax.swing.GroupLayout.DEFAULT_SIZE, 34, Short.MAX_VALUE)
                    .addComponent(cmbCategory)
                    .addComponent(txtSearchProduct, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSearchProduct, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(REDUNDANCY2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(InventoryPageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(InventoryPageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(btnUpdateProduct, javax.swing.GroupLayout.DEFAULT_SIZE, 41, Short.MAX_VALUE)
                        .addComponent(btnAddProduct, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(btnDeleteProduct, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(14, 14, 14))
        );

        AdminParent.add(InventoryPage, "card3");

        NotificationsPage.setBackground(new java.awt.Color(0, 0, 51));
        NotificationsPage.setPreferredSize(new java.awt.Dimension(670, 460));

        TopPanel3.setBackground(new java.awt.Color(255, 255, 255));

        jLabel11.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(0, 0, 51));
        jLabel11.setText("Notifications");

        javax.swing.GroupLayout TopPanel3Layout = new javax.swing.GroupLayout(TopPanel3);
        TopPanel3.setLayout(TopPanel3Layout);
        TopPanel3Layout.setHorizontalGroup(
            TopPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(TopPanel3Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(jLabel11)
                .addContainerGap(720, Short.MAX_VALUE))
        );
        TopPanel3Layout.setVerticalGroup(
            TopPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(TopPanel3Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(jLabel11)
                .addContainerGap(16, Short.MAX_VALUE))
        );

        MidPanel.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout MidPanelLayout = new javax.swing.GroupLayout(MidPanel);
        MidPanel.setLayout(MidPanelLayout);
        MidPanelLayout.setHorizontalGroup(
            MidPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        MidPanelLayout.setVerticalGroup(
            MidPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 50, Short.MAX_VALUE)
        );

        REDUNDANCY3.setPreferredSize(new java.awt.Dimension(630, 348));

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 458, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout REDUNDANCY3Layout = new javax.swing.GroupLayout(REDUNDANCY3);
        REDUNDANCY3.setLayout(REDUNDANCY3Layout);
        REDUNDANCY3Layout.setHorizontalGroup(
            REDUNDANCY3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(REDUNDANCY3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        REDUNDANCY3Layout.setVerticalGroup(
            REDUNDANCY3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, REDUNDANCY3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout NotificationsPageLayout = new javax.swing.GroupLayout(NotificationsPage);
        NotificationsPage.setLayout(NotificationsPageLayout);
        NotificationsPageLayout.setHorizontalGroup(
            NotificationsPageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(TopPanel3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(NotificationsPageLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(NotificationsPageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(REDUNDANCY3, javax.swing.GroupLayout.DEFAULT_SIZE, 858, Short.MAX_VALUE)
                    .addComponent(MidPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        NotificationsPageLayout.setVerticalGroup(
            NotificationsPageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(NotificationsPageLayout.createSequentialGroup()
                .addComponent(TopPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(MidPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(REDUNDANCY3, javax.swing.GroupLayout.DEFAULT_SIZE, 470, Short.MAX_VALUE)
                .addContainerGap())
        );

        AdminParent.add(NotificationsPage, "card4");

        PurchasePage.setBackground(new java.awt.Color(0, 0, 51));
        PurchasePage.setPreferredSize(new java.awt.Dimension(670, 460));

        TopPanel4.setBackground(new java.awt.Color(255, 255, 255));

        jLabel12.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        jLabel12.setForeground(new java.awt.Color(0, 0, 51));
        jLabel12.setText("Purchase");

        javax.swing.GroupLayout TopPanel4Layout = new javax.swing.GroupLayout(TopPanel4);
        TopPanel4.setLayout(TopPanel4Layout);
        TopPanel4Layout.setHorizontalGroup(
            TopPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(TopPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel12)
                .addContainerGap(769, Short.MAX_VALUE))
        );
        TopPanel4Layout.setVerticalGroup(
            TopPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(TopPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout PurchaseParentLayout = new javax.swing.GroupLayout(PurchaseParent);
        PurchaseParent.setLayout(PurchaseParentLayout);
        PurchaseParentLayout.setHorizontalGroup(
            PurchaseParentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        PurchaseParentLayout.setVerticalGroup(
            PurchaseParentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 526, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout PurchasePageLayout = new javax.swing.GroupLayout(PurchasePage);
        PurchasePage.setLayout(PurchasePageLayout);
        PurchasePageLayout.setHorizontalGroup(
            PurchasePageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(TopPanel4, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(PurchasePageLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(PurchaseParent, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        PurchasePageLayout.setVerticalGroup(
            PurchasePageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PurchasePageLayout.createSequentialGroup()
                .addComponent(TopPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(PurchaseParent, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        AdminParent.add(PurchasePage, "card5");

        SettingsPage.setBackground(new java.awt.Color(0, 0, 51));
        SettingsPage.setPreferredSize(new java.awt.Dimension(670, 460));

        TopPanel5.setBackground(new java.awt.Color(255, 255, 255));

        jLabel13.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        jLabel13.setForeground(new java.awt.Color(0, 0, 51));
        jLabel13.setText("Settings");

        javax.swing.GroupLayout TopPanel5Layout = new javax.swing.GroupLayout(TopPanel5);
        TopPanel5.setLayout(TopPanel5Layout);
        TopPanel5Layout.setHorizontalGroup(
            TopPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(TopPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel13)
                .addContainerGap(778, Short.MAX_VALUE))
        );
        TopPanel5Layout.setVerticalGroup(
            TopPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(TopPanel5Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel13, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        SettingsParent.setBackground(new java.awt.Color(255, 255, 255));

        javax.swing.GroupLayout SettingsParentLayout = new javax.swing.GroupLayout(SettingsParent);
        SettingsParent.setLayout(SettingsParentLayout);
        SettingsParentLayout.setHorizontalGroup(
            SettingsParentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        SettingsParentLayout.setVerticalGroup(
            SettingsParentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 526, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout SettingsPageLayout = new javax.swing.GroupLayout(SettingsPage);
        SettingsPage.setLayout(SettingsPageLayout);
        SettingsPageLayout.setHorizontalGroup(
            SettingsPageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(TopPanel5, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(SettingsPageLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(SettingsParent, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        SettingsPageLayout.setVerticalGroup(
            SettingsPageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(SettingsPageLayout.createSequentialGroup()
                .addComponent(TopPanel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(SettingsParent, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        AdminParent.add(SettingsPage, "card6");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(LeftPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(AdminParent, javax.swing.GroupLayout.DEFAULT_SIZE, 870, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(LeftPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 600, Short.MAX_VALUE)
            .addComponent(AdminParent, javax.swing.GroupLayout.DEFAULT_SIZE, 600, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
      
//SIDEBAR MENU
    private void btnHomeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHomeActionPerformed
        
        
        AdminParent.removeAll();
        AdminParent.add(HomePage);
        AdminParent.repaint();
        AdminParent.revalidate();
    }//GEN-LAST:event_btnHomeActionPerformed

    private void btnInventoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnInventoryActionPerformed
        refreshCategoryComboBoxes();

        String[] columnNames = {"Product ID", "Product Name", "Description", "Category", "Stock Level", "Reorder Level", "Price"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        tableAllProducts.setModel(model);
        
        loadData();
                
        AdminParent.removeAll();
        AdminParent.add(InventoryPage);
        AdminParent.repaint();
        AdminParent.revalidate();      
    }//GEN-LAST:event_btnInventoryActionPerformed

    private void btnNotificationsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNotificationsActionPerformed
        checkForNewNotifications(); // Check for new notifications first
        loadNotifications(); // Then load all notifications
        
        AdminParent.removeAll();
        AdminParent.add(NotificationsPage);
        AdminParent.repaint();
        AdminParent.revalidate();
    }//GEN-LAST:event_btnNotificationsActionPerformed

    private void btnPurchaseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPurchaseActionPerformed
        refreshCategoryComboBoxes();
        
        AdminParent.removeAll();
        AdminParent.add(PurchasePage);
        AdminParent.repaint();
        AdminParent.revalidate();
    }//GEN-LAST:event_btnPurchaseActionPerformed

    private void btnSettingsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSettingsActionPerformed
        AdminParent.removeAll();
        AdminParent.add(SettingsPage);
        AdminParent.repaint();
        AdminParent.revalidate();
    }//GEN-LAST:event_btnSettingsActionPerformed

    private void btnLogoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLogoutActionPerformed
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to logout?", "Logout", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                dispose();
                new LoginPage().setVisible(true);
                
            } else {
                
            }
    }//GEN-LAST:event_btnLogoutActionPerformed
//END SIDEBAR MENU

//INVENTORY PAGE EVENTS
    private void btnAddProductActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddProductActionPerformed
        InventoryAddProductNew inventory = new InventoryAddProductNew(this);
        inventory.setVisible(true);
    }//GEN-LAST:event_btnAddProductActionPerformed

    private void btnProductExpirationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnProductExpirationActionPerformed
        
        if (btnProductExpiration.isSelected()){
            TableParent.removeAll();
            TableParent.add(scrollpProductExpiration);
            TableParent.repaint();
            TableParent.revalidate();
        } else{        
            TableParent.removeAll();
            TableParent.add(scrollpAllProducts);
            TableParent.repaint();
            TableParent.revalidate();
        }
    }//GEN-LAST:event_btnProductExpirationActionPerformed

    private void btnUpdateProductActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateProductActionPerformed
        int selectedRow = tableAllProducts.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, 
                "Please select a product to update", 
                "No Selection", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // Get data from selected row using correct indexes and safe parsing
            int productId = (int) tableAllProducts.getValueAt(selectedRow, 0); // ID is first column
            String productName = (String) tableAllProducts.getValueAt(selectedRow, 1);
            String description = (String) tableAllProducts.getValueAt(selectedRow, 2);
            String category = (String) tableAllProducts.getValueAt(selectedRow, 3);

            Object stockLevelObj = tableAllProducts.getValueAt(selectedRow, 4);
            int quantity = (stockLevelObj instanceof Integer) ? (Integer) stockLevelObj : Integer.parseInt(stockLevelObj.toString());

            Object reorderLevelObj = tableAllProducts.getValueAt(selectedRow, 5);
            int reorderLevel = (reorderLevelObj instanceof Integer) ? (Integer) reorderLevelObj : Integer.parseInt(reorderLevelObj.toString());

            String price = getPriceFromDB(productId);
            String expirationDate = getExpirationDateFromDB(productId);

            // Open update form (ID is passed but not displayed)
            InventoryUpdateProduct updateForm = new InventoryUpdateProduct(
                productId, productName, category, quantity, 
                reorderLevel, price, expirationDate, description, this
            );
            updateForm.setVisible(true);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error preparing update: " + e.getMessage(), 
                "Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_btnUpdateProductActionPerformed

    private void btnDeleteProductActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteProductActionPerformed
        // Get the selected row
        int selectedRow = tableAllProducts.getSelectedRow();

        // Check if a row is actually selected
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, 
                "Please select a product by clicking on a row first", 
                "No Selection", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Get the Product ID from the selected row (first column)
        int productIdToDelete = (int) tableAllProducts.getValueAt(selectedRow, 0);

        // Confirmation dialog
        String productName = (String) tableAllProducts.getValueAt(selectedRow, 1);
        int confirm = JOptionPane.showConfirmDialog(
            this, 
            """
            Are you sure you want to delete:
            ID: """ + productIdToDelete + "\n" +
            "Name: " + productName + "?", 
            "Confirm Deletion", 
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );


        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/storvendb", "root", "");
                 PreparedStatement stmt = conn.prepareStatement("DELETE FROM inventory WHERE product_id = ?")) {

                stmt.setInt(1, productIdToDelete);
                int rowsAffected = stmt.executeUpdate();

                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Product deleted successfully!");
                    refreshProductTable(); // Refresh the table view
                } else {
                    JOptionPane.showMessageDialog(this, "Product not found in database!");
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, 
                    "Error deleting product: " + e.getMessage(), 
                    "Database Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }//GEN-LAST:event_btnDeleteProductActionPerformed

    private void cmbCategoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbCategoryActionPerformed
        String selectedCategory = (String) cmbCategory.getSelectedItem();
    
        String baseQuery = "SELECT * FROM inventory";
        if (!"All".equals(selectedCategory)) {
            baseQuery += " WHERE category = '" + selectedCategory + "'";
        }

        // Clear both tables first
        DefaultTableModel model1 = (DefaultTableModel) tableAllProducts.getModel();
        model1.setRowCount(0);
        DefaultTableModel model2 = (DefaultTableModel) tableProductExpiration.getModel();
        model2.setRowCount(0);

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/storvendb", "root", "");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(baseQuery)) {

            while (rs.next()) {
                // For tableAllProducts (full details)
                model1.addRow(new Object[]{
                    rs.getInt("product_id"),
                    rs.getString("product_name"),
                    rs.getString("description"),
                    rs.getString("category"),
                    rs.getInt("stock_level"),
                    rs.getInt("reorder_level"),
                    "₱" + rs.getString("price")
                });

                // For tableProductExpiration (only selected columns)
                model2.addRow(new Object[]{
                    rs.getString("product_name"),
                    rs.getString("description"),
                    rs.getString("category"),
                    rs.getString("expiration_date")
                });
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading inventory: " + e.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_cmbCategoryActionPerformed

    private void btnSearchProductActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchProductActionPerformed
        String keyword = txtSearchProduct.getText().trim();
        searchInventoryProducts(keyword);
    }//GEN-LAST:event_btnSearchProductActionPerformed

    private void txtSearchProductActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSearchProductActionPerformed
        String keyword = txtSearchProduct.getText().trim();
        searchInventoryProducts(keyword);
    }//GEN-LAST:event_txtSearchProductActionPerformed
//END INVENTORY PAGE EVENTS
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(AdminView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(AdminView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(AdminView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(AdminView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new AdminView().setVisible(true);
            }
        });
        
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel AdminParent;
    private javax.swing.JPanel GraphPanel;
    private javax.swing.JPanel HomePage;
    private javax.swing.JPanel InventoryPage;
    private javax.swing.JPanel LeftPanel;
    private javax.swing.JPanel MidPanel;
    private javax.swing.JPanel NotificationsPage;
    private javax.swing.JPanel PurchasePage;
    private javax.swing.JPanel PurchaseParent;
    private javax.swing.JPanel REDUNDANCY1;
    private javax.swing.JPanel REDUNDANCY2;
    private javax.swing.JPanel REDUNDANCY3;
    private javax.swing.JPanel SettingsPage;
    private javax.swing.JPanel SettingsParent;
    private javax.swing.JPanel TableParent;
    private javax.swing.JPanel TopPanel1;
    private javax.swing.JPanel TopPanel2;
    private javax.swing.JPanel TopPanel3;
    private javax.swing.JPanel TopPanel4;
    private javax.swing.JPanel TopPanel5;
    private javax.swing.JPanel TotalSalesPanel;
    private javax.swing.JButton btnAddProduct;
    private javax.swing.JButton btnDeleteProduct;
    private javax.swing.JToggleButton btnHome;
    private javax.swing.JToggleButton btnInventory;
    private javax.swing.JToggleButton btnLogout;
    private javax.swing.JToggleButton btnNotifications;
    private javax.swing.JToggleButton btnProductExpiration;
    private javax.swing.JToggleButton btnPurchase;
    private javax.swing.JButton btnSearchProduct;
    private javax.swing.JToggleButton btnSettings;
    private javax.swing.JToggleButton btnThisMonth;
    private javax.swing.JToggleButton btnThisWeek;
    private javax.swing.JToggleButton btnThisYear;
    private javax.swing.JToggleButton btnToday;
    private javax.swing.JButton btnUpdateProduct;
    private javax.swing.JToggleButton btnYesterday;
    private javax.swing.JComboBox<String> cmbCategory;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel labelSoldItems;
    private javax.swing.JLabel labelTotalAmount;
    private javax.swing.JLabel labelTotalSales;
    private javax.swing.JLabel labelTotalSold;
    private javax.swing.JScrollPane scrollpAllProducts;
    private javax.swing.JScrollPane scrollpProductExpiration;
    private javax.swing.JTable tableAllProducts;
    private javax.swing.JTable tableProductExpiration;
    private javax.swing.JTextField txtSearchProduct;
    // End of variables declaration//GEN-END:variables
    private javax.swing.JTable tablePurchasePageHistory;
    private javax.swing.JScrollPane scrollpPurchaseHistory;
    private javax.swing.JTextField txtPurchaseProductName;
    private javax.swing.JTextField txtPurchaseQuantity;
    private javax.swing.JTextField txtPurchasePrice;
    private javax.swing.JButton btnAddPurchase;
    private javax.swing.JButton btnRefreshPurchases;
    private javax.swing.JComboBox<String> cmbPurchaseCategory;
    private javax.swing.JTextField txtPurchaseDescription;
    private javax.swing.JLabel jLabelPurchaseDescription;

    private void loadFilteredNotifications(String type, String sortOrder) {
        jPanel1.removeAll();
        jPanel1.setLayout(new BoxLayout(jPanel1, BoxLayout.Y_AXIS));

        StringBuilder query = new StringBuilder("SELECT * FROM notifications");
        
        // Add type filter
        if (!type.equals("All")) {
            query.append(" WHERE type = '").append(type).append("'");
        }
        
        // Add sorting
        query.append(" ORDER BY created_at ").append(sortOrder.equals("Newest First") ? "DESC" : "ASC");

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/storvendb", "root", "");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query.toString())) {

            SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm a");

            while (rs.next()) {
                int notificationId = rs.getInt("notification_id");
                String message = rs.getString("message");
                boolean isRead = rs.getBoolean("is_read");
                String notificationType = rs.getString("type");

                java.sql.Timestamp timestamp = rs.getTimestamp("created_at");
                String createdAt = (timestamp != null) ? 
                    dateFormat.format(timestamp) : "N/A";

                Color bgColor = isRead ? new Color(240,240,240) : getColorForType(notificationType);

                JPanel card = createNotificationCard(notificationId, message, createdAt, bgColor, isRead, notificationType);
                jPanel1.add(card);
                jPanel1.add(Box.createVerticalStrut(8));
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error loading notifications: " + e.getMessage(), 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
        }

        jPanel1.revalidate();
        jPanel1.repaint();
    }

    private JPanel createNotificationCard(int notificationId, String message, String dateTime, Color bgColor, boolean isRead, String type) {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout());
        card.setBackground(bgColor);
        card.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10),
            javax.swing.BorderFactory.createLineBorder(Color.GRAY)));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Main message panel
        JPanel messagePanel = new JPanel(new BorderLayout());
        messagePanel.setOpaque(false);
        
        // Type label
        JLabel lblType = new JLabel(type);
        lblType.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblType.setForeground(Color.DARK_GRAY);
        messagePanel.add(lblType, BorderLayout.NORTH);

        // Message label
        JLabel lblMessage = new JLabel(message);
        lblMessage.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblMessage.setForeground(Color.BLACK);
        messagePanel.add(lblMessage, BorderLayout.CENTER);

        // Bottom right: time
        JLabel lblTime = new JLabel(dateTime);
        lblTime.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblTime.setForeground(Color.DARK_GRAY);

        // Action buttons
        JButton btnMarkAsRead = new JButton("✓ Read");
        JButton btnDelete = new JButton("🗑 Delete");
        btnMarkAsRead.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnDelete.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btnMarkAsRead.setFocusable(false);
        btnDelete.setFocusable(false);

        if (isRead) {
            btnMarkAsRead.setEnabled(false);
        }

        // Action listeners
        btnMarkAsRead.addActionListener(e -> {
            markNotificationAsRead(notificationId);
            card.setBackground(new Color(220, 220, 220));
            btnMarkAsRead.setEnabled(false);
        });

        btnDelete.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(AdminView.this,
                "Are you sure you want to delete this notification?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);
                
            if (confirm == JOptionPane.YES_OPTION) {
                deleteNotificationFromDB(notificationId);
                jPanel1.remove(card);
                jPanel1.revalidate();
                jPanel1.repaint();
            }
        });

        // Button panel (left)
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        leftPanel.setOpaque(false);
        leftPanel.add(btnMarkAsRead);
        leftPanel.add(btnDelete);

        // Time label (right)
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        rightPanel.setOpaque(false);
        rightPanel.add(lblTime);

        // Wrap buttons and time in bottom panel
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.add(leftPanel, BorderLayout.WEST);
        bottomPanel.add(rightPanel, BorderLayout.EAST);

        // Final layout
        card.add(messagePanel, BorderLayout.CENTER);
        card.add(bottomPanel, BorderLayout.SOUTH);

        return card;
    }
}