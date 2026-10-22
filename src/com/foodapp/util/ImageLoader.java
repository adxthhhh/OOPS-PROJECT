package com.foodapp.util;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ImageLoader - Unified high-performance asset and icon manager for MealFlow.
 * 
 * Capabilities:
 * 1. Checks memory cache first.
 * 2. Checks local disk under assets/images/<filename> with zero network latency.
 * 3. Falls back to async HTTP/HTTPS download with browser User-Agent and automatic local caching.
 * 4. Failsafe procedural vector generation: If offline or image missing, generates a stunning,
 *    high-DPI culinary vector graphic matching the dish/vendor so the UI never displays empty gray boxes.
 * 5. Built-in modern Material/Feather icon generator for all app icons.
 */
public class ImageLoader {

    private static final Map<String, BufferedImage> MEMORY_CACHE = new ConcurrentHashMap<>();
    private static final ExecutorService DOWNLOAD_POOL = Executors.newFixedThreadPool(4);
    private static final File ASSETS_DIR = new File("assets/images");

    static {
        if (!ASSETS_DIR.exists()) {
            ASSETS_DIR.mkdirs();
        }
        // Pre-load common local assets into memory cache
        preloadLocalAssets();
    }

    private static void preloadLocalAssets() {
        if (ASSETS_DIR.exists() && ASSETS_DIR.isDirectory()) {
            File[] files = ASSETS_DIR.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.isFile() && (f.getName().endsWith(".jpg") || f.getName().endsWith(".png") || f.getName().endsWith(".jpeg"))) {
                        try {
                            BufferedImage img = ImageIO.read(f);
                            if (img != null) {
                                MEMORY_CACHE.put(f.getName(), img);
                            }
                        } catch (Exception ignored) {
                        }
                    }
                }
            }
        }
    }

    /**
     * Resolves an image by local filename or remote URL.
     */
    public static BufferedImage getImage(String keyOrUrl, int width, int height) {
        if (keyOrUrl == null || keyOrUrl.trim().isEmpty()) {
            return generateProceduralFoodImage("Culinary Delight", width, height);
        }

        // 1. Check memory cache by exact key
        BufferedImage cached = MEMORY_CACHE.get(keyOrUrl);
        if (cached != null) {
            return cached;
        }

        // 2. Check local assets directory
        String localFilename = extractFilename(keyOrUrl);
        File localFile = new File(ASSETS_DIR, localFilename);
        if (localFile.exists()) {
            try {
                BufferedImage img = ImageIO.read(localFile);
                if (img != null) {
                    MEMORY_CACHE.put(keyOrUrl, img);
                    MEMORY_CACHE.put(localFilename, img);
                    return img;
                }
            } catch (Exception ignored) {
            }
        }

        // 3. If it is a remote URL, trigger async download and return immediate procedural image
        if (keyOrUrl.startsWith("http://") || keyOrUrl.startsWith("https://")) {
            triggerAsyncDownload(keyOrUrl, localFile, null);
            return generateProceduralFoodImage(localFilename, width, height);
        }

        // 4. Return procedural fallback
        return generateProceduralFoodImage(keyOrUrl, width, height);
    }

    /**
     * Creates a self-updating rounded image panel with automatic async loading.
     */
    public static JPanel createImagePanel(String keyOrUrl, int width, int height, int radius) {
        return new ImagePanel(keyOrUrl, width, height, radius);
    }

    private static void triggerAsyncDownload(String urlString, File targetFile, JComponent repaintTarget) {
        DOWNLOAD_POOL.submit(() -> {
            try {
                URL url = URI.create(urlString).toURL();
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
                conn.setConnectTimeout(6000);
                conn.setReadTimeout(6000);

                int code = conn.getResponseCode();
                if (code == 200) {
                    try (InputStream in = conn.getInputStream()) {
                        BufferedImage img = ImageIO.read(in);
                        if (img != null) {
                            MEMORY_CACHE.put(urlString, img);
                            MEMORY_CACHE.put(targetFile.getName(), img);
                            try {
                                ImageIO.write(img, "jpg", targetFile);
                            } catch (Exception ignored) {
                            }
                            if (repaintTarget != null) {
                                SwingUtilities.invokeLater(repaintTarget::repaint);
                            }
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        });
    }

    private static String extractFilename(String keyOrUrl) {
        if (keyOrUrl.endsWith(".jpg") || keyOrUrl.endsWith(".png") || keyOrUrl.endsWith(".jpeg")) {
            return new File(keyOrUrl).getName();
        }
        // Match common known URLs to filenames
        if (keyOrUrl.contains("AB6AXuAmYT") || keyOrUrl.contains("dish_paneer")) return "dish_paneer_bowl.jpg";
        if (keyOrUrl.contains("AB6AXuB09X") || keyOrUrl.contains("smoked_chicken")) return "dish_smoked_chicken.jpg";
        if (keyOrUrl.contains("AB6AXuDZik") || keyOrUrl.contains("quinoa_avocado")) return "dish_quinoa_avocado.jpg";
        if (keyOrUrl.contains("AB6AXuD1WC") || keyOrUrl.contains("keto_pizza")) return "dish_keto_pizza.jpg";
        if (keyOrUrl.contains("AB6AXuDDLa") || keyOrUrl.contains("tofu_salad")) return "dish_tofu_salad.jpg";
        if (keyOrUrl.contains("AB6AXuArEx") || keyOrUrl.contains("thali")) return "dish_thali.jpg";
        if (keyOrUrl.contains("AB6AXuD-EM") || keyOrUrl.contains("green_kitchen")) return "kitchen_green.jpg";
        if (keyOrUrl.contains("AB6AXuDC4H") || keyOrUrl.contains("fitfuel")) return "kitchen_fitfuel.jpg";
        if (keyOrUrl.contains("AB6AXuDMch") || keyOrUrl.contains("annapurna")) return "kitchen_annapurna.jpg";
        if (keyOrUrl.contains("AB6AXuAygi") || keyOrUrl.contains("paneer_details")) return "dish_paneer_details.jpg";
        if (keyOrUrl.contains("AB6AXuBpfp") || keyOrUrl.contains("cart_paneer")) return "cart_paneer_tikka.jpg";
        if (keyOrUrl.contains("AB6AXuBD_Pt") || keyOrUrl.contains("orange_juice")) return "cart_orange_juice.jpg";
        if (keyOrUrl.contains("AB6AXuCt7k") || keyOrUrl.contains("makhana")) return "cart_makhana.jpg";
        if (keyOrUrl.contains("AEtjO1Ufm4") || keyOrUrl.contains("logo")) return "logo.png";
        if (keyOrUrl.contains("AB6AXuBpiY") || keyOrUrl.contains("avatar")) return "avatar.jpg";

        // Hash fallback
        return "img_" + Math.abs(keyOrUrl.hashCode()) + ".jpg";
    }

    /**
     * Generates a high-fidelity vector culinary illustration when offline or image is unavailable.
     */
    public static BufferedImage generateProceduralFoodImage(String label, int width, int height) {
        int w = Math.max(width, 120);
        int h = Math.max(height, 80);
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Rich warm restaurant lighting background gradient
        GradientPaint bg = new GradientPaint(0, 0, new Color(0x32, 0x1E, 0x15), w, h, new Color(0x18, 0x0E, 0x09));
        g2.setPaint(bg);
        g2.fillRect(0, 0, w, h);

        int cx = w / 2;
        int cy = h / 2 + 5;
        int radius = Math.min(w, h) / 2 - 10;

        // Ceramic plate outer rim & shadow
        g2.setColor(new Color(0, 0, 0, 100));
        g2.fillOval(cx - radius - 2, cy - radius / 2 + 4, (radius + 2) * 2, radius);

        g2.setColor(new Color(0x28, 0x1A, 0x14));
        g2.fillOval(cx - radius, cy - radius / 2, radius * 2, radius);

        g2.setColor(new Color(0xE8, 0xDE, 0xD4));
        g2.fillOval(cx - radius + 4, cy - radius / 2 + 3, (radius - 4) * 2, radius - 6);

        // Base bed (rice / grains / salad)
        g2.setColor(new Color(0xB5, 0x93, 0x6E));
        g2.fillOval(cx - radius + 14, cy - radius / 2 + 10, (radius - 14) * 2, radius - 20);

        // Charred toppings / protein cubes
        int[][] cubes = {
                {cx - 24, cy - 14, 20, 16},
                {cx + 4, cy - 20, 22, 18},
                {cx - 10, cy + 2, 24, 18}
        };
        for (int[] c : cubes) {
            g2.setColor(new Color(0xFF, 0xE4, 0xB5));
            g2.fillRoundRect(c[0], c[1], c[2], c[3], 4, 4);
            g2.setColor(new Color(0x8B, 0x45, 0x13));
            g2.drawLine(c[0] + 2, c[1] + 4, c[0] + c[2] - 2, c[1] + 4);
            g2.drawLine(c[0] + 2, c[1] + 9, c[0] + c[2] - 2, c[1] + 9);
        }

        // Vibrant greens / edamame
        g2.setColor(new Color(0x43, 0xA0, 0x47));
        g2.fillOval(cx - 30, cy - 2, 7, 7);
        g2.fillOval(cx - 20, cy + 12, 8, 8);
        g2.fillOval(cx + 18, cy - 4, 8, 8);
        g2.fillOval(cx + 26, cy + 8, 7, 7);

        // Mint leaf garnish
        g2.setColor(new Color(0x2E, 0x7D, 0x32));
        g2.fillArc(cx - 4, cy - 26, 12, 16, 45, 180);

        // Label ribbon
        g2.setColor(new Color(0, 0, 0, 140));
        g2.fillRoundRect(8, h - 22, w - 16, 16, 4, 4);
        g2.setColor(new Color(0xFF, 0xDA, 0xD3));
        g2.setFont(new Font("SansSerif", Font.BOLD, 9));
        String cleanLabel = label.replaceAll("(?i)(dish_|kitchen_|cart_|\\.jpg|\\.png)", "").replace('_', ' ');
        if (cleanLabel.length() > 24) cleanLabel = cleanLabel.substring(0, 22) + "..";
        g2.drawString("🍽 " + cleanLabel, 14, h - 10);

        g2.dispose();
        return img;
    }

    /**
     * Reusable visual ImagePanel with rounded clipping.
     */
    public static class ImagePanel extends JPanel {
        private final String keyOrUrl;
        private final int width;
        private final int height;
        private final int arc;

        public ImagePanel(String keyOrUrl, int width, int height, int arc) {
            this.keyOrUrl = keyOrUrl;
            this.width = width;
            this.height = height;
            this.arc = arc;
            setPreferredSize(new Dimension(width, height));
            setMinimumSize(new Dimension(width, height));
            setMaximumSize(new Dimension(width, height));
            setOpaque(false);

            // Trigger background download if needed
            String localFilename = extractFilename(keyOrUrl);
            File localFile = new File(ASSETS_DIR, localFilename);
            if (!localFile.exists() && (keyOrUrl.startsWith("http://") || keyOrUrl.startsWith("https://"))) {
                triggerAsyncDownload(keyOrUrl, localFile, this);
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Shape clip = new RoundRectangle2D.Float(0, 0, width, height, arc, arc);
            g2.setClip(clip);

            BufferedImage img = getImage(keyOrUrl, width, height);
            if (img != null) {
                g2.drawImage(img, 0, 0, width, height, null);
            } else {
                g2.setColor(new Color(0xE2, 0xEA, 0xE2));
                g2.fillRect(0, 0, width, height);
            }

            g2.setClip(null);
            // Subtle 1px anti-aliased border
            g2.setColor(new Color(0, 0, 0, 30));
            g2.setStroke(new BasicStroke(1f));
            g2.draw(new RoundRectangle2D.Float(0.5f, 0.5f, width - 1, height - 1, arc, arc));

            g2.dispose();
        }
    }

    /**
     * Vector icon generator for consistent, crisp Material/Feather icons.
     */
    public static Icon getIcon(String name, int size, Color color) {
        return new VectorIcon(name, size, color);
    }

    private static class VectorIcon implements Icon {
        private final String name;
        private final int size;
        private final Color color;

        public VectorIcon(String name, int size, Color color) {
            this.name = name.toLowerCase();
            this.size = size;
            this.color = color;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            switch (name) {
                case "explore":
                case "compass":
                    g2.drawOval(x + 2, y + 2, size - 4, size - 4);
                    Polygon needle = new Polygon();
                    needle.addPoint(x + size / 2, y + 4);
                    needle.addPoint(x + size - 5, x + size / 2);
                    needle.addPoint(x + size / 2, y + size - 4);
                    needle.addPoint(x + 5, x + size / 2);
                    g2.fill(needle);
                    break;

                case "restaurant_menu":
                case "meal_plans":
                case "cutlery":
                    // Fork & Knife
                    g2.drawLine(x + 4, y + 3, x + 4, y + size - 3);
                    g2.drawLine(x + 2, y + 3, x + 6, y + 3);
                    g2.drawLine(x + size - 5, y + 3, x + size - 5, y + size - 3);
                    g2.drawArc(x + size - 8, y + 3, 6, 8, 0, 180);
                    break;

                case "receipt_long":
                case "orders":
                    g2.drawRoundRect(x + 3, y + 2, size - 6, size - 4, 3, 3);
                    g2.drawLine(x + 6, y + 6, x + size - 6, y + 6);
                    g2.drawLine(x + 6, y + 10, x + size - 6, y + 10);
                    g2.drawLine(x + 6, y + 14, x + size - 10, y + 14);
                    break;

                case "autorenew":
                case "subscriptions":
                    g2.drawArc(x + 2, y + 2, size - 4, size - 4, 45, 270);
                    g2.drawLine(x + size - 4, y + size / 2 - 2, x + size - 2, y + size / 2 + 3);
                    g2.drawLine(x + size - 6, y + size / 2 + 3, x + size - 2, y + size / 2 + 3);
                    break;

                case "manage_accounts":
                case "profile":
                    g2.drawOval(x + size / 2 - 4, y + 2, 8, 8);
                    g2.drawArc(x + 2, y + size - 8, size - 4, 12, 0, 180);
                    break;

                case "search":
                    int r = size / 2 - 2;
                    g2.drawOval(x + 2, y + 2, r, r);
                    g2.drawLine(x + r, y + r, x + size - 3, y + size - 3);
                    break;

                case "location_on":
                case "pin":
                    g2.drawOval(x + size / 2 - 3, y + 3, 6, 6);
                    g2.drawArc(x + 3, y + 2, size - 6, size - 4, 0, 180);
                    g2.drawLine(x + 3, y + size / 2, x + size / 2, y + size - 2);
                    g2.drawLine(x + size - 3, y + size / 2, x + size / 2, y + size - 2);
                    break;

                case "notifications":
                case "bell":
                    g2.drawArc(x + 3, y + 4, size - 6, size - 6, 0, 180);
                    g2.drawLine(x + 3, y + size - 6, x + size - 3, y + size - 6);
                    g2.drawOval(x + size / 2 - 2, y + size - 4, 4, 3);
                    break;

                case "shopping_bag":
                case "cart":
                    g2.drawRoundRect(x + 3, y + 5, size - 6, size - 7, 3, 3);
                    g2.drawArc(x + 6, y + 2, size - 12, 6, 0, 180);
                    break;

                case "storefront":
                    g2.drawRect(x + 3, y + 6, size - 6, size - 8);
                    g2.drawLine(x + 2, y + 6, x + size - 2, y + 6);
                    g2.drawLine(x + size / 2, y + 6, x + size / 2, y + size - 2);
                    break;

                case "star":
                    g2.drawString("★", x + 1, y + size - 2);
                    break;

                case "bolt":
                    g2.drawString("⚡", x + 1, y + size - 2);
                    break;

                case "leaf":
                    g2.drawString("🍃", x + 1, y + size - 2);
                    break;

                case "lock":
                    g2.drawRect(x + 3, y + size / 2 - 1, size - 6, size / 2 - 1);
                    g2.drawArc(x + size / 2 - 4, y + 2, 8, 8, 0, 180);
                    break;

                default:
                    g2.drawOval(x + 2, y + 2, size - 4, size - 4);
                    break;
            }

            g2.dispose();
        }

        @Override
        public int getIconWidth() {
            return size;
        }

        @Override
        public int getIconHeight() {
            return size;
        }
    }
}
