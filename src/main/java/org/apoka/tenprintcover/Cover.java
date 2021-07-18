package org.apoka.tenprintcover;

import org.apoka.graphics.Image;

import java.awt.*;
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Cover {
    public static final int DEFAULT_WIDTH = 400;
    public static final int DEFAULT_HEIGHT = 600;
    private static final String C64_LETTERS = " qQwWeErRtTyYuUiIoOpPaAsSdDfFgGhHjJkKlL:zZxXcCvVbBnNmM,;?<>@[]1234567890.=-+*/";
    private final String title;
    private final String subtitle;
    private final String author;
    private final int cover_width;
    private final int cover_height;
    private final int cover_margin;
    private Image cover_image;
    private Color base_color;
    private Color shape_color;

    public Cover(String title, String author) {
        this(title, "", author);
    }

    public Cover(String title, String subtitle, String author) {
        this(title, subtitle, author, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    public Cover(String title, String subtitle, String author, int width, int height) {
        this.title = title;
        this.subtitle = subtitle;
        this.author = author;
        this.cover_width = width;
        this.cover_height = height;
        cover_margin = 2;
        cover_image = new Image(cover_width, cover_height);
        processColors();
    }

    /**
     * Based on some initial constants and the title+author strings, generate a base
     * background color and a shape color to draw onto the background. Try to keep
     * these two colors somewhat compatible with each other by varying only their hue.
     */
    private void processColors() {
        float base_saturation = 1f;
        int base_brightness = 90;
        int color_distance = 100;

        int counts = title.length() + author.length();
        int color_seed = _map(_clip(counts, 2, 80), 2, 80, 10, 360);
        base_color = Color.getHSBColor(color_seed/60f/6f, base_saturation, (base_brightness-(counts % 20))/100f);
        shape_color =  Color.getHSBColor(
                ((color_seed + color_distance) % 360)/60f/6f,
                base_saturation,
                0.9f
        );

        if ((counts % 10) == 0) {
            Color temp = shape_color;
            shape_color = base_color;
            base_color = temp;
        }
    }

    public Image draw() {
        drawBackground();
        drawArtwork();
        drawText();

        //Return the cover Image instance.
        return cover_image;
    }

    /**
     * Fill the background of the image with white.
     */
    private void drawBackground() {
        cover_image.rect(0,0,cover_width,cover_height, Color.WHITE);
    }

    /**
     * Allocate fonts for the title and the author, and draw the text.
     */
    private void drawText() {
        Color fill = new Color(50, 50, 50);

        float title_font_size = cover_width * 0.08f;
        float subtitle_font_size = cover_width * 0.05f;
        String title_font_family = selectFont(title);
        String subtitle_font_family = selectFont(subtitle);
        title_font_size = scale_font(title, title_font_size);
        subtitle_font_size = scale_font(subtitle, subtitle_font_size);

        Font title_font = getFont(title_font_family, title_font_size, true);
        Font subtitle_font = getFont(subtitle_font_family, subtitle_font_size, false);
        int title_height = (int) ((cover_height - cover_width - (cover_height * cover_margin / 100)) * 0.75);

        int x = cover_height * cover_margin / 100;
        int y = cover_height * cover_margin / 100 * 2;
        int width = cover_width - (2 * cover_height * cover_margin / 100);
        int height = title_height;
        int[] tResult = cover_image.text(title, x, y, width, height, fill, title_font);
        if (subtitle != null && !subtitle.isEmpty()) {
            y = (int) Math.min(
                    y + tResult[1] * tResult[0] * cover_height,
                    title_height - subtitle_font_size
            );
            cover_image.text(subtitle, x, y, width, height, fill, subtitle_font);
        }

        float author_font_size = cover_width * 0.07f;
        Font author_font = getFont(selectFont(author), author_font_size, false);
        int author_height = (int) ((cover_height - cover_width - (cover_height * cover_margin / 100)) * 0.25);

        x = cover_height * cover_margin / 100;
        y = title_height;
        width = cover_width - (2 * cover_height * cover_margin / 100);
        height = author_height;
        cover_image.text(author, x, y, width, height, fill, author_font);
    }

    public Font getFont(String family, float size, boolean bold) {
        Map<TextAttribute, Object> attributes = new HashMap<>();
        attributes.put(TextAttribute.FAMILY, family);
        attributes.put(TextAttribute.SIZE, size);
        attributes.put(TextAttribute.WIDTH, 0.68f);

        if(bold) {
            attributes.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD);
        } else {
            attributes.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_REGULAR);
        }

        return new Font(attributes);
    }

    /**
     * Draw the actual artwork for the cover. Given the length of the title string,
     * generate an appropriate sized grid and draw C64 PETSCII into each of the cells.
     * https://www.c64-wiki.com/index.php/PETSCII
     * https://en.wikipedia.org/wiki/PETSCII#/media/File:PET_Keyboard.svg
     */
     private void drawArtwork() {
         int artwork_start_x = 0;
         int artwork_start_y = cover_height - cover_width;

         Grid grid = breakGrid();
         cover_image.rect(0, 0, cover_width, cover_height * cover_margin / 100, base_color);
         cover_image.rect(0, artwork_start_y, cover_width, cover_width, base_color);
         String c64_title = c64Convert();
         List<Integer> range = IntStream.range(0, grid.total).boxed().collect(Collectors.toList());
         List<Character> characters = cycle(c64_title, range.size());

         for (int i = 0; i < range.size(); i++) {
             int grid_x = range.get(i) % grid.count;
             int grid_y = range.get(i) / grid.count;
             int x = grid_x * grid.size + artwork_start_x;
             int y = grid_y * grid.size + artwork_start_y;
             drawShape(characters.get(i), x, y, grid.size);
         }
     }

    private List<Character> cycle(String str, int size) {
        List<Character> chars = str.chars().mapToObj(e -> (char)e).collect(Collectors.toList());

        return Stream.iterate(chars.stream(), s -> chars.stream()).flatMap(Function.identity()).limit(size).collect(Collectors.toList());
    }

    /**
     * Given an alphabetic character from the book's title string and the x, y
     * coordinates and size of the cell within the cover grid, draw a PETSCII
     * shape into that cell.
     * @param c
     * @param x
     * @param y
     * @param s
     */
    private void drawShape(char c, int x, int y, int s) {
        int shape_thickness = 10;
        int thick = s * shape_thickness / 100;

        if (c == 'q' || c == 'Q') {
            cover_image.ellipse(x, y, s, s, shape_color);
        } else if (c == 'w' || c == 'W') {
            cover_image.ellipse(x, y, s, s, shape_color);
            cover_image.ellipse(x + thick, y + thick, s - (thick * 2), s - (thick * 2), base_color);
        } else if (c == 'e' || c == 'E') {
            cover_image.rect(x, y + thick, s, thick, shape_color);
        } else if (c == 'r' || c == 'R') {
            cover_image.rect(x, y + s - (thick * 2), s, thick, shape_color);
        } else if (c == 't' || c == 'T') {
            cover_image.rect(x + thick, y, thick, s, shape_color);
        } else if (c == 'y' || c == 'Y') {
            cover_image.rect(x + s - (thick * 2), y, thick, s, shape_color);
        } else if (c == 'u' || c == 'U') {
            cover_image.arc(x, y, 2 * s, 2 * s, 90, 90, shape_color, thick);
        } else if (c == 'i' || c == 'I') {
            cover_image.arc(x - s, y, 2 * s, 2 * s, 0, 90, shape_color, thick);
        } else if (c == 'o' || c == 'O') {
            cover_image.rect(x, y, s, thick, shape_color);
            cover_image.rect(x, y, thick, s, shape_color);
        } else if (c == 'p' || c == 'P') {
            cover_image.rect(x, y, s, thick, shape_color);
            cover_image.rect(x + s - thick, y, thick, s, shape_color);
        } else if (c == 'a'|| c=='A'){
            cover_image.triangle(x, y + s, x + (s / 2), y, x + s, y + s, shape_color);
        } else  if (c == 's'|| c=='S') {
            cover_image.triangle(x, y, x + (s / 2), y + s, x + s, y, shape_color);
        } else if (c == 'd'|| c=='D') {
            cover_image.rect(x, y + (thick * 2), s, thick, shape_color);
        } else if (c == 'f'|| c =='F') {
            cover_image.rect(x, y + s - (thick * 3), s, thick, shape_color);
        } else if (c == 'g' || c=='G') {
            cover_image.rect(x + (thick * 2), y, thick, s, shape_color);
        } else if (c == 'h'|| c=='H') {
            cover_image.rect(x + s - (thick * 3), y, thick, s, shape_color);
        } else if (c == 'j' || c=='J') {
            cover_image.arc(x, y - s, 2 * s, 2 * s, 180, 90, shape_color, thick);
        } else if (c == 'k'|| c=='K') {
            cover_image.arc(x - s, y - s, 2 * s, 2 * s, 270, 90, shape_color, thick);
        } else if (c == 'l'|| c=='L') {
            cover_image.rect(x, y, thick, s, shape_color);
            cover_image.rect(x, y + s - thick, s, thick, shape_color);
        } else if (c == ':') {
            cover_image.rect(x + s - thick, y, thick, s, shape_color);
            cover_image.rect(x, y + s - thick, s, thick, shape_color);
        } else if (c == 'z' || c=='Z') {
            cover_image.triangle(x, y + (s / 2), x + (s / 2), y, x + s, y + (s / 2), shape_color);
            cover_image.triangle(x, y + (s / 2), x + (s / 2), y + s, x + s, y + (s / 2), shape_color);
        } else if (c == 'x' || c=='X') {
            cover_image.ellipse(x + (s / 2), y + (s / 3), thick * 2, thick * 2, shape_color);
            cover_image.ellipse(x + (s / 3), y + s - (s / 3), thick * 2, thick * 2, shape_color);
            cover_image.ellipse(x + s - (s / 3), y + s - (s / 3), thick * 2, thick * 2, shape_color);
        } else if (c == 'c'|| c=='C') {
            cover_image.rect(x, y + (thick * 3), s, thick, shape_color);
        } else if (c == 'v'|| c=='V') {
            cover_image.rect(x, y, s, s, shape_color);
            cover_image.triangle(x + thick, y, x + (s / 2), y + (s / 2) - thick, x + s - thick, y, base_color);
            cover_image.triangle(x, y + thick, x + (s / 2) - thick, y + (s / 2), x, y + s - thick, base_color);
            cover_image.triangle(x + thick, y + s, x + (s / 2), y + (s / 2) + thick, x + s - thick, y + s, base_color);
            cover_image.triangle(x + s, y + thick, x + s, y + s - thick, x + (s / 2) + thick, y + (s / 2), base_color);
        } else if (c == 'b' || c=='B') {
            cover_image.rect(x + (thick * 3), y, thick, s, shape_color);
        } else if (c == 'n' || c=='N') {
            cover_image.rect(x, y, s, s, shape_color);
            cover_image.triangle(x, y, x + s - thick, y, x, y + s - thick, base_color);
            cover_image.triangle(x + thick, y + s, x + s, y + s, x + s, y + thick, base_color);
        } else if (c == 'm' || c=='M') {
            cover_image.rect(x, y, s, s, shape_color);
            cover_image.triangle(x + thick, y, x + s, y, x + s, y + s - thick, base_color);
            cover_image.triangle(x, y + thick, x, y + s, x + s - thick, y + s, base_color);
        } else if (c == ',') {
            cover_image.rect(x + (s / 2), y + (s / 2), s / 2, s / 2, shape_color);
        } else if (c == ';') {
            cover_image.rect(x, y + (s / 2), s / 2, s / 2, shape_color);
        } else if (c == '?') {
            cover_image.rect(x, y, s / 2, s / 2, shape_color);
            cover_image.rect(x + (s / 2), y + (s / 2), s / 2, s / 2, shape_color);
        } else if (c == '<') {
            cover_image.rect(x + (s / 2), y, s / 2, s / 2, shape_color);
        } else if (c == '>') {
            cover_image.rect(x, y, s / 2, s / 2, shape_color);
        } else if (c == '@') {
            cover_image.rect(x, y + (s / 2) - (thick / 2), s, thick, shape_color);
        } else if (c == '[') {
            cover_image.rect(x + (s / 2) - (thick / 2), y, thick, s, shape_color);
        } else if (c == ']') {
            cover_image.rect(x, y + (s / 2) - (thick / 2), s, thick, shape_color);
            cover_image.rect(x + (s / 2) - (thick / 2), y, thick, s, shape_color);
        } else if (c =='0') {
            cover_image.rect(x + (s / 2) - (thick / 2), y + (s / 2) - (thick / 2), thick, s / 2 + thick / 2, shape_color);
            cover_image.rect(x + (s / 2) - (thick / 2), y + (s / 2) - (thick / 2), s / 2 + thick / 2, thick, shape_color);
        } else if (c =='1') {
            cover_image.rect(x, y + (s / 2) - (thick / 2), s, thick, shape_color);
            cover_image.rect(x + (s / 2) - (thick / 2), y, thick, s / 2 + thick / 2, shape_color);
        } else if (c =='2') {
            cover_image.rect(x, y + (s / 2) - (thick / 2), s, thick, shape_color);
            cover_image.rect(x + (s / 2) - (thick / 2), y + (s / 2) - (thick / 2), thick, s / 2 + thick / 2, shape_color);
        } else if (c =='3') {
            cover_image.rect(x, y + (s / 2) - (thick / 2), s / 2 + thick / 2, thick, shape_color);
            cover_image.rect(x + (s / 2) - (thick / 2), y, thick, s, shape_color);
        } else if (c =='4') {
            cover_image.rect(x, y, thick * 2, s, shape_color);
        } else if (c =='5') {
            cover_image.rect(x, y, thick * 3, s, shape_color);
        } else if (c =='6') {
            cover_image.rect(x + s - (thick * 3), y, thick * 3, s, shape_color);
        } else if (c =='7') {
            cover_image.rect(x, y, s, thick * 2, shape_color);
        } else if (c =='8') {
            cover_image.rect(x, y, s, thick * 3, shape_color);
        } else if (c =='9') {
            cover_image.rect(x, y + s - (thick * 3), s, thick * 3, shape_color);
        } else if (c =='.') {
            cover_image.rect(x + (s / 2) - (thick / 2), y + (s / 2) - (thick / 2), thick, s / 2 + thick / 2, shape_color);
            cover_image.rect(x, y + (s / 2) - (thick / 2), s / 2 + thick / 2, thick, shape_color);
        } else if (c =='=') {
            cover_image.rect(x + (s / 2) - (thick / 2), y, thick, s / 2 + thick / 2, shape_color);
            cover_image.rect(x, y + (s / 2) - (thick / 2), s / 2, thick, shape_color);
        } else if (c =='-') {
            cover_image.rect(x + (s / 2) - (thick / 2), y, thick, s / 2 + thick / 2, shape_color);
            cover_image.rect(x + (s / 2) - (thick / 2), y + (s / 2) - (thick / 2), s / 2 + thick / 2, thick, shape_color);
        } else if (c =='+') {
            cover_image.rect(x + (s / 2) - (thick / 2), y + (s / 2) - (thick / 2), s / 2 + thick / 2, thick, shape_color);
            cover_image.rect(x + (s / 2) - (thick / 2), y, thick, s, shape_color);
        } else if (c =='*') {
            cover_image.rect(x + s - (thick * 2), y, thick * 2, s, shape_color);
        } else if (c =='/') {
            cover_image.rect(x, y + s - (thick * 2), s, thick * 2, shape_color);
        } else if (c == ' ') {
            cover_image.rect(x, y, s, s, base_color);
        }
        //else
        //assert not "Implement."
    }

    /**
     * Compute the graphics grid size based on the length of the book title.
     */
    private Grid breakGrid() {
        int min_title = 2;
        int max_title = 60;
        int length = _clip(title.length(), min_title, max_title);

        int grid_count = _map(length, min_title, max_title, 2, 11);
        int grid_total = grid_count * grid_count;
        int grid_size = cover_width / grid_count;
        return new Grid(grid_count, grid_total, grid_size);
    }

    /**
     * Given the title of the book, filter through its characters and ensure
     * that only a certain range is used for the title; characters outside of
     * that range are replaced with a somewhat random character.
     * @return
     */
    private String c64Convert() {
        StringBuilder c64_title = new StringBuilder();

        for(char c : title.toCharArray()) {
            if (C64_LETTERS.contains(""+c)) {
                c64_title.append(c);
            } else {
                //random.choice(c64_letters)
                c64_title.append(C64_LETTERS.charAt(c % C64_LETTERS.length()));
            }
        }
        return c64_title.toString();
    }

    /**
     * Return a font appropriate for the text. Uses Noto CJK if text contains letters of
     * Simplified Chinese, Traditional Chinese, Japanese, and Korean (CJK), otherwise Noto Sans.
     * http://www.unicode.org/faq/han_cjk.html
     */
    private String selectFont(String text) {
        for (char c : text.toCharArray()) {
            if (c >=0x4E00) {
                return "Noto Sans CJK SC";
            }
        }

        return "Noto Sans";
    }

    /**
     * If the text is long, use a smaller font size.
     */
    private float scale_font(String text, float font_size) {
        float width = text.length() * font_size;
        if (width > cover_width * 3f) {
            //This is an empirical, unintelligent, heuristic.
            return font_size * 0.8f;
        } else if (width < cover_width) {
            return font_size * 1.2f;
        } else {
            return font_size;
        }
    }

    /**
     * Helper function that implements the Processing function map(). For more
     * details see https://processing.org/reference/map_.html
     * http://stackoverflow.com/questions/17134839/how-does-the-map-function-in-processing-work
     */
    private int _map(int value, int istart, int istop, int ostart, int ostop) {
        return (int) (ostart + (ostop - ostart) * ((value - istart) / (double)(istop - istart)));
    }

    /**
     * Helper function to clip a given value based on a lower/upper bound.
     */
    private int _clip(int value, int lower, int upper) {
        if(value<lower){
            return lower;
        } else if(value>upper) {
            return upper;
        } else {
            return value;
        }
    }
}

class Grid {
    int count;
    int total;
    int size;

    public Grid(int count, int total, int size) {
        this.count=count;
        this.total=total;
        this.size=size;
    }
}
