package map;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ColorUtil {
    public static BufferedImage getColorTile(String color) {
        if (color == null || color.isEmpty()) {
            return null;
        }
        try {
            Color c = Color.decode(color);
            BufferedImage image = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = (Graphics2D) image.getGraphics();
            g2d.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 128)); // Semi-transparent color
            g2d.fillRect(0, 0, 256, 256);
            g2d.dispose();
            return image;
        } catch (NumberFormatException e) {
            System.err.println("Invalid color format: " + color);
            return null;
        }
    }

    public static String getColorFromGradient(double time) {
        if (time < 0) {
            return null;
        }

        double[] times = {0, 5, 20, 40, 60};
        Color[] stopcolors = {
            new Color(87, 199, 133),  // #57C785
            new Color(237, 221, 83), // #EDDD53
            new Color(237, 124, 83), // #ED7C53
            new Color(237, 83, 83),  // #ED5353
            new Color(127, 83, 237)  // #7F53ED
        };

        for (int i = 0; i < times.length - 1; i++) {
            if (time <= times[i + 1]) {
                double ratio = (time - times[i]) / (times[i + 1] - times[i]);
                Color interpolatedColor = combineColor(stopcolors[i], stopcolors[i + 1], ratio);
                return String.format("#%02X%02X%02X", interpolatedColor.getRed(), interpolatedColor.getGreen(), interpolatedColor.getBlue());
            }
        }

        Color lastColor = stopcolors[stopcolors.length - 1];
        return String.format("#%02X%02X%02X", lastColor.getRed(), lastColor.getGreen(), lastColor.getBlue());
    }

    private static Color combineColor(Color c1, Color c2, double ratio) {
        int red = (int) (c1.getRed() + ratio * (c2.getRed() - c1.getRed()));
        int green = (int) (c1.getGreen() + ratio * (c2.getGreen() - c1.getGreen()));
        int blue = (int) (c1.getBlue() + ratio * (c2.getBlue() - c1.getBlue()));
        return new Color(red, green, blue);
    }
}