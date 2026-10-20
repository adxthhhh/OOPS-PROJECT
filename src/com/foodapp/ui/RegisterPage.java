package com.foodapp.ui;

import com.foodapp.auth.AuthService;
import com.foodapp.auth.AuthValidator;
import com.foodapp.auth.Customer;

import javax.swing.*;
import java.awt.*;

public class RegisterPage extends JFrame {
    private JTextField nameField = new JTextField(20);
    private JTextField emailField = new JTextField(20);
    private JTextField phoneField = new JTextField(20);
    private JPasswordField passField = new JPasswordField(20);
    private JPasswordField confirmField = new JPasswordField(20);

    public RegisterPage() {
        setTitle("Register");
        setSize(380, 340);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel p = new JPanel(new GridLayout(6, 2, 8, 8));
        p.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        p.add(new JLabel("Name:"));             p.add(nameField);
        p.add(new JLabel("Email:"));            p.add(emailField);
        p.add(new JLabel("Phone:"));            p.add(phoneField);
        p.add(new JLabel("Password:"));         p.add(passField);
        p.add(new JLabel("Confirm Password:")); p.add(confirmField);

        JButton regBtn = new JButton("Register");
        JButton backBtn = new JButton("Back to Login");
        p.add(regBtn); p.add(backBtn);
        add(p);
        setVisible(true);

        regBtn.addActionListener(e -> doRegister());
        backBtn.addActionListener(e -> { new LoginPage().setVisible(true); dispose(); });
    }

    private void doRegister() {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String pw = new String(passField.getPassword());
        String confirm = new String(confirmField.getPassword());

        String error = null;
        if (!AuthValidator.isNotEmpty(name)) error = "Name is required.";
        else if (!AuthValidator.isValidEmail(email)) error = "Invalid email.";
        else if (!AuthValidator.isValidPhone(phone)) error = "Phone must be 10 digits.";
        else if (!AuthValidator.isValidPassword(pw)) error = "Password must be at least 6 characters.";
        else if (!pw.equals(confirm)) error = "Passwords do not match.";

        if (error != null) {
            JOptionPane.showMessageDialog(this, error);
            return;
        }
        if (AuthService.register(new Customer(name, email, phone, pw))) {
            JOptionPane.showMessageDialog(this, "Registered! Please login.");
            new LoginPage().setVisible(true);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Email already registered.");
        }
    }
}
