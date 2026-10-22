package com.foodapp.ui;

import javax.swing.*;

/**
 * HomePage - The main dashboard for customers after authentication.
 * Extends {@link ExplorePage} to present the modern MealFlow Explore interface,
 * neighborhood kitchens, popular chef dishes, and meal plans.
 */
public class HomePage extends ExplorePage {

    public HomePage(String customerName) {
        super(customerName);
    }

    public static void main(String[] args) {
        UIStyle.setup();
        SwingUtilities.invokeLater(() -> new HomePage("Rahul").setVisible(true));
    }
}

