package land.chipmunk.chipmunkmod.modules;

import land.chipmunk.chipmunkmod.util.misc.ColorUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class RainbowName {
    private final MinecraftClient client;

    public static final RainbowName INSTANCE = new RainbowName(MinecraftClient.getInstance());

    private static final String BUKKIT_COLOR_CODES = "123456789abcdefklmorx";
    private static final String TEAM_NAME_CHARACTERS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz_-.+";

    private Timer timer = null;

    public boolean enabled = false;

    private String[] team;

    public String displayName;

    private int startHue = 0;

    public void init () {
        final TimerTask task = new TimerTask() {
            public void run () {
                tick();
            }
        };

        if (timer != null) cleanup();

        timer = new Timer();
        timer.schedule(task, 0, 50);
    }

    private String[] generateColorCodes(int length) {
        String SALTCHARS = BUKKIT_COLOR_CODES;
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < length) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr.split("");
    }

    private String generateUsername (String[] codes) {
        StringBuilder string = new StringBuilder();
        for (String code : codes) string.append("&").append(code);
        return string.toString();
    }

    private String generateUsername (int _codes) {
        StringBuilder string = new StringBuilder();

        final String[] codes = generateColorCodes(_codes);

        for (String code : codes) string.append("&").append(code);
        return string.toString();
    }

    private String generateUsername (char[] codes, char character) {
        StringBuilder string = new StringBuilder();
        for (char code : codes) string.append(character + code);
        return string.toString();
    }

    private String[] generateTeamName () {
        String SALTCHARS = TEAM_NAME_CHARACTERS;
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < TEAM_NAME_CHARACTERS.length()) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr.split("");
    }

    public void enable () {
        final String[] colorCodes = generateColorCodes(8);
        client.getNetworkHandler().sendChatCommand("extras:username " + generateUsername(colorCodes));

        team = generateTeamName();

        CommandCore.INSTANCE.run("minecraft:team add " + String.join("", team));

        CommandCore.INSTANCE.run("minecraft:execute as " + client.getNetworkHandler().getProfile().getId() + " run team join " + String.join("", team));

        enabled = true;
    }

    public void disable () {
        client.getNetworkHandler().sendChatCommand("extras:username " + client.getSession().getUsername());

        CommandCore.INSTANCE.run("minecraft:team remove " + String.join("", team));
        team = null;

        CommandCore.INSTANCE.run("essentials:nick " + client.getSession().getUsername() + " off");

        enabled = false;
    }

    public RainbowName (MinecraftClient client) {
        this.client = client;
        this.displayName = client.getSession().getUsername();
    }

    private void tick () {
        try {
            final ClientPlayNetworkHandler networkHandler = client.getNetworkHandler();

            if (networkHandler == null) {
                cleanup();
                return;
            }

            if (!enabled) return;

            int hue = startHue;
            int increment = (int) (360.0 / Math.max(displayName.length(), 20));

            Component component = Component.empty();
            StringBuilder essentialsNickname = new StringBuilder();

            for (char character : displayName.toCharArray()) {
                String color = String.format("%06x", ColorUtils.hsvToRgb(hue, 100, 100));
                component = component.append(Component.text(character).color(TextColor.fromHexString("#" + color)));
                essentialsNickname.append("\u00a7#").append(color).append(character != ' ' ? character : '_');
                hue = (hue + increment) % 360;
            }

            CommandCore.INSTANCE.run("minecraft:team modify " + String.join("", team) + " prefix " + GsonComponentSerializer.gson().serialize(component));
            CommandCore.INSTANCE.run("essentials:nick " + client.getSession().getUsername() + " " + essentialsNickname);

            startHue = (startHue + increment) % 360;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void cleanup () {
        if (timer == null) return;

        timer.cancel();
        timer.purge();
        timer = null;
    }
}
