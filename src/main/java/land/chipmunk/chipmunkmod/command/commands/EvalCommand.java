package land.chipmunk.chipmunkmod.command.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import land.chipmunk.chipmunkmod.util.player.ChatUtils;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.JsePlatform;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.greedyString;
import static land.chipmunk.chipmunkmod.command.CommandManager.argument;
import static land.chipmunk.chipmunkmod.command.CommandManager.literal;

public class EvalCommand {
    public static void register (CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(
                literal("eval")
                        .then(
                                argument("code", greedyString())
                                    .executes(EvalCommand::eval)
                        )
        );
    }

    public static int eval (CommandContext<FabricClientCommandSource> context) {
        final String code = getString(context, "code");

        try {
            final Globals globals = JsePlatform.standardGlobals();

            globals.set("client", CoerceJavaToLua.coerce(MinecraftClient.getInstance()));
            globals.set("context", CoerceJavaToLua.coerce(context));
            globals.set("class", CoerceJavaToLua.coerce(Class.class));

            LuaValue chunk = globals.load(code);

            ChatUtils.infoPrefix("Eval", "%s", chunk.call().toString());
        } catch (Exception e) {
            ChatUtils.infoPrefix("Eval", "Error: [hl]%s", e.toString());
        }

        return Command.SINGLE_SUCCESS;
    }
}
