package land.chipmunk.chipmunkmod.util.misc;

import com.mojang.brigadier.Command;
import land.chipmunk.chipmunkmod.ChipmunkMod;
import land.chipmunk.chipmunkmod.Configuration;
import land.chipmunk.chipmunkmod.modules.Chat;
import land.chipmunk.chipmunkmod.modules.CustomChat;
import land.chipmunk.chipmunkmod.util.player.ChatUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import static land.chipmunk.chipmunkmod.ChipmunkMod.MCInstance;

public class BotValidationUtils {
    private static final MinecraftClient client = MCInstance;
    private static final ClientPlayerEntity player = client.player;

    private static final Configuration.Bots botinfo = ChipmunkMod.CONFIG.bots;

    public static int lambda(String command) throws RuntimeException {
        final String prefix = botinfo.lambda.prefix;
        final String key = botinfo.lambda.key;

        if (key == null)
            throw new RuntimeException("The key of the bot is unspecified (null), did you incorrectly add it to your config?");

        try {
            String[] arguments = command.split(" ");

            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            long time = System.currentTimeMillis() / 6_000;

            String rawKey = key + ";" + player.getUuidAsString() + ";" + time;

            sha256.update(rawKey.getBytes());
            byte[] hash = sha256.digest();
            ByteBuffer buffer = ByteBuffer.wrap(hash, 0, 6);
            long bigInt = (buffer.getInt() & 0xFFFFFFFFL);
            String stringHash = Long.toString(bigInt, 36);

            final String[] restArguments = Arrays.copyOfRange(arguments, 1, arguments.length);
            final String toSend = prefix +
                    arguments[0] +
                    " " +
                    stringHash +
                    " " +
                    String.join(" ", restArguments);

            Chat.sendChatMessage(toSend);
        } catch (NoSuchAlgorithmException err) {
            ChatUtils.errorPrefix("Validation", "Something went wrong: [hl]%s", err.toString());
        }

        return Command.SINGLE_SUCCESS;
    }

    public static int nbot(String command) throws RuntimeException {
        final String prefix = botinfo.nbot.prefix;
        final String key = botinfo.nbot.key;

        if (key == null)
            throw new RuntimeException("The key of the bot is unspecified (null), did you incorrectly add it to your config?");

        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            long time = System.currentTimeMillis() / 5_000;

            String rawKey = command.replaceAll("&[0-9a-fklmnor]", "") + ";" + player.getUuidAsString() + ";" + time + ";" + key;

            sha256.update(rawKey.getBytes());
            byte[] hash = sha256.digest();
            ByteBuffer buffer = ByteBuffer.wrap(hash, 0, 4);
            long bigInt = (buffer.getInt() & 0xFFFFFFFFL);
            String stringHash = Long.toString(bigInt, 36);

            Chat.sendChatMessage(prefix + command + " " + stringHash, true);
        } catch (NoSuchAlgorithmException err) {
            ChatUtils.errorPrefix("Validation", "Something went wrong: [hl]%s", err.toString());
        }

        return Command.SINGLE_SUCCESS;
    }

    public static int qbot (String command) {
        final String prefix = botinfo.qbot.prefix;
        final String key = botinfo.qbot.key;

        if (key == null)
            throw new RuntimeException("The key of the bot is unspecified (null), did you incorrectly add it to your config?");

        try {
            String[] arguments = command.split(" ");

            long currentTime = System.currentTimeMillis() / 1_000;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            String input = currentTime + key;

            byte[] hash = digest.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            final String[] restArguments = Arrays.copyOfRange(arguments, 1, arguments.length);
            final String result = hexString.substring(0, 16);

            Chat.sendChatMessage(prefix + arguments[0] + " " + result + " " + String.join(" ", restArguments));
        } catch (NoSuchAlgorithmException err) {
            ChatUtils.errorPrefix("Validation", "Something went wrong: [hl]%s", err.toString());
        }

        return Command.SINGLE_SUCCESS;
    }

    public static int hbot (String command) throws RuntimeException {
        final String prefix = botinfo.hbot.prefix;
        final String key = botinfo.hbot.key;

        if (key == null)
            throw new RuntimeException("The key of the bot is unspecified (null), did you incorrectly add it to your config?");

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            String time = String.valueOf(System.currentTimeMillis() / 10_000);

            String input = prefix + command.replaceAll("&[0-9a-fklmnor]", "") + ";" + player.getUuidAsString() + ";" + time + ";" + key;

            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            BigInteger bigInt = new BigInteger(1, Arrays.copyOfRange(hash, 0, 4));
            String stringHash = bigInt.toString(Character.MAX_RADIX);

            Chat.sendChatMessage(prefix + command + " " + stringHash, true);
        } catch (NoSuchAlgorithmException err) {
            ChatUtils.errorPrefix("Validation", "Something went wrong: [hl]%s", err.toString());
        }

        return Command.SINGLE_SUCCESS;
    }

    public static int sbot (String command) throws RuntimeException {
        final String prefix = botinfo.sbot.prefix;
        final String key = botinfo.sbot.key;

        if (key == null)
            throw new RuntimeException("The key of the bot is unspecified (null), did you incorrectly add it to your config?");

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            String time = String.valueOf(System.currentTimeMillis() / 20_000);

            String input = prefix + command.replaceAll("&[0-9a-fklmnorx]", "") + ";" + player.getName().getString() + ";" + time + ";" + key;

            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            BigInteger bigInt = new BigInteger(1, Arrays.copyOfRange(hash, 0, 4));
            String stringHash = bigInt.toString(Character.MAX_RADIX);

            Chat.sendChatMessage(prefix + command + " " + stringHash, true);
        } catch (NoSuchAlgorithmException err) {
            ChatUtils.errorPrefix("Validation", "Something went wrong: [hl]%s", err.toString());
        }

        return Command.SINGLE_SUCCESS;
    }

    public static int chomens (String command) throws RuntimeException {
        final String prefix = botinfo.chomens.prefix;
        final String key = botinfo.chomens.key;

        if (key == null)
            throw new RuntimeException("The key of the bot is unspecified (null), did you incorrectly add it to your config?");

        try {
            String[] arguments = command.split(" ");

            MessageDigest md = MessageDigest.getInstance("SHA-256");
            String time = String.valueOf(System.currentTimeMillis() / 5_000);

            String input = player.getUuidAsString() + arguments[0] + time + key;

            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            String stringHash = ((String) Hexadecimal.encode(hash)).substring(0, 16); // Some fix? idk why this giving error "Cannot resolve method 'substring' in 'Object'" for some reason!!

            final boolean shouldSectionSign = CustomChat.INSTANCE.enabled && player.hasPermissionLevel(2) && player.isCreative();

            if (shouldSectionSign) {
                stringHash = String.join("",
                        Arrays.stream(stringHash.split(""))
                                .map((letter) -> "§" + letter)
                                .toArray(String[]::new)
                );
            }

            final String[] restArguments = Arrays.copyOfRange(arguments, 1, arguments.length);
            final String toSend = prefix +
                    arguments[0] +
                    " " +
                    stringHash +
                    (shouldSectionSign ? "§r" : "") +
                    " " +
                    String.join(" ", restArguments);

            Chat.sendChatMessage(toSend);
        } catch (NoSuchAlgorithmException err) {
            ChatUtils.errorPrefix("Validation", "Something went wrong: [hl]%s", err.toString());
        }

        return Command.SINGLE_SUCCESS;
    }

    // Why formatted differently?
    public static int fnfboyfriend (String command) {
        final String prefix = botinfo.fnfboyfriend.prefix;
        final String key = botinfo.fnfboyfriend.key;

        if (key == null)
            throw new RuntimeException("The key of the bot is unspecified (null), did you incorrectly add it to your config?");

        try {
            String[] arguments = command.split(" ");

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            long currentTime = System.currentTimeMillis() / 1_000;

            String input = currentTime + key;

            byte[] hash = digest.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            final String[] restArguments = Arrays.copyOfRange(arguments, 1, arguments.length);
            final String result = hexString.substring(0, 16);

            Chat.sendChatMessage(prefix + arguments[0] + " " + result + " " + String.join(" ", restArguments));
        } catch (NoSuchAlgorithmException err) {
            ChatUtils.errorPrefix("Validation", "Something went wrong: [hl]%s", err.toString());
        }

        return Command.SINGLE_SUCCESS;
    }

    public static int kittycorp (String command) throws RuntimeException {
        final String prefix = botinfo.kittycorp.prefix;
        final String key = botinfo.kittycorp.key;

        if (key == null)
            throw new RuntimeException("The key of the bot is unspecified (null), did you incorrectly add it to your config?");

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            String time = String.valueOf(System.currentTimeMillis() / 10_000);

            String input = prefix + command.replaceAll("&[0-9a-fklmnorx]", "") + ";" + time + ";" + key;

            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            BigInteger bigInt = new BigInteger(1, Arrays.copyOfRange(hash, 0, 4));
            String stringHash = bigInt.toString(Character.MAX_RADIX);

            Chat.sendChatMessage(prefix + command + " " + stringHash, true);
        } catch (NoSuchAlgorithmException err) {
            ChatUtils.errorPrefix("Validation", "Something went wrong: [hl]%s", err.toString());
        }

        return Command.SINGLE_SUCCESS;
    }
}
