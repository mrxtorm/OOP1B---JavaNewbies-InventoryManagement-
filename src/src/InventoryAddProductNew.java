package src;

import java.awt.Component;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JButton;



public class InventoryAddProductNew extends javax.swing.JFrame {
    
    private final AdminView adminview;
    private javax.swing.JButton btnAdd;
    private javax.swing.JButton btnCancel;
    private javax.swing.JComboBox<String> cmbCategory;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTextField txtExpirationDate;
    private javax.swing.JTextField txtPrice;
    private javax.swing.JTextField txtProductName;
    private javax.swing.JTextField txtQuantity;
    private javax.swing.JTextField txtReorderLevel;
    private javax.swing.JLabel jLabelDescription;
    private javax.swing.JTextField txtDescription;

    public InventoryAddProductNew(AdminView adminview) {
        this.adminview = adminview;
        initComponents();
        setLocationRelativeTo(null);
        loadCategories();
    }
    
    private void loadCategories() {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/storvendb", "root", "");
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT category_name FROM categories ORDER BY category_name")) {
            
            ArrayList<String> categories = new ArrayList<>();
            while (rs.next()) {
                categories.add(rs.getString("category_name"));
            }
            
            cmbCategory.setModel(new javax.swing.DefaultComboBoxModel<>(categories.toArray(new String[0])));
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error loading categories: " + e.getMessage(), 
                "Database Error", 
                JOptionPane.ERROR_MESSAGE);
            
            cmbCategory.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { 
                "Snacks", "Canned & Instant Foods", "Beverages", 
                "Powdered Drinks", "Cooking Essentials", "Personal Care", 
                "Laundry & Cleaning Supplies", "School & Office Supplies" 
            }));
        }
    }

    private void initComponents() {
        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        txtProductName = new javax.swing.JTextField();
        cmbCategory = new javax.swing.JComboBox<>();
        txtQuantity = new javax.swing.JTextField();
        txtReorderLevel = new javax.swing.JTextField();
        txtPrice = new javax.swing.JTextField();
        txtExpirationDate = new javax.swing.JTextField();
        txtExpirationDate.setText("yyyy-mm-dd");
        txtExpirationDate.setForeground(java.awt.Color.GRAY);
        
        txtExpirationDate.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                if (txtExpirationDate.getText().equals("yyyy-mm-dd")) {
                    txtExpirationDate.setText("");
                    txtExpirationDate.setForeground(java.awt.Color.BLACK);
                }
            }
            
            @Override
            public void focusLost(java.awt.event.FocusEvent evt) {
                if (txtExpirationDate.getText().isEmpty()) {
                    txtExpirationDate.setText("yyyy-mm-dd");
                    txtExpirationDate.setForeground(java.awt.Color.GRAY);
                }
            }
        });
        btnCancel = new javax.swing.JButton();
        btnAdd = new javax.swing.JButton();
        jLabelDescription = new javax.swing.JLabel();
        txtDescription = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Add New Product");
        setResizable(false);

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));

        jLabel2.setText("Product Name");
        jLabel3.setText("Category");
        jLabel4.setText("Quantity");
        jLabel5.setText("Reorder Level");
        jLabel6.setText("Price");
        jLabel7.setText("Expiration Date");
        jLabelDescription.setText("Description");

        btnCancel.setText("Cancel");
        btnCancel.addActionListener(evt -> btnCancelActionPerformed(evt));

        btnAdd.setText("Add Product");
        btnAdd.addActionListener(evt -> btnAddActionPerformed(evt));

        JButton btnSelectProduct = new JButton("Select Product");
        btnSelectProduct.addActionListener(evt -> {
            ProductSelectionDialog dialog = new ProductSelectionDialog(this);
            dialog.setVisible(true);
        });

        // Layout
        GroupLayout jPanel1Layout = new GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(25)
                .addGroup(jPanel1Layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addComponent(jLabelDescription)
                    .addComponent(jLabel3)
                    .addComponent(jLabel4))
                .addGap(28)
                .addGroup(jPanel1Layout.createParallelGroup(Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(txtProductName, GroupLayout.PREFERRED_SIZE, 200, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(btnSelectProduct))
                    .addComponent(txtDescription, GroupLayout.PREFERRED_SIZE, 200, GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbCategory, GroupLayout.PREFERRED_SIZE, 200, GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtQuantity, GroupLayout.PREFERRED_SIZE, 200, GroupLayout.PREFERRED_SIZE))
                .addGap(27)
                .addGroup(jPanel1Layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(jLabel5)
                    .addComponent(jLabel6)
                    .addComponent(jLabel7))
                .addGap(23)
                .addGroup(jPanel1Layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(txtReorderLevel, GroupLayout.PREFERRED_SIZE, 200, GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtPrice, GroupLayout.PREFERRED_SIZE, 200, GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtExpirationDate, GroupLayout.PREFERRED_SIZE, 200, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(33, Short.MAX_VALUE))
            .addGroup(Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnCancel, GroupLayout.PREFERRED_SIZE, 120, GroupLayout.PREFERRED_SIZE)
                .addGap(18)
                .addComponent(btnAdd, GroupLayout.PREFERRED_SIZE, 120, GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(25)
                .addGroup(jPanel1Layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(txtProductName, GroupLayout.PREFERRED_SIZE, 35, GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSelectProduct)
                    .addComponent(jLabel5)
                    .addComponent(txtReorderLevel, GroupLayout.PREFERRED_SIZE, 35, GroupLayout.PREFERRED_SIZE))
                .addGap(18)
                .addGroup(jPanel1Layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(jLabelDescription)
                    .addComponent(txtDescription, GroupLayout.PREFERRED_SIZE, 35, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6)
                    .addComponent(txtPrice, GroupLayout.PREFERRED_SIZE, 35, GroupLayout.PREFERRED_SIZE))
                .addGap(18)
                .addGroup(jPanel1Layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(cmbCategory, GroupLayout.PREFERRED_SIZE, 35, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7)
                    .addComponent(txtExpirationDate, GroupLayout.PREFERRED_SIZE, 35, GroupLayout.PREFERRED_SIZE))
                .addGap(18)
                .addGroup(jPanel1Layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(txtQuantity, GroupLayout.PREFERRED_SIZE, 35, GroupLayout.PREFERRED_SIZE))
                .addGap(30)
                .addGroup(jPanel1Layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(btnAdd, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnCancel, GroupLayout.PREFERRED_SIZE, 40, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(25, Short.MAX_VALUE))
        );

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addComponent(jPanel1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addComponent(jPanel1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }

    private void btnCancelActionPerformed(java.awt.event.ActionEvent evt) {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to cancel?", "Cancel", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
        }
    }

    private void btnAddActionPerformed(java.awt.event.ActionEvent evt) {
        try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/storvendb", "root", "")) {
            // First check if product exists in purchases
            String checkPurchaseSql = "SELECT * FROM purchases WHERE product_name = ? AND category = ?";
            PreparedStatement checkPurchaseStmt = conn.prepareStatement(checkPurchaseSql);
            String category = cmbCategory.getSelectedItem().toString();
            
            checkPurchaseStmt.setString(1, txtProductName.getText());
            checkPurchaseStmt.setString(2, category);
            
            ResultSet purchaseRs = checkPurchaseStmt.executeQuery();
            
            if (!purchaseRs.next()) {
                JOptionPane.showMessageDialog(this, 
                    "This product has not been purchased yet. Please purchase the product first before adding to inventory.",
                    "Purchase Required",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Check if product already exists in inventory
            String checkInventorySql = "SELECT * FROM inventory WHERE product_name = ? AND category = ?";
            PreparedStatement checkInventoryStmt = conn.prepareStatement(checkInventorySql);
            checkInventoryStmt.setString(1, txtProductName.getText());
            checkInventoryStmt.setString(2, category);
            
            ResultSet inventoryRs = checkInventoryStmt.executeQuery();
            
            if (inventoryRs.next()) {
                JOptionPane.showMessageDialog(this, 
                    "This product is already in the inventory. Please update the existing product instead.",
                    "Duplicate Product",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }

            // If product exists in purchases and not in inventory, proceed with adding to inventory
            String sql = "INSERT INTO inventory (product_name, category, stock_level, reorder_level, price, expiration_date, description) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);

            stmt.setString(1, txtProductName.getText());
            stmt.setString(2, category);
            stmt.setInt(3, Integer.parseInt(txtQuantity.getText()));
            stmt.setInt(4, Integer.parseInt(txtReorderLevel.getText()));
            stmt.setString(5, txtPrice.getText());

            if (category.equalsIgnoreCase("School & Office Supplies") ||
                category.equalsIgnoreCase("Personal Care") ||
                category.equalsIgnoreCase("Laundry & Cleaning Supplies")) {
                stmt.setNull(6, java.sql.Types.DATE);
            } else {
                stmt.setString(6, txtExpirationDate.getText());
            }

            stmt.setString(7, txtDescription.getText());

            stmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "Product added to inventory successfully!");
            adminview.loadData();
            dispose();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void setProductDetails(String productName, String category, String price, int quantity, String description) {
        txtProductName.setText(productName);
        cmbCategory.setSelectedItem(category);
        txtPrice.setText(price.replace("₱", "").trim());
        txtQuantity.setText(String.valueOf(quantity));
        txtDescription.setText(description);
    }
} 