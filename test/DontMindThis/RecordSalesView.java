package DontMindThis;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;

public class RecordSalesView extends JFrame {
    
    private JPanel panel;
    private JLabel lblProduct, lblQuantity, lblMessage;
    private JComboBox<String> comboProduct;
    private JTextField txtQuantity;
    private JButton btnAdd, btnSubmit;
    private JTable salesTable;
    private DefaultTableModel tableModel;
    
    public RecordSalesView() {
        setTitle("Record Sales");
        setSize(600, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        panel = new JPanel();
        panel.setLayout(new BorderLayout(10, 10));
        
        JPanel topPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        
        lblProduct = new JLabel("Select Product:");
        lblQuantity = new JLabel("Quantity:");
        lblMessage = new JLabel();
        lblMessage.setForeground(Color.RED);
        
        // Example product list, can be populated dynamically from the database
        comboProduct = new JComboBox<>(new String[]{"Product 1", "Product 2", "Product 3"});
        txtQuantity = new JTextField();
        
        btnAdd = new JButton("Add Product");
        btnSubmit = new JButton("Submit Sale");
        
        topPanel.add(lblProduct);
        topPanel.add(comboProduct);
        topPanel.add(lblQuantity);
        topPanel.add(txtQuantity);
        
        panel.add(topPanel, BorderLayout.NORTH);
        
        // Table to show added products and their quantities
        tableModel = new DefaultTableModel(new String[]{"Product", "Quantity", "Total"}, 0);
        salesTable = new JTable(tableModel);
        JScrollPane tableScroll = new JScrollPane(salesTable);
        
        panel.add(tableScroll, BorderLayout.CENTER);
        
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BorderLayout());
        bottomPanel.add(btnAdd, BorderLayout.WEST);
        bottomPanel.add(btnSubmit, BorderLayout.EAST);
        
        panel.add(bottomPanel, BorderLayout.SOUTH);
        panel.add(lblMessage, BorderLayout.PAGE_END);
        
        add(panel);
        
        // Add product to table when 'Add Product' is clicked
        btnAdd.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String product = (String) comboProduct.getSelectedItem();
                String quantityText = txtQuantity.getText();
                
                if (quantityText.isEmpty()) {
                    lblMessage.setText("Please enter a quantity.");
                    return;
                }
                
                int quantity = Integer.parseInt(quantityText);
                double price = getProductPrice(product); // Assume you have a method to get price
                double total = price * quantity;
                
                // Add the product and quantity to the table
                tableModel.addRow(new Object[]{product, quantity, total});
                
                // Clear quantity field for next entry
                txtQuantity.setText("");
                lblMessage.setText("");
            }
        });
        
        // Submit all sales
        btnSubmit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Here you can implement the logic for recording all the sales in the database
                lblMessage.setText("Sales Recorded Successfully!");
                tableModel.setRowCount(0); // Clear the table after submission
            }
        });
    }
    
    private double getProductPrice(String product) {
        // You can replace this with a method that fetches the price of the product from the database
        switch (product) {
            case "Product 1": return 10.0;
            case "Product 2": return 15.0;
            case "Product 3": return 20.0;
            default: return 0.0;
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new RecordSalesView().setVisible(true);
            }
        });
    }
}
