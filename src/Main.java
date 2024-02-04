import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

import java.text.SimpleDateFormat;

public class Main extends JFrame {
    private static JTextField txtName, txtAddress, txtNIK, txtDOB;
    private static JButton btnAdd;
    private static JButton btnUpdate;
    private static JButton btnDelete;

    private static JButton btnExit;

    private static JButton btnListPatients;

    private JTable table;

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MMM-dd");

    static String url = "jdbc:mysql://localhost:3306/pasien";
    static String user = "root";
    static String password = "";

    public static void main(String[] args) {
        // Main frame
        new Main();
    }

    public Main(){
        JFrame frame = new JFrame("Data Pasien");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Add input fields to form panel
        formPanel.add(new JLabel("Nama"), gbc);
        txtName = new JTextField(20);
        formPanel.add(txtName, gbc);
        formPanel.add(new JLabel("NIK"), gbc);
        txtNIK = new JTextField(20);
        formPanel.add(txtNIK, gbc);
        formPanel.add(new JLabel("Alamat"), gbc);
        txtAddress = new JTextField(20);
        formPanel.add(txtAddress, gbc);
        formPanel.add(new JLabel("Tanggal Lahir"), gbc);
        txtDOB = new JTextField(20);
        formPanel.add(txtDOB, gbc);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(btnAdd = new JButton("Add"));
        buttonPanel.add(btnUpdate = new JButton("Update"));
        buttonPanel.add(btnDelete = new JButton("Delete"));
        buttonPanel.add(btnListPatients = new JButton("List Patients"));
        buttonPanel.add(btnExit = new JButton("Exit"));


        // Add form and button panels to the left side
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(formPanel, BorderLayout.NORTH);
        leftPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Table panel
        table =  new JTable(showData());
        final JScrollPane[] tableScrollPane = {new JScrollPane(table)};

        // Add panels to main frame
        frame.add(leftPanel, BorderLayout.WEST);
        //frame.add(tableScrollPane[0], BorderLayout.CENTER);

        btnAdd.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addData();
                DefaultTableModel tableModel = showData();
                table.setModel(tableModel);
                table.revalidate();
                table.repaint();
            }
        });

        btnUpdate.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String nik = txtNIK.getText();
                updateData(nik);
                DefaultTableModel tableModel = showData();
                table.setModel(tableModel);
                table.revalidate();
                table.repaint();
            }
        });

        btnDelete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteData();
                DefaultTableModel tableModel = showData();
                table.setModel(tableModel);
                table.revalidate();
                table.repaint();
            }
        });

        btnListPatients.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new PatientListFrame().setVisible(true);
            }
        });

        btnExit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        // Display the frame
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
    public static DefaultTableModel showData(){
        String[] columnNames = {"ID", "nama", "alamat", "NIK", "TL"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);
        try {
            Connection conn = DriverManager.getConnection(url, user, password);
            Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet rs = stmt.executeQuery("SELECT * FROM datapasien");
            while (rs.next()) {
                Object[] row = new Object[5];
                row[0] = rs.getInt("ID");
                row[1] = rs.getString("nama");
                row[2] = rs.getString("alamat");
                row[3] = rs.getInt("nik");
                row[4] = rs.getDate("TL");
                tableModel.addRow(row);
            }
        } catch (SQLException error) {
            error.printStackTrace();
        }
        return tableModel;
    }

    public void addData(){
        String name = txtName.getText();
        String address = txtAddress.getText();
        String nik = txtNIK.getText();
        String dob = txtDOB.getText(); // Assuming this is a string in the format "YYYY-MM-DD"

        String query = "INSERT INTO datapasien (nama, alamat, nik, TL) VALUES (?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(url, user, password);
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, name);
            pstmt.setString(2, address);
            pstmt.setLong(3, Long.parseLong(nik));
            pstmt.setDate(4, Date.valueOf(dob));

            // check if nik already exists
            String query2 = "SELECT * FROM datapasien WHERE nik = ?";
            PreparedStatement pstmt2 = conn.prepareStatement(query2, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            pstmt2.setLong(1, Long.parseLong(nik));
            ResultSet pt = pstmt2.executeQuery();
            pt.last();
            if (pt.getRow() > 0) {
                JOptionPane.showMessageDialog(this, "Data pasien sudah ada.");
                return;
            }

            // execute the preparedstatement
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                JOptionPane.showMessageDialog(this, "Data pasien berhasil ditambahkan.");
            } else {
                JOptionPane.showMessageDialog(this, "Data pasien gagal ditambahkan.");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }



    public void updateData(String nik) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        String name = txtName.getText();
        String address = txtAddress.getText();
        String dob = txtDOB.getText(); // Assuming this is a string in the format "YYYY-MM-DD"

        try {
            conn = DriverManager.getConnection(url, user, password);

            String sql = "UPDATE datapasien SET nama = ?, alamat = ?, TL = ? WHERE NIK = ?";
            pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, name);
            pstmt.setString(2, address);
            pstmt.setDate(3, Date.valueOf(dob));
            pstmt.setLong(4, Long.parseLong(nik));

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                JOptionPane.showMessageDialog(this, "Data pasien berhasil diupdate.");
            } else {
                JOptionPane.showMessageDialog(this, "Data pasien gagal diupdate.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    //delete data
    public void deleteData(){
        Connection conn = null;
        PreparedStatement pstmt = null;
        String nik = txtNIK.getText();

        try {
            conn = DriverManager.getConnection(url, user, password);

            String sql = "DELETE from datapasien WHERE NIK = ?";

            pstmt = conn.prepareStatement(sql);
            pstmt.setLong(1, Long.parseLong(nik));

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                JOptionPane.showMessageDialog(this, "Data pasien berhasil dihapus.");
            } else {
                JOptionPane.showMessageDialog(this, "Data pasien gagal dihapus.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (pstmt != null) pstmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }
}

class PatientListFrame extends JFrame {
    static String url = "jdbc:mysql://localhost:3306/pasien";
    static String user = "root";
    static String password = "";
    public PatientListFrame() {
        setTitle("Daftar Pasien");
        setSize(500, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        String[] columnNames = {"ID", "Nama", "Alamat", "NIK", "Tanggal Lahir"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);

        // Fetch data from database and add to table model
        try {
            Connection conn = DriverManager.getConnection(url, user, password);
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM datapasien");
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MMM-dd");
            while (rs.next()) {
                int id = rs.getInt("ID");
                String name = rs.getString("nama");
                String address = rs.getString("alamat");
                String nik = rs.getString("nik");
                Date date = rs.getDate("TL");
                String formattedDate = dateFormat.format(date);
                model.addRow(new Object[]{id, name, address, nik, formattedDate});
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        add(scrollPane, BorderLayout.CENTER);
    }
}
