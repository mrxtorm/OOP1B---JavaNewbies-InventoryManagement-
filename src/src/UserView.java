package src;

import java.sql.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.CategoryChartBuilder;
import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.style.Styler;

public final class UserView extends javax.swing.JFrame {

    public UserView() {
        initComponents();
        
        setLocationRelativeTo(null);
        
//LEFT PANEL/SIDE BAR        
        //FOR SIDE BAR BUTTONS TO CHANGE THEIR COLORS WHEN SELECTED
        styleToggleButton(btnHome);
        styleToggleButton(btnInventory);
        styleToggleButton(btnRecordSales);
        styleToggleButton(btnAlerts);
        
        //SELECT HOME BUTTON AS DEFAULT
        btnHome.setSelected(true);
        
        //USED TO GROUP THE SIDEBAR BUTTONS IN ORDER TO TOGGLE ONE BUTTON ONLY
        sideBarButtons();
//END LEFT PANEL/SIDE BAR   

//HOME PAGE
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
//END HOME PAGE

//INVENTORY PAGE
        loadData();
//END INVENTORY PAGE
        
//RECORD SALES PAGE
        loadRecordHistory();
        initializeProductList();
        setupProductListSelection();
//END RECORD SALES PAGE 

//ALERTS PAGE
        

        JButton btnMarkAllRead = new JButton("✓ Mark All as Read");
        btnMarkAllRead.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnMarkAllRead.setBackground(Color.WHITE);
        btnMarkAllRead.setFocusable(false);

        btnMarkAllRead.addActionListener(e -> markAllNotificationsAsRead());

        MidPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 10));
        MidPanel.add(btnMarkAllRead);

        JScrollPane notificationScroll = new JScrollPane(jPanel1);
        notificationScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        notificationScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        notificationScroll.getVerticalScrollBar().setUnitIncrement(16);

        REDUNDANCY4.setLayout(new BorderLayout());
        REDUNDANCY4.add(notificationScroll, BorderLayout.CENTER);
//END ALERTS PAGE
    }
    
//LEFT PANEL METHODS
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
    
    private void sideBarButtons() {
        ButtonGroup toggleGroup = new ButtonGroup();
    
        toggleGroup.add(btnHome);
        toggleGroup.add(btnInventory);
        toggleGroup.add(btnRecordSales);
        toggleGroup.add(btnAlerts);
        toggleGroup.add(btnLogout);
    }
//END LEFT PANEL METHODS
 
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
        // Get sales data from database for the specified time period (by category)
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
//END HOMEPAGE COMPONENTS/METHODS

//INVENTORY COMPONENTS/METHODS    
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
        } catch (SQLException e) {
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
//END INVENTORY COMPONENTS/METHODS
    
//RECORD SALES COMPONENTS/METHODS
    public boolean isLoadingProducts = true;
    
    private void updateTotal() {
        DefaultTableModel model = (DefaultTableModel) tableRecordSales.getModel();
        double total = 0;
        for (int i = 0; i < model.getRowCount(); i++) {
            double subtotal = (double) model.getValueAt(i, 3);
            total += subtotal;
        }
        labelTotalPrice.setText(String.format("₱%.2f", total));
    }

    private double getProductPrice(String productName) {
        double price = -1;
        try {Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/storvendb", "root", "");
            String sql = "SELECT price FROM inventory WHERE product_name=?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, productName);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                price = rs.getDouble("price");
            }
            rs.close();
            pst.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return price;
    }

    private int getAvailableStock(String productName) {
        int stock = 0;
        try {Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/storvendb", "root", "");
            String sql = "SELECT stock_level FROM inventory WHERE product_name=?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, productName);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                stock = rs.getInt("stock_level");
            }
            rs.close();
            pst.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return stock;
    }
  
    private void loadRecordHistory() {
        DefaultTableModel model = (DefaultTableModel) tableHistory.getModel();
        model.setRowCount(0);

        DateTimeFormatter displayFormat = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm:ss");

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/storvendb", "root", "");
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(
                "SELECT s.sales_id, i.product_name, i.description, i.category, s.quantity_sold, s.sale_amount, s.sale_datetime " +
                "FROM salerecords s " +
                "JOIN inventory i ON s.product_id = i.product_id " +
                "ORDER BY s.sale_datetime DESC"
            )) {

           while (rs.next()) {
               Timestamp timestamp = rs.getTimestamp("sale_datetime");
               String formattedDateTime = timestamp.toLocalDateTime()
                   .format(displayFormat);

               model.addRow(new Object[]{
                   rs.getInt("sales_id"),
                   rs.getString("product_name"),
                   rs.getString("description"),
                   rs.getString("category"),
                   rs.getInt("quantity_sold"),
                   "₱" + rs.getString("sale_amount"),
                   formattedDateTime
               });
           }
       } catch (Exception e) {
           JOptionPane.showMessageDialog(this, "Error loading sales data: " + e.getMessage());
       }
    }
    
    // Initialize the product list with all products
    private void initializeProductList() {
        DefaultListModel<String> model = new DefaultListModel<>();

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/storvendb", "root", "");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT product_name FROM inventory")) {

            while (rs.next()) {
                model.addElement(rs.getString("product_name"));
            }

            listProductList.setModel(model);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading products: " + e.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
            model.addElement("Error loading products");
            listProductList.setModel(model);
        }
    }

    // Search products method (shows all products if keyword is empty)
    private void searchProducts(String keyword) {
        DefaultListModel<String> model = new DefaultListModel<>();
        String query = "SELECT product_name FROM inventory";

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
                model.addElement(rs.getString("product_name"));
            }

            listProductList.setModel(model);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error searching products: " + e.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addProducts() throws SQLException {
        String productName = listProductList.getSelectedValue();
        String quantityStr = txtQuantity.getText().trim();

        if (productName == null || productName.startsWith("Error") || quantityStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a valid product and enter quantity.");
            return;
        }

        try {
            int quantity = Integer.parseInt(quantityStr);

            if (quantity <= 0) {
                JOptionPane.showMessageDialog(this, "Quantity must be greater than 0.");
                return;
            }

            int availableStock = getAvailableStock(productName);

            DefaultTableModel model = (DefaultTableModel) tableRecordSales.getModel();
            int currentTableQuantity = 0;
            for (int i = 0; i < model.getRowCount(); i++) {
                String existingProduct = model.getValueAt(i, 0).toString();
                if (existingProduct.equals(productName)) {
                    currentTableQuantity = (int) model.getValueAt(i, 2);
                    break;
                }
            }

            if (quantity + currentTableQuantity > availableStock) {
                JOptionPane.showMessageDialog(this, "Not enough stock. Available: " + availableStock);
                return;
            }

            double unitPrice = getProductPrice(productName);
            double subtotal = unitPrice * quantity;

            boolean found = false;

            for (int i = 0; i < model.getRowCount(); i++) {
                String existingProduct = model.getValueAt(i, 0).toString();
                if (existingProduct.equals(productName)) {
                    int existingQty = (int) model.getValueAt(i, 2);
                    int newQty = existingQty + quantity;
                    double newSubtotal = unitPrice * newQty;

                    model.setValueAt(newQty, i, 2);
                    model.setValueAt(newSubtotal, i, 4);
                    found = true;
                    break;
                }
            }

            if (!found) {
                String description = "";
                try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/storvendb", "root", "");
                     PreparedStatement pst = conn.prepareStatement("SELECT description FROM inventory WHERE product_name=?")) {
                    pst.setString(1, productName);
                    ResultSet rs = pst.executeQuery();
                    if (rs.next()) {
                        description = rs.getString("description");
                    }
                }
                model.addRow(new Object[]{
                    productName,
                    description,
                    quantity,
                    unitPrice,
                    subtotal
                });
            }

            updateTotal();
            txtQuantity.setText("");

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number for quantity.");
        }
    }
    
    // Handle product selection from list
    private void setupProductListSelection() {
        listProductList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selectedProduct = listProductList.getSelectedValue();
                if (selectedProduct != null && !selectedProduct.startsWith("Error")) {
                    String description = "";
                    try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/storvendb", "root", "");
                         PreparedStatement pst = conn.prepareStatement("SELECT description FROM inventory WHERE product_name=?")) {
                        pst.setString(1, selectedProduct);
                        ResultSet rs = pst.executeQuery();
                        if (rs.next()) {
                            description = rs.getString("description");
                        }
                    } catch (SQLException ex) {
                        description = "";
                    }
                    labelSelectedProductInfo.setText("" + selectedProduct+ " " + description);
                    // Show product info when selected from the list
                    double unitPrice = getProductPrice(selectedProduct);
                    int stockAvailable = getAvailableStock(selectedProduct);

                    if (unitPrice >= 0) {
                        JOptionPane.showMessageDialog(this, 
                            "Price: ₱" + String.format("%.2f", unitPrice) + "\nAvailable Stock: " + stockAvailable,
                            "Product Information", 
                            JOptionPane.INFORMATION_MESSAGE);
                            txtQuantity.requestFocusInWindow();
                            
                    }
                }else {
                    labelSelectedProductInfo.setText(" ");
                }
            }
        });
    }
    
    private void searchHistorySales(String keyword) {
        DefaultTableModel model = (DefaultTableModel) tableHistory.getModel();
        model.setRowCount(0);

        String baseQuery = "SELECT * FROM salerecords";
        String condition = "";
        if (!keyword.isEmpty()) {
            condition = " WHERE product_name LIKE ?";
        }
        String orderBy = " ORDER BY sale_datetime DESC";
        String query = baseQuery + condition + orderBy;

        SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/storvendb", "root", "")) {
            PreparedStatement pstmt = conn.prepareStatement(query);

            if (!keyword.isEmpty()) {
                pstmt.setString(1, "%" + keyword + "%");
            }

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Timestamp timestamp = rs.getTimestamp("sale_datetime");
                String formattedDateTime = (timestamp != null) ? formatter.format(timestamp) : "";

                model.addRow(new Object[]{
                    rs.getInt("sales_id"),
                    rs.getString("product_name"),
                    rs.getString("description"),
                    rs.getString("category"),
                    rs.getInt("quantity_sold"),
                    "₱ " + rs.getString("sale_amount"),
                    formattedDateTime
                });
            }

            tableHistory.setModel(model);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Error searching products: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void refreshTableHistory() {
        DefaultTableModel model = (DefaultTableModel) tableHistory.getModel();
        model.setRowCount(0);

        String query = "SELECT * FROM salerecords ORDER BY sale_datetime DESC";

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/storvendb", "root", "");
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            SimpleDateFormat formatter = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");

            while (rs.next()) {
                Timestamp timestamp = rs.getTimestamp("sale_datetime");
                String formattedDateTime = (timestamp != null) ? formatter.format(timestamp) : "";

                model.addRow(new Object[] {
                    rs.getInt("sales_id"),
                    rs.getString("product_name"),
                    rs.getString("description"),
                    rs.getString("category"),
                    rs.getInt("quantity_sold"),
                    "₱ " + rs.getString("sale_amount"),
                    formattedDateTime
                });
            }

            tableHistory.setModel(model);

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, 
                "Error refreshing table: " + e.getMessage(), 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
//END RECORD SALES COMPONENTS/METHODS
    
//ALERTS PAGE COMPONENTS/METHODS    
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

                Color bgColor = isRead ? new Color(220, 220, 220) : getColorForType(rs.getString("type"));

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
        // This keeps notifications persistent and compatible with AdminView

        // Then load notifications from DB
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
            deleteNotificationFromDB(notificationId);
            jPanel1.remove(card);
            jPanel1.revalidate();
            jPanel1.repaint();
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
//END ALERTS PAGE COMPONENTS/METHOD

    public void refreshCategoryComboBoxes() {
        String[] categories = getCategoriesFromDatabase();

        // For inventory page filter combo box (includes "All" option)
        if (cmbCategory != null) {
            String[] categoriesWithAll = new String[categories.length + 1];
            categoriesWithAll[0] = "All";
            System.arraycopy(categories, 0, categoriesWithAll, 1, categories.length);
            cmbCategory.setModel(new javax.swing.DefaultComboBoxModel<>(categoriesWithAll));
        } 
        if (cmbRCategory != null) {
            String[] categoriesWithAll = new String[categories.length + 1];
            categoriesWithAll[0] = "All";
            System.arraycopy(categories, 0, categoriesWithAll, 1, categories.length);
            cmbRCategory.setModel(new javax.swing.DefaultComboBoxModel<>(categoriesWithAll));
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
        btnRecordSales = new javax.swing.JToggleButton();
        btnAlerts = new javax.swing.JToggleButton();
        btnLogout = new javax.swing.JToggleButton();
        UserParent = new javax.swing.JPanel();
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
        ViewInventoryPage = new javax.swing.JPanel();
        TopPanel = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        cmbCategory = new javax.swing.JComboBox<>();
        btnProductExpiration = new javax.swing.JToggleButton();
        txtSearchProduct = new javax.swing.JTextField();
        btnSearchProduct = new javax.swing.JButton();
        REDUNDANCY2 = new javax.swing.JPanel();
        TableParent = new javax.swing.JPanel();
        scrollpAllProducts = new javax.swing.JScrollPane();
        tableAllProducts = new javax.swing.JTable();
        scrollpProductExpiration = new javax.swing.JScrollPane();
        tableProductExpiration = new javax.swing.JTable();
        RecordSalesPage = new javax.swing.JPanel();
        TopPanel2 = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        btnRecordSale = new javax.swing.JButton();
        btnRecordHistory = new javax.swing.JButton();
        REDUNDANCY3 = new javax.swing.JPanel();
        RParent = new javax.swing.JPanel();
        RecordSale = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        txtRSearchProduct = new javax.swing.JTextField();
        btnRSSearch = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        listProductList = new javax.swing.JList<>();
        jLabel3 = new javax.swing.JLabel();
        txtQuantity = new javax.swing.JTextField();
        btnAdd = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tableRecordSales = new javax.swing.JTable();
        btnRemove = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        labelTotalPrice = new javax.swing.JLabel();
        btnClear = new javax.swing.JButton();
        btnRecord = new javax.swing.JButton();
        labelSelectedProductInfo = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        RecordHistory = new javax.swing.JPanel();
        btnRefresh = new javax.swing.JButton();
        cmbRCategory = new javax.swing.JComboBox<>();
        txtRHSearch = new javax.swing.JTextField();
        btnRHSearch = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        tableHistory = new javax.swing.JTable();
        AlertsPage = new javax.swing.JPanel();
        TopPanel3 = new javax.swing.JPanel();
        jLabel12 = new javax.swing.JLabel();
        MidPanel = new javax.swing.JPanel();
        REDUNDANCY4 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        LeftPanel.setBackground(new java.awt.Color(255, 255, 255));

        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/images/Logo 120x120.png"))); // NOI18N
        jLabel1.setText("jLabel1");
        jLabel1.setPreferredSize(new java.awt.Dimension(0, 0));

        btnHome.setBackground(new java.awt.Color(255, 255, 255));
        btnHome.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnHome.setForeground(new java.awt.Color(0, 0, 51));
        btnHome.setText("  Home");
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
        btnInventory.setText("  View Inventory");
        btnInventory.setBorder(null);
        btnInventory.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnInventory.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        btnInventory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnInventoryActionPerformed(evt);
            }
        });

        btnRecordSales.setBackground(new java.awt.Color(255, 255, 255));
        btnRecordSales.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnRecordSales.setForeground(new java.awt.Color(0, 0, 51));
        btnRecordSales.setText("  Record Sales");
        btnRecordSales.setBorder(null);
        btnRecordSales.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnRecordSales.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        btnRecordSales.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRecordSalesActionPerformed(evt);
            }
        });

        btnAlerts.setBackground(new java.awt.Color(255, 255, 255));
        btnAlerts.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnAlerts.setForeground(new java.awt.Color(0, 0, 51));
        btnAlerts.setText("  Alerts");
        btnAlerts.setBorder(null);
        btnAlerts.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnAlerts.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        btnAlerts.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAlertsActionPerformed(evt);
            }
        });

        btnLogout.setBackground(new java.awt.Color(255, 255, 255));
        btnLogout.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        btnLogout.setForeground(new java.awt.Color(0, 0, 51));
        btnLogout.setText("  Logout");
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
                            .addComponent(btnRecordSales, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnAlerts, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnInventory, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(btnHome, javax.swing.GroupLayout.DEFAULT_SIZE, 110, Short.MAX_VALUE)
                            .addComponent(btnLogout, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        LeftPanelLayout.setVerticalGroup(
            LeftPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, LeftPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btnHome, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnInventory, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnRecordSales, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnAlerts, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(82, 82, 82)
                .addComponent(btnLogout, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(214, Short.MAX_VALUE))
        );

        UserParent.setPreferredSize(new java.awt.Dimension(0, 0));
        UserParent.setLayout(new java.awt.CardLayout());

        HomePage.setBackground(new java.awt.Color(0, 0, 51));

        TopPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jLabel10.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(0, 0, 51));
        jLabel10.setText("User Home Page");

        javax.swing.GroupLayout TopPanel1Layout = new javax.swing.GroupLayout(TopPanel1);
        TopPanel1.setLayout(TopPanel1Layout);
        TopPanel1Layout.setHorizontalGroup(
            TopPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(TopPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel10)
                .addContainerGap(686, Short.MAX_VALUE))
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
                .addGap(33, 33, 33)
                .addComponent(btnYesterday, javax.swing.GroupLayout.DEFAULT_SIZE, 140, Short.MAX_VALUE)
                .addGap(35, 35, 35)
                .addComponent(btnToday, javax.swing.GroupLayout.DEFAULT_SIZE, 138, Short.MAX_VALUE)
                .addGap(35, 35, 35)
                .addComponent(btnThisWeek, javax.swing.GroupLayout.DEFAULT_SIZE, 140, Short.MAX_VALUE)
                .addGap(35, 35, 35)
                .addComponent(btnThisMonth, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(35, 35, 35)
                .addComponent(btnThisYear, javax.swing.GroupLayout.DEFAULT_SIZE, 140, Short.MAX_VALUE)
                .addGap(35, 35, 35))
            .addGroup(TotalSalesPanelLayout.createSequentialGroup()
                .addGap(44, 44, 44)
                .addComponent(labelSoldItems)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(labelTotalSold)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(labelTotalSales)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(labelTotalAmount)
                .addGap(60, 60, 60))
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
            .addGroup(REDUNDANCY1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(GraphPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        REDUNDANCY1Layout.setVerticalGroup(
            REDUNDANCY1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(REDUNDANCY1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(GraphPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 422, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout HomePageLayout = new javax.swing.GroupLayout(HomePage);
        HomePage.setLayout(HomePageLayout);
        HomePageLayout.setHorizontalGroup(
            HomePageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(TopPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, HomePageLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(HomePageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
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

        UserParent.add(HomePage, "card2");

        ViewInventoryPage.setBackground(new java.awt.Color(0, 0, 51));

        TopPanel.setBackground(new java.awt.Color(255, 255, 255));

        jLabel9.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(0, 0, 51));
        jLabel9.setText("Inventory");

        javax.swing.GroupLayout TopPanelLayout = new javax.swing.GroupLayout(TopPanel);
        TopPanel.setLayout(TopPanelLayout);
        TopPanelLayout.setHorizontalGroup(
            TopPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(TopPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel9)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        TopPanelLayout.setVerticalGroup(
            TopPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(TopPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel9, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        cmbCategory.setBackground(new java.awt.Color(0, 51, 102));
        cmbCategory.setForeground(new java.awt.Color(255, 255, 255));
        cmbCategory.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "All", "Snacks", "Canned & Instant Foods", "Beverages", "Powdered Drinks", "Cooking Essentials", "Personal Care", "Laundry & Cleaning Supplies", "School & Office Supplies" }));
        cmbCategory.setToolTipText("Category");
        cmbCategory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbCategoryActionPerformed(evt);
            }
        });

        btnProductExpiration.setBackground(new java.awt.Color(0, 51, 102));
        btnProductExpiration.setForeground(new java.awt.Color(255, 255, 255));
        btnProductExpiration.setText("Product Expiration");
        btnProductExpiration.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnProductExpirationActionPerformed(evt);
            }
        });

        txtSearchProduct.setBackground(new java.awt.Color(204, 204, 204));
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
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "Product ID", "Product Name", "Category", "Stock Level", "Reorder Level", "Price (Pesos)"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.Integer.class, java.lang.Double.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        tableAllProducts.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        tableAllProducts.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        tableAllProducts.setRowHeight(30);
        tableAllProducts.setRowMargin(5);
        tableAllProducts.setShowHorizontalLines(true);
        scrollpAllProducts.setViewportView(tableAllProducts);

        TableParent.add(scrollpAllProducts, "card2");

        scrollpProductExpiration.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
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
        tableProductExpiration.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS);
        tableProductExpiration.setRowHeight(30);
        tableProductExpiration.setRowMargin(5);
        tableProductExpiration.setShowHorizontalLines(true);
        scrollpProductExpiration.setViewportView(tableProductExpiration);

        TableParent.add(scrollpProductExpiration, "card3");

        javax.swing.GroupLayout REDUNDANCY2Layout = new javax.swing.GroupLayout(REDUNDANCY2);
        REDUNDANCY2.setLayout(REDUNDANCY2Layout);
        REDUNDANCY2Layout.setHorizontalGroup(
            REDUNDANCY2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
            .addGroup(REDUNDANCY2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(REDUNDANCY2Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(TableParent, javax.swing.GroupLayout.DEFAULT_SIZE, 646, Short.MAX_VALUE)
                    .addContainerGap()))
        );
        REDUNDANCY2Layout.setVerticalGroup(
            REDUNDANCY2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 484, Short.MAX_VALUE)
            .addGroup(REDUNDANCY2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, REDUNDANCY2Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(TableParent, javax.swing.GroupLayout.DEFAULT_SIZE, 332, Short.MAX_VALUE)
                    .addContainerGap()))
        );

        javax.swing.GroupLayout ViewInventoryPageLayout = new javax.swing.GroupLayout(ViewInventoryPage);
        ViewInventoryPage.setLayout(ViewInventoryPageLayout);
        ViewInventoryPageLayout.setHorizontalGroup(
            ViewInventoryPageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(TopPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(ViewInventoryPageLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(ViewInventoryPageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(REDUNDANCY2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(ViewInventoryPageLayout.createSequentialGroup()
                        .addComponent(cmbCategory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnProductExpiration)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 259, Short.MAX_VALUE)
                        .addComponent(txtSearchProduct, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnSearchProduct)
                        .addGap(8, 8, 8)))
                .addContainerGap())
        );
        ViewInventoryPageLayout.setVerticalGroup(
            ViewInventoryPageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, ViewInventoryPageLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(TopPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(ViewInventoryPageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnSearchProduct, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(txtSearchProduct)
                    .addComponent(btnProductExpiration, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbCategory))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(REDUNDANCY2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        UserParent.add(ViewInventoryPage, "card3");

        RecordSalesPage.setBackground(new java.awt.Color(0, 0, 51));

        TopPanel2.setBackground(new java.awt.Color(255, 255, 255));

        jLabel11.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(0, 0, 51));
        jLabel11.setText("Sales");

        javax.swing.GroupLayout TopPanel2Layout = new javax.swing.GroupLayout(TopPanel2);
        TopPanel2.setLayout(TopPanel2Layout);
        TopPanel2Layout.setHorizontalGroup(
            TopPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(TopPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel11)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        TopPanel2Layout.setVerticalGroup(
            TopPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(TopPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel11, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        btnRecordSale.setBackground(new java.awt.Color(0, 51, 102));
        btnRecordSale.setText("Record");
        btnRecordSale.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRecordSaleActionPerformed(evt);
            }
        });

        btnRecordHistory.setBackground(new java.awt.Color(0, 51, 102));
        btnRecordHistory.setText("History");
        btnRecordHistory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRecordHistoryActionPerformed(evt);
            }
        });

        RParent.setLayout(new java.awt.CardLayout());

        RecordSale.setBackground(new java.awt.Color(255, 255, 255));

        jLabel2.setForeground(new java.awt.Color(0, 0, 0));
        jLabel2.setText("Search Product:");

        txtRSearchProduct.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtRSearchProductActionPerformed(evt);
            }
        });

        btnRSSearch.setBackground(new java.awt.Color(0, 51, 102));
        btnRSSearch.setForeground(new java.awt.Color(255, 255, 255));
        btnRSSearch.setText("Search");
        btnRSSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRSSearchActionPerformed(evt);
            }
        });

        jScrollPane3.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        jScrollPane3.setViewportView(listProductList);

        jLabel3.setForeground(new java.awt.Color(0, 0, 0));
        jLabel3.setText("Quantity:");

        txtQuantity.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtQuantityActionPerformed(evt);
            }
        });

        btnAdd.setBackground(new java.awt.Color(0, 51, 102));
        btnAdd.setForeground(new java.awt.Color(255, 255, 255));
        btnAdd.setText("Add");
        btnAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAddActionPerformed(evt);
            }
        });

        tableRecordSales.setBackground(new java.awt.Color(255, 255, 255));
        tableRecordSales.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Product Name", "Quantity", "Unit Price", "Sub Total"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        tableRecordSales.setRowHeight(30);
        tableRecordSales.setRowMargin(5);
        tableRecordSales.setShowHorizontalLines(true);
        jScrollPane1.setViewportView(tableRecordSales);

        btnRemove.setBackground(new java.awt.Color(0, 51, 102));
        btnRemove.setForeground(new java.awt.Color(255, 255, 255));
        btnRemove.setText("Remove");
        btnRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRemoveActionPerformed(evt);
            }
        });

        jLabel5.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(0, 0, 0));
        jLabel5.setText("Total:");

        labelTotalPrice.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        labelTotalPrice.setForeground(new java.awt.Color(0, 0, 0));
        labelTotalPrice.setText("₱0.00");

        btnClear.setBackground(new java.awt.Color(0, 51, 102));
        btnClear.setForeground(new java.awt.Color(255, 255, 255));
        btnClear.setText("Clear");
        btnClear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnClearActionPerformed(evt);
            }
        });

        btnRecord.setBackground(new java.awt.Color(0, 51, 102));
        btnRecord.setForeground(new java.awt.Color(255, 255, 255));
        btnRecord.setText("Record");
        btnRecord.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRecordActionPerformed(evt);
            }
        });

        labelSelectedProductInfo.setForeground(new java.awt.Color(0, 0, 0));
        labelSelectedProductInfo.setText(" ");
        labelSelectedProductInfo.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));

        jLabel4.setForeground(new java.awt.Color(0, 0, 0));
        jLabel4.setText("Selected:");

        javax.swing.GroupLayout RecordSaleLayout = new javax.swing.GroupLayout(RecordSale);
        RecordSale.setLayout(RecordSaleLayout);
        RecordSaleLayout.setHorizontalGroup(
            RecordSaleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(RecordSaleLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(RecordSaleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(RecordSaleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jLabel3)
                        .addComponent(jLabel2)
                        .addGroup(RecordSaleLayout.createSequentialGroup()
                            .addComponent(txtRSearchProduct, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(btnRSSearch))
                        .addGroup(RecordSaleLayout.createSequentialGroup()
                            .addComponent(txtQuantity, javax.swing.GroupLayout.PREFERRED_SIZE, 175, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(btnAdd))
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                        .addComponent(labelSelectedProductInfo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(RecordSaleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(RecordSaleLayout.createSequentialGroup()
                        .addComponent(btnRemove)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(RecordSaleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(RecordSaleLayout.createSequentialGroup()
                                .addGap(13, 13, 13)
                                .addComponent(jLabel5)
                                .addGap(18, 18, 18)
                                .addComponent(labelTotalPrice)
                                .addGap(3, 3, 3))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, RecordSaleLayout.createSequentialGroup()
                                .addComponent(btnClear)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(btnRecord))))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 575, Short.MAX_VALUE))
                .addContainerGap())
        );
        RecordSaleLayout.setVerticalGroup(
            RecordSaleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, RecordSaleLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(RecordSaleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(RecordSaleLayout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(RecordSaleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtRSearchProduct, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnRSSearch))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane3))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 337, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(RecordSaleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(RecordSaleLayout.createSequentialGroup()
                        .addGroup(RecordSaleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(labelTotalPrice)
                            .addComponent(jLabel5))
                        .addGap(25, 25, 25)
                        .addGroup(RecordSaleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnRecord)
                            .addComponent(btnClear))
                        .addGap(36, 36, 36))
                    .addGroup(RecordSaleLayout.createSequentialGroup()
                        .addGroup(RecordSaleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(btnRemove)
                            .addComponent(jLabel4))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(labelSelectedProductInfo)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(RecordSaleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtQuantity, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnAdd))
                        .addGap(26, 26, 26))))
        );

        RParent.add(RecordSale, "card6");

        RecordHistory.setBackground(new java.awt.Color(255, 255, 255));

        btnRefresh.setBackground(new java.awt.Color(0, 51, 102));
        btnRefresh.setText("Refresh");
        btnRefresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRefreshActionPerformed(evt);
            }
        });

        cmbRCategory.setBackground(new java.awt.Color(0, 51, 102));
        cmbRCategory.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "All", "Snacks", "Canned & Instant Foods", "Beverages", "Powdered Drinks", "Cooking Essentials", "Personal Care", "Laundry & Cleaning Supplies", "School & Office Supplies" }));
        cmbRCategory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cmbRCategoryActionPerformed(evt);
            }
        });

        txtRHSearch.setBackground(new java.awt.Color(204, 255, 255));
        txtRHSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtRHSearchActionPerformed(evt);
            }
        });

        btnRHSearch.setBackground(new java.awt.Color(0, 51, 102));
        btnRHSearch.setText("Search");
        btnRHSearch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRHSearchActionPerformed(evt);
            }
        });

        tableHistory.setBackground(new java.awt.Color(255, 255, 255));
        tableHistory.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "Sales ID", "Product Name", "Category", "Quantity Sold", "Amount", "Date & Time"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.Double.class, java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        tableHistory.setRowHeight(30);
        tableHistory.setRowMargin(5);
        tableHistory.setShowHorizontalLines(true);
        jScrollPane2.setViewportView(tableHistory);

        javax.swing.GroupLayout RecordHistoryLayout = new javax.swing.GroupLayout(RecordHistory);
        RecordHistory.setLayout(RecordHistoryLayout);
        RecordHistoryLayout.setHorizontalGroup(
            RecordHistoryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(RecordHistoryLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(RecordHistoryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 834, Short.MAX_VALUE)
                    .addGroup(RecordHistoryLayout.createSequentialGroup()
                        .addComponent(btnRefresh, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(cmbRCategory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(txtRHSearch, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnRHSearch)))
                .addContainerGap())
        );
        RecordHistoryLayout.setVerticalGroup(
            RecordHistoryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(RecordHistoryLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(RecordHistoryLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnRefresh, javax.swing.GroupLayout.DEFAULT_SIZE, 40, Short.MAX_VALUE)
                    .addComponent(cmbRCategory)
                    .addComponent(txtRHSearch)
                    .addComponent(btnRHSearch, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 409, Short.MAX_VALUE)
                .addContainerGap())
        );

        RParent.add(RecordHistory, "card3");

        javax.swing.GroupLayout REDUNDANCY3Layout = new javax.swing.GroupLayout(REDUNDANCY3);
        REDUNDANCY3.setLayout(REDUNDANCY3Layout);
        REDUNDANCY3Layout.setHorizontalGroup(
            REDUNDANCY3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
            .addGroup(REDUNDANCY3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(REDUNDANCY3Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(RParent, javax.swing.GroupLayout.DEFAULT_SIZE, 852, Short.MAX_VALUE)
                    .addContainerGap()))
        );
        REDUNDANCY3Layout.setVerticalGroup(
            REDUNDANCY3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 485, Short.MAX_VALUE)
            .addGroup(REDUNDANCY3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(REDUNDANCY3Layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(RParent, javax.swing.GroupLayout.DEFAULT_SIZE, 333, Short.MAX_VALUE)
                    .addContainerGap()))
        );

        javax.swing.GroupLayout RecordSalesPageLayout = new javax.swing.GroupLayout(RecordSalesPage);
        RecordSalesPage.setLayout(RecordSalesPageLayout);
        RecordSalesPageLayout.setHorizontalGroup(
            RecordSalesPageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(TopPanel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(RecordSalesPageLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(RecordSalesPageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(REDUNDANCY3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(RecordSalesPageLayout.createSequentialGroup()
                        .addComponent(btnRecordSale, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(btnRecordHistory, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        RecordSalesPageLayout.setVerticalGroup(
            RecordSalesPageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(RecordSalesPageLayout.createSequentialGroup()
                .addComponent(TopPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(RecordSalesPageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnRecordSale, javax.swing.GroupLayout.DEFAULT_SIZE, 35, Short.MAX_VALUE)
                    .addComponent(btnRecordHistory, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(REDUNDANCY3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        UserParent.add(RecordSalesPage, "card5");

        AlertsPage.setBackground(new java.awt.Color(0, 0, 51));

        TopPanel3.setBackground(new java.awt.Color(255, 255, 255));

        jLabel12.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        jLabel12.setForeground(new java.awt.Color(0, 0, 51));
        jLabel12.setText("Alerts");

        javax.swing.GroupLayout TopPanel3Layout = new javax.swing.GroupLayout(TopPanel3);
        TopPanel3.setLayout(TopPanel3Layout);
        TopPanel3Layout.setHorizontalGroup(
            TopPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(TopPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel12)
                .addContainerGap(804, Short.MAX_VALUE))
        );
        TopPanel3Layout.setVerticalGroup(
            TopPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(TopPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel12, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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

        javax.swing.GroupLayout REDUNDANCY4Layout = new javax.swing.GroupLayout(REDUNDANCY4);
        REDUNDANCY4.setLayout(REDUNDANCY4Layout);
        REDUNDANCY4Layout.setHorizontalGroup(
            REDUNDANCY4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(REDUNDANCY4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        REDUNDANCY4Layout.setVerticalGroup(
            REDUNDANCY4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(REDUNDANCY4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout AlertsPageLayout = new javax.swing.GroupLayout(AlertsPage);
        AlertsPage.setLayout(AlertsPageLayout);
        AlertsPageLayout.setHorizontalGroup(
            AlertsPageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(TopPanel3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(AlertsPageLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(AlertsPageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(REDUNDANCY4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(MidPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        AlertsPageLayout.setVerticalGroup(
            AlertsPageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(AlertsPageLayout.createSequentialGroup()
                .addComponent(TopPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(MidPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(REDUNDANCY4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        UserParent.add(AlertsPage, "card4");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(LeftPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 130, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(UserParent, javax.swing.GroupLayout.DEFAULT_SIZE, 870, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(LeftPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(UserParent, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
  
    
    
    
    
//SIDEBAR MENU
    private void btnHomeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHomeActionPerformed
        
        
        UserParent.removeAll();
        UserParent.add(HomePage);
        UserParent.repaint();
        UserParent.revalidate();
    }//GEN-LAST:event_btnHomeActionPerformed

    private void btnInventoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnInventoryActionPerformed
        refreshCategoryComboBoxes();
        
        // Fix: Add 'Description' column next to 'Product Name'
        String[] columnNames = {"Product ID", "Product Name", "Description", "Category", "Stock Level", "Reorder Level", "Price (Pesos)"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        tableAllProducts.setModel(model);
        
        loadData();
        
        UserParent.removeAll();
        UserParent.add(ViewInventoryPage);
        UserParent.repaint();
        UserParent.revalidate();
    }//GEN-LAST:event_btnInventoryActionPerformed

    private void btnAlertsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAlertsActionPerformed
        checkForNewNotifications(); // Check for new notifications first
        loadNotifications(); // Then load all notifications
        
        UserParent.removeAll();
        UserParent.add(AlertsPage);
        UserParent.repaint();
        UserParent.revalidate();
    }//GEN-LAST:event_btnAlertsActionPerformed

    private void btnRecordSalesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRecordSalesActionPerformed
        refreshCategoryComboBoxes();
        
        // Fix: Add 'Description' column next to 'Product Name'
        String[] columnNames = {"Product Name", "Description", "Quantity", "Unit Price", "Sub Total"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        tableRecordSales.setModel(model);

        isLoadingProducts = false;

        // Refresh the product list when switching to Record Sales page
        initializeProductList();

        UserParent.removeAll();
        UserParent.add(RecordSalesPage);
        UserParent.repaint();
        UserParent.revalidate();
    }//GEN-LAST:event_btnRecordSalesActionPerformed

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
                    "₱ " + rs.getString("price")
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
    
//RECORD SALES PAGE EVENTS   
    private void btnRecordSaleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRecordSaleActionPerformed
        RParent.removeAll();
        RParent.add(RecordSale);
        RParent.repaint();
        RParent.revalidate();
    }//GEN-LAST:event_btnRecordSaleActionPerformed

    private void btnRecordHistoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRecordHistoryActionPerformed
        // Fix: Add 'Description' column next to 'Product Name'
        String[] columnNames = {"Sales ID", "Product Name", "Description", "Category", "Quantity Sold", "Amount", "Date & Time"};
        DefaultTableModel model1 = new DefaultTableModel(columnNames, 0);
        tableHistory.setModel(model1);

        loadRecordHistory();

        RParent.removeAll();
        RParent.add(RecordHistory);
        RParent.repaint();
        RParent.revalidate();
    }//GEN-LAST:event_btnRecordHistoryActionPerformed

    private void btnRHSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRHSearchActionPerformed
        String keyword = txtRHSearch.getText().trim();
        searchHistorySales(keyword);
    }//GEN-LAST:event_btnRHSearchActionPerformed

    private void txtRHSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtRHSearchActionPerformed
        String keyword = txtRHSearch.getText().trim();
        searchHistorySales(keyword);
    }//GEN-LAST:event_txtRHSearchActionPerformed

    private void cmbRCategoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbRCategoryActionPerformed
        String selectedCategory = (String) cmbRCategory.getSelectedItem();

        String baseQuery = "SELECT * FROM salerecords";
        if (!"All".equalsIgnoreCase(selectedCategory)) {
            baseQuery += " WHERE category = '" + selectedCategory + "'";
        }
        baseQuery += " ORDER BY sale_datetime DESC";

        // Clear the table first
        DefaultTableModel model = (DefaultTableModel) tableHistory.getModel();
        model.setRowCount(0);

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/storvendb", "root", "");
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(baseQuery)) {

            while (rs.next()) {
                Timestamp timestamp = rs.getTimestamp("sale_datetime");
                String formattedDateTime = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss").format(timestamp);

                model.addRow(new Object[]{
                    rs.getInt("sales_id"),
                    rs.getString("product_name"),
                    rs.getString("description"),
                    rs.getString("category"),
                    rs.getInt("quantity_sold"),
                    "₱ " + rs.getString("sale_amount"),
                    formattedDateTime
                });
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error loading sales history: " + e.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_cmbRCategoryActionPerformed

    private void btnRefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRefreshActionPerformed
        refreshTableHistory();
    }//GEN-LAST:event_btnRefreshActionPerformed

    private void btnRecordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRecordActionPerformed
        if (tableRecordSales.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No sales to record.");
            return;
        }

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/storvendb", "root", "")) {
            conn.setAutoCommit(false); // Start transaction

            // Get current timestamp
            java.sql.Timestamp saleTimestamp = new java.sql.Timestamp(System.currentTimeMillis());

            for (int i = 0; i < tableRecordSales.getRowCount(); i++) {
                String productName = tableRecordSales.getValueAt(i, 0).toString();
                String description = tableRecordSales.getValueAt(i, 1).toString();
                int quantity = Integer.parseInt(tableRecordSales.getValueAt(i, 2).toString());
                double unitPrice = Double.parseDouble(tableRecordSales.getValueAt(i, 3).toString());
                double subtotal = Double.parseDouble(tableRecordSales.getValueAt(i, 4).toString());

                // Get product_id and category from inventory
                int productId = -1;
                String category = "";
                String getIdSql = "SELECT product_id, category FROM inventory WHERE product_name = ?";
                try (PreparedStatement getIdStmt = conn.prepareStatement(getIdSql)) {
                    getIdStmt.setString(1, productName);
                    ResultSet rs = getIdStmt.executeQuery();
                    if (rs.next()) {
                        productId = rs.getInt("product_id");
                        category = rs.getString("category");
                    }
                }

                // Insert into salerecords with timestamp
                String insertSql = "INSERT INTO salerecords (product_id, product_name, category, quantity_sold, sale_amount, sale_datetime, description) VALUES (?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
                    pstmt.setInt(1, productId);
                    pstmt.setString(2, productName);
                    pstmt.setString(3, category);
                    pstmt.setInt(4, quantity);
                    pstmt.setDouble(5, subtotal);
                    pstmt.setTimestamp(6, saleTimestamp);
                    pstmt.setString(7, description);
                    pstmt.executeUpdate();
                }

                // Update inventory
                String updateSql = "UPDATE inventory SET stock_level = stock_level - ? WHERE product_name = ?";
                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setInt(1, quantity);
                    updateStmt.setString(2, productName);
                    updateStmt.executeUpdate();
                }
            }

            conn.commit();
            JOptionPane.showMessageDialog(this, "Sales recorded successfully with timestamp: "
                + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(saleTimestamp));

            // Clear and refresh
            ((DefaultTableModel)tableRecordSales.getModel()).setRowCount(0);
            updateTotal();
            
        } catch (SQLException e) {
            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/storvendb", "root", "")) {
                conn.rollback();
            } catch (SQLException ex) {
            }
            JOptionPane.showMessageDialog(this, "Error recording sales: " + e.getMessage());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid number format: " + e.getMessage());
        }
    }//GEN-LAST:event_btnRecordActionPerformed

    private void btnClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearActionPerformed
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to clear table data?", "Clear", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            DefaultTableModel model = (DefaultTableModel) tableRecordSales.getModel();
            model.setRowCount(0); // Clear table
            updateTotal();

        } else {

        }
    }//GEN-LAST:event_btnClearActionPerformed

    private void btnRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRemoveActionPerformed
        int selectedRow = tableRecordSales.getSelectedRow();
        if (selectedRow != -1) {
            DefaultTableModel model = (DefaultTableModel) tableRecordSales.getModel();
            model.removeRow(selectedRow);
            updateTotal();
        } else {
            JOptionPane.showMessageDialog(this, "Please select a row to remove.");
        }
    }//GEN-LAST:event_btnRemoveActionPerformed

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddActionPerformed
        try {
            addProducts();
        } catch (SQLException ex) {
            Logger.getLogger(UserView.class.getName()).log(Level.SEVERE, null, ex);
        }
        txtQuantity.requestFocusInWindow();
    }//GEN-LAST:event_btnAddActionPerformed

    private void txtQuantityActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtQuantityActionPerformed
        try {
            addProducts();
        } catch (SQLException ex) {
            Logger.getLogger(UserView.class.getName()).log(Level.SEVERE, null, ex);
        }
        txtQuantity.requestFocusInWindow();
    }//GEN-LAST:event_txtQuantityActionPerformed

    private void btnRSSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnRSSearchActionPerformed
        String keyword = txtRSearchProduct.getText().trim();
        searchProducts(keyword);
    }//GEN-LAST:event_btnRSSearchActionPerformed

    private void txtRSearchProductActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtRSearchProductActionPerformed
        String keyword = txtRSearchProduct.getText().trim();
        searchProducts(keyword);
    }//GEN-LAST:event_txtRSearchProductActionPerformed
//END RECORD SALES PAGE EVENTS
    
    
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
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(UserView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new UserView().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel AlertsPage;
    private javax.swing.JPanel GraphPanel;
    private javax.swing.JPanel HomePage;
    private javax.swing.JPanel LeftPanel;
    private javax.swing.JPanel MidPanel;
    private javax.swing.JPanel REDUNDANCY1;
    private javax.swing.JPanel REDUNDANCY2;
    private javax.swing.JPanel REDUNDANCY3;
    private javax.swing.JPanel REDUNDANCY4;
    private javax.swing.JPanel RParent;
    private javax.swing.JPanel RecordHistory;
    private javax.swing.JPanel RecordSale;
    private javax.swing.JPanel RecordSalesPage;
    private javax.swing.JPanel TableParent;
    private javax.swing.JPanel TopPanel;
    private javax.swing.JPanel TopPanel1;
    private javax.swing.JPanel TopPanel2;
    private javax.swing.JPanel TopPanel3;
    private javax.swing.JPanel TotalSalesPanel;
    private javax.swing.JPanel UserParent;
    private javax.swing.JPanel ViewInventoryPage;
    private javax.swing.JButton btnAdd;
    private javax.swing.JToggleButton btnAlerts;
    private javax.swing.JButton btnClear;
    private javax.swing.JToggleButton btnHome;
    private javax.swing.JToggleButton btnInventory;
    private javax.swing.JToggleButton btnLogout;
    private javax.swing.JToggleButton btnProductExpiration;
    private javax.swing.JButton btnRHSearch;
    private javax.swing.JButton btnRSSearch;
    private javax.swing.JButton btnRecord;
    private javax.swing.JButton btnRecordHistory;
    private javax.swing.JButton btnRecordSale;
    private javax.swing.JToggleButton btnRecordSales;
    private javax.swing.JButton btnRefresh;
    private javax.swing.JButton btnRemove;
    private javax.swing.JButton btnSearchProduct;
    private javax.swing.JToggleButton btnThisMonth;
    private javax.swing.JToggleButton btnThisWeek;
    private javax.swing.JToggleButton btnThisYear;
    private javax.swing.JToggleButton btnToday;
    private javax.swing.JToggleButton btnYesterday;
    private javax.swing.JComboBox<String> cmbCategory;
    private javax.swing.JComboBox<String> cmbRCategory;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JLabel labelSelectedProductInfo;
    private javax.swing.JLabel labelSoldItems;
    private javax.swing.JLabel labelTotalAmount;
    private javax.swing.JLabel labelTotalPrice;
    private javax.swing.JLabel labelTotalSales;
    private javax.swing.JLabel labelTotalSold;
    private javax.swing.JList<String> listProductList;
    private javax.swing.JScrollPane scrollpAllProducts;
    private javax.swing.JScrollPane scrollpProductExpiration;
    private javax.swing.JTable tableAllProducts;
    private javax.swing.JTable tableHistory;
    private javax.swing.JTable tableProductExpiration;
    private javax.swing.JTable tableRecordSales;
    private javax.swing.JTextField txtQuantity;
    private javax.swing.JTextField txtRHSearch;
    private javax.swing.JTextField txtRSearchProduct;
    private javax.swing.JTextField txtSearchProduct;
    // End of variables declaration//GEN-END:variables
    }