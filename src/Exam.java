import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class Exam extends JFrame {
    private JComboBox<String> productNameComboBox;
    private JTextField priceField;
    private JTextField quantityField;
    private JTable cartTable;
    private JButton addToCartButton;
    private JButton deleteButton;

    private DefaultTableModel tableModel;
    private Map<String, Integer> productPrices;

    public Exam() {
        setTitle("Form Transaction");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(5, 5));

        // Panel Pilihan Product dengan GridLayout
        JPanel topPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        productNameComboBox = new JComboBox<>(new String[]{"Vit", "Aqua", "Prima"});
        productNameComboBox.addActionListener(e -> updatePriceField());
        priceField = new JTextField();
        priceField.setEditable(false);
        quantityField = new JTextField();

        topPanel.add(new JLabel("Product name"));
        topPanel.add(productNameComboBox);
        topPanel.add(new JLabel("Price"));
        topPanel.add(priceField);
        topPanel.add(new JLabel("Quantity"));
        topPanel.add(quantityField);

        // Panel Button dengan FlowLayout
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        addToCartButton = new JButton("Add to Cart");
        deleteButton = new JButton("Delete");

        addToCartButton.addActionListener(e -> addToCart());
        deleteButton.addActionListener(e -> deleteSelectedRow());

        buttonPanel.add(addToCartButton);
        buttonPanel.add(deleteButton);

        // Panel Tabel dengan DefaultTableModel
        String[] columnNames = {"Product Name", "Quantity", "Price", "Subtotal"};
        Object[][] data = {};
        tableModel = new DefaultTableModel(data, columnNames);
        cartTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(cartTable);

        //add product prices data
        productPrices = new HashMap<>();
        productPrices.put("Vit", 12000);
        productPrices.put("Aqua", 14000);
        productPrices.put("Prima", 13000);

        // Add panels to the frame
        add(topPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);
        add(scrollPane, BorderLayout.SOUTH);

        // Function untuk mengupdate field harga
        updatePriceField();

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void addToCart() {
        String selectedProduct = (String) productNameComboBox.getSelectedItem();
        if (selectedProduct != null) {
            try {
                int quantity = Integer.parseInt(quantityField.getText());
                if (quantity <= 0) {
                    JOptionPane.showMessageDialog(this, "Quantity must be greater than 0", "Input Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Integer price = productPrices.get(selectedProduct);
                if (price != null) {
                    int rowIndex = findProductInTable(selectedProduct);
                    if (rowIndex >= 0) {
                        // Product sudah ada di cart, update quantity dan subtotal
                        int existingQuantity = (int) tableModel.getValueAt(rowIndex, 1);
                        int newQuantity = existingQuantity + quantity;
                        tableModel.setValueAt(newQuantity, rowIndex, 1);
                        tableModel.setValueAt(newQuantity * price, rowIndex, 3);
                    } else {
                        // Product belum ada di cart, buat row baru
                        tableModel.addRow(new Object[]{selectedProduct, quantity, price, quantity * price});
                    }
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Quantity must be a valid number", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private int findProductInTable(String productName) {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            if (tableModel.getValueAt(i, 0).equals(productName)) {
                return i;
            }
        }
        return -1; // Product not found
    }

    private void deleteSelectedRow() {
        int selectedRow = cartTable.getSelectedRow();
        if (selectedRow >= 0) {
            // konfirmasi sebelum menghapus product
            int confirm = JOptionPane.showConfirmDialog(this, "Do you really want to delete the selected item?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                tableModel.removeRow(selectedRow);
            }
        } else {
            // row belum dipilih, tampilkan pesan error
            JOptionPane.showMessageDialog(this, "Please select a row to delete.", "Selection Required", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void updatePriceField() {
        Map<String, Integer> productPrices = new HashMap<>();
        productPrices.put("Vit", 12000);
        productPrices.put("Aqua", 14000);
        productPrices.put("Prima", 13000);

        String selectedProduct = (String) productNameComboBox.getSelectedItem();
        if (selectedProduct != null) {
            Integer price = productPrices.get(selectedProduct);
            priceField.setText(price != null ? price.toString() : "");
        }
    }

    public static void main(String[] args) {
        new Exam();
    }
}