package land.chipmunk.chipmunkmod.util.misc;

import land.chipmunk.chipmunkmod.ChipmunkMod;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

import java.awt.*;

/**
 * Utility class for handling colors in various formats and performing color-related operations.
 * Supports RGB, RGBA color spaces and various color conversions.
 */
@SuppressWarnings("unused")
public class ColorUtils implements Copyable<ColorUtils>, Serializable<ColorUtils> {
    // Default color constants from configuration
    public static final int PRIMARY = parseHexColor(ChipmunkMod.CONFIG.client.colors.PRIMARY);
    public static final int SECONDARY = parseHexColor(ChipmunkMod.CONFIG.client.colors.SECONDARY);
    public static final int SUCCESS = parseHexColor(ChipmunkMod.CONFIG.client.colors.SUCCESS);
    public static final int DANGER = parseHexColor(ChipmunkMod.CONFIG.client.colors.DANGER);
    public static final int GRAY = parseHexColor(ChipmunkMod.CONFIG.client.colors.GRAY);
    public static final int WARNING = parseHexColor("#FFAE1A");

    // Color components (0-255)
    public int r, g, b, a;

    /**
     * Default constructor - creates white color with full opacity
     */
    public ColorUtils() {
        this(255, 255, 255, 255);
    }

    /**
     * Creates a color with RGB values and full opacity
     */
    public ColorUtils(int r, int g, int b) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = 255;

        validate();
    }

    /**
     * Creates a color with RGBA values
     */
    public ColorUtils(int r, int g, int b, int a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;

        validate();
    }

    /**
     * Creates a color from normalized float values (0.0-1.0)
     */
    public ColorUtils(float r, float g, float b, float a) {
        this.r = (int)(r*255);
        this.g = (int)(g*255);
        this.b = (int)(b*255);
        this.a = (int)(a*255);

        validate();
    }

    /**
     * Creates a color from a packed RGBA integer
     */
    public ColorUtils(int packed) {
        this.r = toRGBAR(packed);
        this.g = toRGBAG(packed);
        this.b = toRGBAB(packed);
        this.a = toRGBAA(packed);
    }

    /**
     * Copy constructor
     */
    public ColorUtils(ColorUtils color) {
        this.r = color.r;
        this.g = color.g;
        this.b = color.b;
        this.a = color.a;
    }

    /**
     * Creates a color from Java AWT Color
     */
    public ColorUtils(java.awt.Color color) {
        this.r = color.getRed();
        this.g = color.getGreen();
        this.b = color.getBlue();
        this.a = color.getAlpha();
    }

    /**
     * Creates a color from Minecraft Formatting
     * If the formatting is not a color, creates white
     */
    public ColorUtils(Formatting formatting) {
        if (formatting.isColor()) {
            this.r = toRGBAR(formatting.getColorValue());
            this.g = toRGBAG(formatting.getColorValue());
            this.b = toRGBAB(formatting.getColorValue());
            this.a = toRGBAA(formatting.getColorValue());
        } else {
            this.r = 255;
            this.g = 255;
            this.b = 255;
            this.a = 255;
        }
    }

    /**
     * Creates a color from Minecraft TextColor
     */
    public ColorUtils(TextColor textColor) {
        this.r = toRGBAR(textColor.getRgb());
        this.g = toRGBAG(textColor.getRgb());
        this.b = toRGBAB(textColor.getRgb());
        this.a = toRGBAA(textColor.getRgb());
    }

    /**
     * Creates a color from Minecraft Style
     * If style has no color, creates white
     */
    public ColorUtils(Style style) {
        TextColor textColor = style.getColor();
        if (textColor == null) {
            this.r = 255;
            this.g = 255;
            this.b = 255;
            this.a = 255;
        } else {
            this.r = toRGBAR(textColor.getRgb());
            this.g = toRGBAG(textColor.getRgb());
            this.b = toRGBAB(textColor.getRgb());
            this.a = toRGBAA(textColor.getRgb());
        }
    }

    /**
     * Parses a hex color string (e.g., "#ffaaff" or "ffaaff") into an RGB integer.
     *
     * @param hexColor Hex color string to convert
     * @return Integer representation of the color
     */
    public static int parseHexColor(String hexColor) {
        String cleanHex = hexColor.startsWith("#") ? hexColor.substring(1) : hexColor;
        return Integer.parseInt(cleanHex, 16);
    }

    /**
     * Creates text with a gradient effect between two colors.
     * Each character in the text will be colored with a color interpolated
     * between startColor and endColor based on its position.
     *
     * @param text The text to apply the gradient to
     * @param startColor The color to start the gradient from
     * @param endColor The color to end the gradient with
     * @return A MutableText object with the gradient effect applied
     */
    public static MutableText createGradientText(String text, int startColor, int endColor) {
        MutableText result = Text.empty();
        int length = text.length();

        for (int i = 0; i < length; i++) {
            int color = interpolateColor(startColor, endColor, (float) i / (length - 1));
            result.append(Text.literal(String.valueOf(text.charAt(i)))
                    .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(color))));
        }

        return result;
    }

    /**
     * Interpolates between two colors based on a progress value.
     *
     * @param color1 The starting color
     * @param color2 The ending color
     * @param progress The interpolation progress (0.0 to 1.0)
     * @return The interpolated color as a packed RGB integer
     */
    private static int interpolateColor(int color1, int color2, float progress) {
        int r = (int) ((color1 >> 16 & 0xFF) + (color2 >> 16 & 0xFF - (color1 >> 16 & 0xFF)) * progress);
        int g = (int) ((color1 >> 8 & 0xFF) + (color2 >> 8 & 0xFF - (color1 >> 8 & 0xFF)) * progress);
        int b = (int) ((color1 & 0xFF) + (color2 & 0xFF - (color1 & 0xFF)) * progress);

        return (r << 16) | (g << 8) | b;
    }

    /**
     * Converts RGBA components to packed integer
     */
    public static int fromRGBA(int r, int g, int b, int a) {
        return (r << 16) + (g << 8) + (b) + (a << 24);
    }

    /**
     * Extracts red component from packed color
     */
    public static int toRGBAR(int color) {
        return (color >> 16) & 0x000000FF;
    }

    /**
     * Extracts green component from packed color
     */
    public static int toRGBAG(int color) {
        return (color >> 8) & 0x000000FF;
    }

    /**
     * Extracts blue component from packed color
     */
    public static int toRGBAB(int color) {
        return (color) & 0x000000FF;
    }

    /**
     * Extracts alpha component from packed color
     */
    public static int toRGBAA(int color) {
        return (color >> 24) & 0x000000FF;
    }

    public static int hsvToRgb (int hue, int saturation, int value) {
        Color color = Color.getHSBColor(hue / 360.0f, saturation / 100.0f, value / 100.0f);
        return color.getRGB() & 0xFFFFFF;
    }

    /**
     * Creates a color from HSV (Hue, Saturation, Value) values
     * @param h Hue (0-360)
     * @param s Saturation (0-1)
     * @param v Value (0-1)
     */
    public static ColorUtils fromHsv(double h, double s, double v) {
        double hh, p, q, t, ff;
        int i;
        double r, g, b;

        if (s <= 0.0) {       // < is bogus, just shuts up warnings
            r = v;
            g = v;
            b = v;
            return new ColorUtils((int) (r * 255), (int) (g * 255), (int) (b * 255), 255);
        }
        hh = h;
        if (hh >= 360.0) hh = 0.0;
        hh /= 60.0;
        i = (int) hh;
        ff = hh - i;
        p = v * (1.0 - s);
        q = v * (1.0 - (s * ff));
        t = v * (1.0 - (s * (1.0 - ff)));

        switch (i) {
            case 0:
                r = v;
                g = t;
                b = p;
                break;
            case 1:
                r = q;
                g = v;
                b = p;
                break;
            case 2:
                r = p;
                g = v;
                b = t;
                break;
            case 3:
                r = p;
                g = q;
                b = v;
                break;
            case 4:
                r = t;
                g = p;
                b = v;
                break;
            case 5:
            default:
                r = v;
                g = p;
                b = q;
                break;
        }
        return new ColorUtils((int) (r * 255), (int) (g * 255), (int) (b * 255), 255);
    }

    /**
     * Sets all color components at once
     */
    public ColorUtils set(int r, int g, int b, int a) {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;

        validate();

        return this;
    }

    // Setter methods for individual components
    public ColorUtils r(int r) {
        this.r = r;
        validate();
        return this;
    }

    public ColorUtils g(int g) {
        this.g = g;
        validate();
        return this;
    }

    public ColorUtils b(int b) {
        this.b = b;
        validate();
        return this;
    }

    public ColorUtils a(int a) {
        this.a = a;
        validate();
        return this;
    }

    /**
     * Sets color from another ColorUtils instance
     */
    @Override
    public ColorUtils set(ColorUtils value) {
        r = value.r;
        g = value.g;
        b = value.b;
        a = value.a;

        validate();

        return this;
    }

    /**
     * Parses color from string in format "r,g,b" or "r,g,b,a"
     * @return true if parsing was successful
     */
    public boolean parse(String text) {
        String[] split = text.split(",");
        if (split.length != 3 && split.length != 4) return false;

        try {
            // Not assigned directly because of exception handling
            int r = Integer.parseInt(split[0]);
            int g = Integer.parseInt(split[1]);
            int b = Integer.parseInt(split[2]);
            int a = split.length == 4 ? Integer.parseInt(split[3]) : this.a;

            this.r = r;
            this.g = g;
            this.b = b;
            this.a = a;

            return true;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

    /**
     * Creates a copy of this color
     */
    @Override
    public ColorUtils copy() {
        return new ColorUtils(r, g, b, a);
    }

    /**
     * Converts to Minecraft TextColor
     */
    public TextColor toTextColor() {
        return TextColor.fromRgb(getPacked());
    }

    /**
     * Creates a new Style with this color
     */
    public Style toStyle() {
        return Style.EMPTY.withColor(toTextColor());
    }

    /**
     * Applies this color to an existing Style
     */
    public Style styleWith(Style style) {
        return style.withColor(toTextColor());
    }

    /**
     * Ensures all color components are within valid range (0-255)
     */
    public void validate() {
        if (r < 0) r = 0;
        else if (r > 255) r = 255;

        if (g < 0) g = 0;
        else if (g > 255) g = 255;

        if (b < 0) b = 0;
        else if (b > 255) b = 255;

        if (a < 0) a = 0;
        else if (a > 255) a = 255;
    }

    /**
     * Converts to Vec3d with normalized components (0.0-1.0)
     */
    public Vec3d getVec3d() {
        return new Vec3d(r / 255.0, g / 255.0, b / 255.0);
    }

    /**
     * Converts to Vector3f with normalized components (0.0-1.0)
     */
    public Vector3f getVec3f() {
        return new Vector3f(r / 255.0f, g / 255.0f, b / 255.0f);
    }

    /**
     * Gets the color as a packed RGBA integer
     */
    public int getPacked() {
        return fromRGBA(r, g, b, a);
    }

    /**
     * Serializes the color to NBT
     */
    @Override
    public NbtCompound toTag() {
        NbtCompound tag = new NbtCompound();

        tag.putInt("r", r);
        tag.putInt("g", g);
        tag.putInt("b", b);
        tag.putInt("a", a);

        return tag;
    }

    /**
     * Deserializes the color from NBT
     */
    @Override
    public ColorUtils fromTag(NbtCompound tag) {
        r = tag.getInt("r");
        g = tag.getInt("g");
        b = tag.getInt("b");
        a = tag.getInt("a");

        validate();
        return this;
    }

    @Override
    public String toString() {
        return r + " " + g + " " + b + " " + a;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ColorUtils color = (ColorUtils) o;

        return r == color.r && g == color.g && b == color.b && a == color.a;
    }

    @Override
    public int hashCode() {
        int result = r;
        result = 31 * result + g;
        result = 31 * result + b;
        result = 31 * result + a;
        return result;
    }
}
