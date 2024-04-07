package JDBC_sql;

import org.sqlite.JDBC;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.sql.Date;

/**
 * Author: Candy Torres
 * Course: Software Development I - CEN 3024C
 * Due Date: April 7, 2024.
 * Class Name: JDBC_sql
 * Description: Represents the main page for Library Management System GUI integrating SQLite.
 * This page provides access to various functions such as adding, removing,
 * checking books in/out, upload books from a txt file, and database display.
 */
public class JDBC_sql extends JFrame {
    private static final String DB_URL = "jdbc:sqlite:C:\\Users\\Candy\\Desktop\\JDBC\\LMSlibrary.db";
    private Connection connection;
    private Statement statement;
    private JLabel outputLabel;

    public JDBC_sql() {
        setTitle("Library Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 400);
        setLocationRelativeTo(null);

        // Set up UI components
        Font titleFont = new Font("Arial", Font.BOLD, 24);
        getRootPane().setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JLabel titleLabel = new JLabel("Please make a selection", JLabel.CENTER);
        titleLabel.setFont(titleFont);

        outputLabel = new JLabel("Please make a selection");
        outputLabel.setHorizontalAlignment(SwingConstants.CENTER);
        outputLabel.setFont(outputLabel.getFont().deriveFont(Font.BOLD, 20f));
        JPanel buttonPanel = new JPanel(new GridLayout(0, 1, 0, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        getContentPane().add(titleLabel, BorderLayout.NORTH);
        getContentPane().add(buttonPanel, BorderLayout.CENTER);

        // Connect to the database
        try {

            // Register the JDBC driver
            Class.forName("org.sqlite.JDBC");

            // establish connection
            connection = DriverManager.getConnection(DB_URL);
            statement = connection.createStatement();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        // Check if statement is null after initialization
        if (statement == null) {
            JOptionPane.showMessageDialog(null, "Failed to initialize database connection.", "Error", JOptionPane.ERROR_MESSAGE);
        }


        // Initialize main buttons
        switchToMainButtons(buttonPanel);
    }

    private void switchToMainButtons(JPanel buttonPanel) {
        buttonPanel.removeAll();
        JButton displayButton = new JButton("Display Database");
        JButton removeButton = new JButton("Remove Book");
        JButton checkOutButton = new JButton("Check Out Book");
        JButton checkInButton = new JButton("Check In Book");
        JButton exitButton = new JButton("Exit");

        // Set font for buttons
        Font buttonFont = new Font(Font.SANS_SERIF, Font.PLAIN, 14);
        displayButton.setFont(buttonFont);
        removeButton.setFont(buttonFont);
        checkOutButton.setFont(buttonFont);
        checkInButton.setFont(buttonFont);
        exitButton.setFont(buttonFont);

        // Add action listeners for buttons
        displayButton.addActionListener(e -> {
            try {
                displayDatabase();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "An error occurred while displaying the database.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        removeButton.addActionListener(e -> removeBook());
        checkOutButton.addActionListener(e -> checkOutBook());
        checkInButton.addActionListener(e -> checkInBook());
        exitButton.addActionListener(e -> exitApplication());

        // Add buttons to panel
        buttonPanel.add(displayButton);
        buttonPanel.add(removeButton);
        buttonPanel.add(checkOutButton);
        buttonPanel.add(checkInButton);
        buttonPanel.add(exitButton);

        // Refresh the panel
        revalidate();
        repaint();
    }

    private void removeBook() {
        String barcode = JOptionPane.showInputDialog(null, "Enter barcode of the book to remove:");
        if (barcode != null) {
            try {
                String query = "DELETE FROM books WHERE barcode = ?";
                PreparedStatement statement = connection.prepareStatement(query);
                statement.setString(1, barcode);
                int rowsDeleted = statement.executeUpdate();
                if (rowsDeleted > 0) {
                    JOptionPane.showMessageDialog(null, "Book removed successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(null, "Book with barcode " + barcode + " not found.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "An error occurred while removing the book.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void checkOutBook() {
        String title = JOptionPane.showInputDialog(null, "Enter the title of the book to check out:");
        if (title != null) {
            try {
                String query = "SELECT * FROM books WHERE title = ? AND status = 'checked in'";
                PreparedStatement checkStatement = connection.prepareStatement(query);
                checkStatement.setString(1, title);
                ResultSet resultSet = checkStatement.executeQuery();

                if (resultSet.next()) {
                    int id = resultSet.getInt("barcode");
                    String updateQuery = "UPDATE books SET status = 'checked out', due_date = ? WHERE barcode = ?";
                    PreparedStatement updateStatement = connection.prepareStatement(updateQuery);

                    LocalDate currentDate = LocalDate.now();
                    LocalDate dueDate = currentDate.plusWeeks(4);
                    Date sqlDueDate = Date.valueOf(dueDate);

                    updateStatement.setDate(1, sqlDueDate);
                    updateStatement.setInt(2, id);

                    int rowsUpdated = updateStatement.executeUpdate();
                    if (rowsUpdated > 0) {
                        JOptionPane.showMessageDialog(null, "Book '" + title + "' checked out successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(null, "Failed to check out the book.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Book '" + title + "' is not available for checkout.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "An error occurred while checking out the book.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void checkInBook() {
        String title = JOptionPane.showInputDialog(null, "Enter the title of the book to check in:");
        if (title != null) {
            try {
                String query = "SELECT * FROM books WHERE title = ? AND status = 'checked out'";
                PreparedStatement checkStatement = connection.prepareStatement(query);
                checkStatement.setString(1, title);
                ResultSet resultSet = checkStatement.executeQuery();

                if (resultSet.next()) {
                    int id = resultSet.getInt("barcode");
                    String updateQuery = "UPDATE books SET status = 'checked in', due_date = NULL WHERE barcode = ?";
                    PreparedStatement updateStatement = connection.prepareStatement(updateQuery);

                    updateStatement.setInt(1, id);

                    int rowsUpdated = updateStatement.executeUpdate();
                    if (rowsUpdated > 0) {
                        JOptionPane.showMessageDialog(null, "Book '" + title + "' checked in successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(null, "Failed to check in the book.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Book '" + title + "' is not currently checked out.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "An error occurred while checking in the book.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void displayDatabase() throws SQLException {
        JFrame bookList = new JFrame();
        bookList.setSize(680, 450);
        JLabel bookItems = new JLabel();
        bookItems.setHorizontalAlignment(SwingConstants.CENTER);
        bookItems.setFont(bookItems.getFont().deriveFont(Font.BOLD, 20f));

        try {
            ResultSet resultSet = statement.executeQuery("SELECT * FROM books");
            StringBuilder stringBuilder = new StringBuilder();
            while (resultSet.next()) {
                String title = resultSet.getString("title");
                String author = resultSet.getString("author");
                String genre = resultSet.getString("genre");
                Integer barcode = resultSet.getInt("barcode");
                String status = resultSet.getString("status");
                String dueDate = resultSet.getString("due_date");

                stringBuilder.append("Title: ").append(title).append(", Author: ")
                        .append(author).append(", Genre: ").append(genre).append(", Barcode: ").append(barcode)
                        .append(", Status: ").append(status).append(", Due Date: ").append(dueDate).append("<br>");
            }
            bookItems.setText("<html><body><div align='center'>Library Database Contents</div><br>" + stringBuilder.toString() + "</body></html>");
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "An error occurred while displaying the database.", "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            if (bookList != null) {
                bookList.getContentPane().add(bookItems);
                bookList.setVisible(true);
            }
        }
    }


    private void exitApplication() {
        int choice = JOptionPane.showConfirmDialog(null, "Are you sure you want to exit?", "Confirm Exit", JOptionPane.YES_NO_OPTION);
        if (choice == JOptionPane.YES_OPTION) {
            dispose();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new JDBC_sql().setVisible(true));
    }
}
