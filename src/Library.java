import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;

public class Library extends JFrame {
    private JTable booksTable;
    Connection conn = null;
    Statement stmt = null;
    private DefaultTableModel tableModel;

    public Library() {
        setTitle("Book Borrowing App");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Initialize table model and table
        tableModel = new DefaultTableModel(new Object[]{"ID", "Title", "Category", "Quantity"}, 0);
        booksTable = new JTable(tableModel);
        loadBooksFromDatabase(); // Implement this method to load books from the database
        JScrollPane scrollPane = new JScrollPane(booksTable);
        add(scrollPane, BorderLayout.CENTER);

        // Add mouse listener to the table for row selection
        booksTable.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) { // Single-click event
                    int selectedRow = booksTable.getSelectedRow();
                    if (selectedRow >= 0) {
                        // Open a new frame with book details
                        BookDetailsFrame bookDetailsFrame = new BookDetailsFrame(
                                Library.this,
                                tableModel.getValueAt(selectedRow, 0).toString(), // ID
                                tableModel.getValueAt(selectedRow, 1).toString(), // Title
                                tableModel.getValueAt(selectedRow, 2).toString(), // Category
                                tableModel.getValueAt(selectedRow, 3).toString()  // Quantity
                        );
                        bookDetailsFrame.setVisible(true);
                    }
                }
            }
        });

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void loadBooksFromDatabase() {
        ResultSet rs = null;
        try {
            // Step 1: Load the JDBC driver for MySQL.
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Step 2: Establish a connection to the database.
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Test", "root", "");

            // Step 3: Create a statement object for sending SQL statements to the database.
            stmt = conn.createStatement();

            // Step 4: Execute the SQL query and retrieve the data from the 'Book' table.
            String sql = "SELECT id, title, category, quantity FROM Book";
            rs = stmt.executeQuery(sql);

            // Step 5: Clear the table model before loading new data.
            tableModel.setRowCount(0);

            // Step 6: Process the result set and populate the table model.
            while (rs.next()) {
                int id = rs.getInt("id");
                String title = rs.getString("title");
                String category = rs.getString("category");
                int quantity = rs.getInt("quantity");
                tableModel.addRow(new Object[]{id, title, category, quantity});
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Step 7: Close all resources to avoid memory leaks.
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void refreshBooksTable() {
        loadBooksFromDatabase();
    }


    class BookDetailsFrame extends JFrame {
        private JTextField titleTextField;
        private JTextField categoryTextField;
        private JTextField quantityTextField;
        private JTextField customerNameTextField;
        private JTextField phoneNumberTextField;
        private JTextField emailTextField;
        private JButton saveButton;
        private String bookId;

        private Library library;

        public BookDetailsFrame(Library library,String bookId, String title, String category, String quantity) {
            setTitle("Book Details");
            this.library = library;
            this.bookId = bookId;
            setLayout(new GridLayout(7, 2));
            titleTextField = new JTextField(title);
            categoryTextField = new JTextField(category);
            quantityTextField = new JTextField(quantity);
            titleTextField.setEditable(false);
            categoryTextField.setEditable(false);
            quantityTextField.setEditable(false);
            customerNameTextField = new JTextField();
            phoneNumberTextField = new JTextField();
            emailTextField = new JTextField();

            add(new JLabel("Title:"));
            add(titleTextField);
            add(new JLabel("Category:"));
            add(categoryTextField);
            add(new JLabel("Quantity:"));
            add(quantityTextField);
            add(new JLabel("Customer Name:"));
            add(customerNameTextField);
            add(new JLabel("Phone Number:"));
            add(phoneNumberTextField);
            add(new JLabel("Email:"));
            add(emailTextField);

            // Save button
            saveButton = new JButton("Save");
            saveButton.addActionListener(e -> {
                borrowBook();
            });
            add(saveButton);

            pack();
            setLocationRelativeTo(null);
        }

        private void borrowBook() {
            // Validate the input fields
            if (!isPhoneNumberValid(phoneNumberTextField.getText()) || !isEmailValid(emailTextField.getText())) {
                return;
            }

            // Update the database and table model
            updateBookInDatabase(bookId);
        }

        private boolean isPhoneNumberValid(String phoneNumber) {
            // Check if the phone number is not empty and contains only digits
            return !phoneNumber.isEmpty() && phoneNumber.matches("\\d+");
        }

        private boolean isEmailValid(String email) {
            // Check if the email is not empty and contains '@'
            return !email.isEmpty() && email.contains("@");
        }

        private void updateBookInDatabase(String bookId) {
            Connection conn = null;
            PreparedStatement pstmt = null;
            try {
                // Step 1: Establish a connection to the database.
                conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/Test", "root", "");

                // Step 2: Prepare a SQL statement to update the book quantity.
                String sql = "UPDATE Book SET quantity = quantity - 1 WHERE id = ? AND quantity > 0";
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, bookId);

                // Step 3: Execute the update.
                int affectedRows = pstmt.executeUpdate();
                if (affectedRows == 0) {
                    JOptionPane.showMessageDialog(this, "No available quantity to borrow.", "Borrowing Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Book borrowed successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    // Optionally, close this details frame
                    this.dispose();
                }
                if (affectedRows > 0) {
                    library.refreshBooksTable();
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error updating the database.", "Database Error", JOptionPane.ERROR_MESSAGE);
            } finally {
                // Step 4: Close all resources to avoid memory leaks.
                try {
                    if (pstmt != null) pstmt.close();
                    if (conn != null) conn.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {
        new Library();
    }
}