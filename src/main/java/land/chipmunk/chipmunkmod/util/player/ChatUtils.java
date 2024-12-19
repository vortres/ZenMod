package land.chipmunk.chipmunkmod.util.player;

import com.mojang.brigadier.StringReader;
import land.chipmunk.chipmunkmod.ChipmunkMod;
import land.chipmunk.chipmunkmod.util.misc.ColorUtils;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import static land.chipmunk.chipmunkmod.ChipmunkMod.MCInstance;

public class ChatUtils {
    // Define the main prefix that appears before all messages
    private static final Text PREFIX = Text.empty()
            .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(ColorUtils.GRAY)))
            .append("[")
            .append(ColorUtils.createGradientText(ChipmunkMod.NAME, ColorUtils.PRIMARY, ColorUtils.SECONDARY))
            .append("] ");

    /**
     * Basic message sending methods with predefined colors
     */
    // Info messages
    public static void info(String message, Object... args) {
        sendMsg(Style.EMPTY.withColor(Formatting.GRAY), message, args);
    }
    public static void infoPrefix(String prefix, String message, Object... args) {
        sendMsg(prefix, Style.EMPTY.withColor(TextColor.fromRgb(ColorUtils.PRIMARY)), Style.EMPTY.withColor(Formatting.GRAY), message, args);    }

    // Warning messages
    public static void warning(String message, Object... args) {
        sendMsg(Style.EMPTY.withColor(TextColor.fromRgb(ColorUtils.WARNING)), message, args);
    }
    public static void warningPrefix(String prefix, String message, Object... args) {
        sendMsg(prefix, Style.EMPTY.withColor(TextColor.fromRgb(ColorUtils.PRIMARY)), Style.EMPTY.withColor(TextColor.fromRgb(ColorUtils.WARNING)), message, args);
    }

    // Error messages
    public static void error(String message, Object... args) {
        sendMsg(Style.EMPTY.withColor(TextColor.fromRgb(ColorUtils.DANGER)), message, args);
    }
    public static void errorPrefix(String prefix, String message, Object... args) {
        sendMsg(prefix, Style.EMPTY.withColor(TextColor.fromRgb(ColorUtils.PRIMARY)), Style.EMPTY.withColor(TextColor.fromRgb(ColorUtils.DANGER)), message, args);
    }

    /**
     * Core message sending methods
     */
    public static void sendMsg(Style color, String message, Object... args) {
        sendMsg(null, null, color, message, args);
    }

    public static void sendMsg(@Nullable String prefixTitle, @Nullable Style prefixStyle, Style messageStyle, String messageContent, Object... args) {
        // Combine the args to a single message
        String formattedContent = String.format(messageContent, args);
        MutableText message = formatMsg(formattedContent, messageStyle);

        sendMsg(prefixTitle, prefixStyle, message);
    }

    /**
     * Final message sending method that handles all formatting and sends to chat.
     * Format:
     * Without custom prefix: "[ModPrefix] message"
     * With custom prefix: "[ModPrefix] [CustomPrefix] message"
     */
    public static void sendMsg(@Nullable String prefixTitle, @Nullable Style prefixStyle, Text msg) {
        // Safety check - ensure we're in-game
        if (MCInstance.world == null) return;

        MutableText message = Text.empty();
        message.append(PREFIX);
        if (prefixTitle != null) {
            message.append(getCustomPrefix(prefixTitle, prefixStyle));  // Add custom prefix if provided
        }
        message.append(msg);

        // Send to chat
        MCInstance.inGameHud.getChatHud().addMessage(message);
    }

    /**
     * Creates a custom prefix
     */
    private static MutableText getCustomPrefix(String prefixTitle, Style prefixStyle) {
        MutableText prefix = Text.empty();
        // Set brackets to dark gray
        prefix.setStyle(Style.EMPTY.withColor(TextColor.fromRgb(ColorUtils.GRAY)));
        prefix.append("[");

        // Add the prefix title with its color
        MutableText moduleTitle = Text.literal(prefixTitle);
        moduleTitle.setStyle(prefixStyle);
        prefix.append(moduleTitle);

        prefix.append("] ");

        return prefix;
    }

    /**
     * Formats message text with support for formatting tags
     * Supported tags:
     * [default]/[def] - Reset to default color
     * [highlight]/[hl] - White text
     * [underline]/[ul] - Underlined text
     * [bold]/[b] - Bold text
     */
    private static MutableText formatMsg(String message, Style defaultStyle) {
        Style style = defaultStyle;
        StringReader reader = new StringReader(message);
        StringBuilder result = new StringBuilder();
        MutableText text = Text.empty();
        boolean formatting = false;

        while (reader.canRead()) {
            char ch = reader.read();
            if (ch == '[') {
                text.append(Text.literal(result.toString()).setStyle(style));
                result.setLength(0);
                result.append(ch);
                formatting = true;
            } else {
                result.append(ch);

                if (formatting && ch == ']') {
                    String tag = result.toString().toLowerCase();
                    switch (tag) {
                        case "[default]", "[def]" -> {
                            style = defaultStyle;
                            result.setLength(0);
                        }
                        case "[highlight]", "[hl]" -> {
                            style = style.withFormatting(Formatting.WHITE);
                            result.setLength(0);
                        }
                        case "[underline]", "[ul]" -> {
                            style = style.withFormatting(Formatting.UNDERLINE);
                            result.setLength(0);
                        }
                        case "[bold]", "[b]" -> {
                            style = style.withFormatting(Formatting.BOLD);
                            result.setLength(0);
                        }
                    }
                    formatting = false;
                }
            }
        }

        if (!result.isEmpty()) {
            text.append(Text.literal(result.toString()).setStyle(style));
        }
        return text;
    }
}
