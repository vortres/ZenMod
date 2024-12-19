package land.chipmunk.chipmunkmod.command.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import land.chipmunk.chipmunkmod.modules.SelfCare;
import land.chipmunk.chipmunkmod.util.player.ChatUtils;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;

import static com.mojang.brigadier.arguments.BoolArgumentType.bool;
import static com.mojang.brigadier.arguments.BoolArgumentType.getBool;
import static land.chipmunk.chipmunkmod.command.CommandManager.argument;
import static land.chipmunk.chipmunkmod.command.CommandManager.literal;

public class SelfCareCommand {
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(
                literal("selfcare")
                        .then(createCommand("op"))
                        .then(createCommand("gamemode"))
                        .then(createCommand("cspy"))
                        .then(createCommand("icu"))
        );
    }

    private static LiteralArgumentBuilder<FabricClientCommandSource> createCommand(String settingType) {
        return literal(settingType)
                .then(argument("enabled", bool())
                        .executes(context -> updateSelfCareSetting(context, settingType))
                );
    }

    public static int updateSelfCareSetting(CommandContext<FabricClientCommandSource> context, String settingType) {
        boolean isEnabled = getBool(context, "enabled");

        switch (settingType) {
            case "op" -> {
                SelfCare.INSTANCE.opEnabled = isEnabled;
                sendFeedback("op", isEnabled);
            }
            case "gamemode" -> {
                SelfCare.INSTANCE.gamemodeEnabled = isEnabled;
                sendFeedback("gamemode", isEnabled);
            }
            case "cspy" -> {
                SelfCare.INSTANCE.cspyEnabled = isEnabled;
                sendFeedback("CommandSpy", isEnabled);
            }
            case "icu" -> {
                SelfCare.INSTANCE.icuEnabled = isEnabled;
                sendFeedback("iControlU", isEnabled);
            }
        }

        return Command.SINGLE_SUCCESS;
    }

    private static void sendFeedback(String feature, boolean isEnabled) {
        ChatUtils.infoPrefix("Self Care", "The [hl]%s[def] self-care is now [hl]%s[def]", feature, isEnabled ? "on" : "off");
    }
}
