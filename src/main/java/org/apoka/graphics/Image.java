package org.apoka.graphics;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Image {
    private BufferedImage bufImage;

    public Image(int width, int height) {
        bufImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    }

    public void triangle(int x1, int y1, int x2, int y2, int x3, int y3, Color color){
        int[] xs = {x1, x2, x3};
        int[] ys = {y1, y2, y3};

        Graphics g = bufImage.getGraphics();
        g.setColor(color);
        g.fillPolygon(xs, ys, 3);
    }

    public void rect(int x, int y, int width, int height, Color color) {
        Graphics g = bufImage.getGraphics();
        g.setColor(color);
        g.fillRect(x, y, width, height);
    }

    public void ellipse(int x, int y, int width, int height, Color color) {
        Graphics g = bufImage.getGraphics();
        g.setColor(color);

        //g.fillArc(x, y, width, height, 0, 360);
        g.fillOval(x, y, width, height);
    }

    public void arc(int x, int y, int width, int height, int start, int angle, Color color) {
        arc(x, y, width, height, start, angle, color, 1);
    }

    public void arc(int x, int y, int width, int height, int start, int angle, Color color, int thick) {
        Graphics2D g = (Graphics2D) bufImage.getGraphics();
        g.setColor(color);

        //set stroke
        g.setStroke(new BasicStroke(thick));
        g.drawArc(x+thick/2, y+thick/2, width-thick*2, height-thick*2, start, angle);
    }

    public int[] text(String text, int x, int y, int width, int height, Color color, Font font) {
        //Prepare the context for text rendering.
        Graphics2D g = (Graphics2D) bufImage.getGraphics();
        g.setColor(color);

        g.setFont(font);
        //antialiasing
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        //Get some font metrics.
        FontMetrics metrics = g.getFontMetrics(font);

        int fontHeight = metrics.getHeight();

        //Initialize text cursor to the baseline of the font.
        int w_x = x;
        int w_y = metrics.getAscent() + y;

        //Draw the text one line at a time and ensure the bounding box.
        String line = "";
        int nlines = 1;
        for(String word : text.split(" ")) {

            int line_width = metrics.stringWidth(line+" "+word);
            if(line_width<width) {
                line = line+" "+word;
            } else if(!line.isEmpty()) {
                //First word of the line extends beyond the line: chop and done.
                g.drawString(chop(metrics, word, width), w_x, w_y);
                return new int[]{nlines, fontHeight};
            } else {
                //Filled a line, render it, and move on to the next line.
                g.drawString(line, w_x, w_y);
                line = word;
                w_y += metrics.getHeight();

                if(w_y > height) {
                    return new int[]{nlines, fontHeight};
                }
                nlines++;
            }
        }

        g.drawString(line, w_x, w_y);

        return new int[]{nlines, fontHeight};
    }

    private String chop(FontMetrics metrics, String word, int width) {
        String total = "";

        for(char c : word.toCharArray()) {
            int total_width = metrics.stringWidth(total+c+"…");

            if(total_width >= width) {
                return total + "…";
            }

            total += c;
        }

        throw new RuntimeException("Should not be here, else 'word' fit into the bounding box");
    }

    public void save(String filename) throws IOException {
        ImageIO.write(bufImage, "png", new File(filename));
    }
}
