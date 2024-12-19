package land.chipmunk.chipmunkmod.command.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import land.chipmunk.chipmunkmod.util.player.ChatUtils;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.CreativeInventoryActionC2SPacket;

import java.util.Objects;

import static land.chipmunk.chipmunkmod.command.CommandManager.literal;
import static land.chipmunk.chipmunkmod.command.CommandManager.argument;
import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static com.mojang.brigadier.arguments.IntegerArgumentType.getInteger;
import static net.minecraft.command.argument.ItemStackArgumentType.itemStack;
import static net.minecraft.command.argument.ItemStackArgumentType.getItemStackArgument;

public class ItemCommand {

    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess) {
        dispatcher.register(
                literal("item")
                        .then(argument("item", itemStack(commandRegistryAccess))
                                .executes(context -> setItem(context, 1))  // Default count to 1 if unspecified
                                .then(argument("count", integer(1, 512))
                                        .executes(ItemCommand::setItem)
                                )
                        )
        );
    }

    public static int setItem(CommandContext<FabricClientCommandSource> context) throws CommandSyntaxException {
        int count = getInteger(context, "count");
        return setItem(context, count);
    }

    public static int setItem(CommandContext<FabricClientCommandSource> context, int count) throws CommandSyntaxException {
        FabricClientCommandSource source = context.getSource();
        MinecraftClient client = source.getClient();

        ItemStack stack = getItemStackArgument(context, "item").createStack(count, false);
        assert client.player != null;
        int slot = 36 + client.player.getInventory().selectedSlot;  // Get player's currently selected hotbar slot

        // Send packet to server to update the inventory with the new item stack
        Objects.requireNonNull(client.getNetworkHandler()).getConnection().send(new CreativeInventoryActionC2SPacket(slot, stack));

        ChatUtils.infoPrefix("Item", "Replaced your held item with [hl]%s %s", count, stack.toHoverableText());

        return Command.SINGLE_SUCCESS;
    }
}
