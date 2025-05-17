package src;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class ProductSelectionDialog extends JDialog {
    private JTable productTable;
    private JTextField searchField;
    private String selectedProduct;
    private String selectedCategory;
    private String selectedPrice;
    private int selectedQuantity;
    private String selectedDescription;
    private InventoryAddProductNew parentForm;

    public ProductSelectionDialog(InventoryAddProductNew parent) {
        super(parent, "Select Purchased Product", true);
        this.parentForm = parent;
        initComponents();
        loadPurchasedProducts();
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        setSize(600, 400);
        setLayout(new BorderLayout(10, 10));

        // Search Panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchField = new JTextField(20);
        JButton searchButton = new JButton("Search");
        searchPanel.add(new JLabel("Search: "));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        // Table
        String[] columns = {"Product Name", "Category", "Price", "Quantity", "Description", "Purchase Date"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        productTable = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(productTable);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton selectButton = new JButton("Select");
        JButton cancelButton = new JButton("Cancel");
        buttonPanel.add(selectButton);
        buttonPanel.add(cancelButton);

        // Add components
        add(searchPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Add action listeners
        searchButton.addActionListener(e -> searchProducts());
        searchField.addActionListener(e -> searchProducts());
        
        selectButton.addActionListener(e -> {
            int selectedRow = productTable.getSelectedRow();
            if (selectedRow != -1) {
                selectedProduct = (String) productTable.getValueAt(selectedRow, 0);
                selectedCategory = (String) productTable.getValueAt(selectedRow, 1);
                selectedPrice = (String) productTable.getValueAt(selectedRow, 2);
                selectedQuantity = (int) productTable.getValueAt(selectedRow, 3);
                selectedDescription = (String) productTable.getValueAt(selectedRow, 4);
                parentForm.setProductDetails(selectedProduct, selectedCategory, selectedPrice, selectedQuantity, selectedDescription);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, 
                    "Please select a product first", 
                    "No Selection", 
                    JOptionPane.WARNING_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dispose());
    }

    private void loadPurchasedProducts() {
        DefaultTableModel model = (DefaultTableModel) productTable.getModel();
        model.setRowCount(0);

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/storvendb", "root", "");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT DISTINCT product_name, category, unit_price, quantity, description, purchase_date " +
                 "FROM purchases ORDER BY purchase_date DESC")) {

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("product_name"),
                    rs.getString("category"),
                    "₱" + String.format("%.2f", rs.getDouble("unit_price")),
                    rs.getInt("quantity"),
                    rs.getString("description"),
                    rs.getTimestamp("purchase_date")
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error loading products: " + e.getMessage(), 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void searchProducts() {
        String searchTerm = searchField.getText().trim().toLowerCase();
        DefaultTableModel model = (DefaultTableModel) productTable.getModel();
        model.setRowCount(0);

        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/storvendb", "root", "");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT DISTINCT product_name, category, unit_price, quantity, description, purchase_date " +
                 "FROM purchases " +
                 "WHERE LOWER(product_name) LIKE '%" + searchTerm + "%' " +
                 "OR LOWER(category) LIKE '%" + searchTerm + "%' " +
                 "OR LOWER(description) LIKE '%" + searchTerm + "%' " +
                 "ORDER BY purchase_date DESC")) {

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("product_name"),
                    rs.getString("category"),
                    "₱" + String.format("%.2f", rs.getDouble("unit_price")),
                    rs.getInt("quantity"),
                    rs.getString("description"),
                    rs.getTimestamp("purchase_date")
                });
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error searching products: " + e.getMessage(), 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
} 