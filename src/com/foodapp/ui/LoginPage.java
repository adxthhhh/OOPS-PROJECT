package com.foodapp.ui;

import com.foodapp.auth.AuthService;
import com.foodapp.auth.AuthValidator;
import com.foodapp.auth.Customer;

import javax.swing.*;
import java.awt.*;

public class LoginPage extends JFrame {
    private JTextField emailField = new JTextField(20);
    private JPasswordField passField = new JPasswordField(20);

    public LoginPage() {
        setTitle("Login");
        setSize(350, 250);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel p = new JPanel(new GridLayout(4, 2, 8, 8));
        p.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        p.add(new JLabel("Email:"));    p.add(emailField);
        p.add(new JLabel("Password:")); p.add(passField);

        JButton loginBtn = new JButton("Login");
        JButton regBtn = new JButton("Register");
        p.add(loginBtn); p.add(regBtn);
        add(p);

        loginBtn.addActionListener(e -> doLogin());
        regBtn.addActionListener(e -> { new RegisterPage(); dispose(); });
    }

    private void doLogin() {
        String email = emailField.getText().trim();
        String pw = new String(passField.getPassword());

        if (!AuthValidator.isNotEmpty(email) || !AuthValidator.isNotEmpty(pw)) {
            JOptionPane.showMessageDialog(this, "Please fill all fields.");
            return;
        }
        if (!AuthValidator.isValidEmail(email)) {
            JOptionPane.showMessageDialog(this, "Invalid email format.");
            return;
        }
        Customer c = AuthService.login(email, pw);
        if (c == null) {
            JOptionPane.showMessageDialog(this, "Wrong email or password.");
        } else {
            JOptionPane.showMessageDialog(this, "Welcome, " + c.getName() + "!");
            new HomePage(c.getName()).setVisible(true);
            dispose();
        }
    }

    public static void main(String[] args) {
        UIStyle.setup();
        SwingUtilities.invokeLater(() -> new LoginPage().setVisible(true));
    }
}
