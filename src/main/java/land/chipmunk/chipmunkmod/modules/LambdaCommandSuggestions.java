package land.chipmunk.chipmunkmod.modules;

import land.chipmunk.chipmunkmod.ChipmunkMod;
import land.chipmunk.chipmunkmod.data.LambdaBotCommand;
import land.chipmunk.chipmunkmod.listeners.Listener;
import land.chipmunk.chipmunkmod.listeners.ListenerManager;
import land.chipmunk.chipmunkmod.util.player.UUIDUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.PlainTextContent;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

import static land.chipmunk.chipmunkmod.ChipmunkMod.MCInstance;

public class LambdaCommandSuggestions extends Listener {
    public static final String ID = "lambda_request_command_suggestion";

    public static LambdaCommandSuggestions INSTANCE = new LambdaCommandSuggestions(MCInstance);
    private final MinecraftClient client;

    public List<LambdaBotCommand> commands = new ArrayList<>();

    public LambdaCommandSuggestions(MinecraftClient client) {
        this.client = client;

        ListenerManager.addListener(this);
    }

    public void init () {}

    @Override
    public void coreMoved () { forceRequest(); }

    public void forceRequest () {
        final ClientPlayerEntity player = client.player;
        if (player == null) return;

        final String selector = UUIDUtil.selector(player.getUuid());

        final Component component = Component
                .text(ID)
                .append(Component.text(selector));

        final String serialized = GsonComponentSerializer.gson().serialize(component);

        CommandCore.INSTANCE.run("tellraw @a[team=LambdaBot] " + serialized); // I use team cuz i can have selfcare
    }

    @Override
    public void chatMessageReceived(Text message) {
        try {
            final List<Text> children = message.getSiblings();
            if (children.isEmpty()) return;

            final Text textComponent = children.getFirst();
            if (!textComponent.getString().equals(ID)) return;

            commands = children.subList(1, children.size())
                    .stream()
                    .map(
                            (eachComponent) -> {
                                final LambdaBotCommand command = new LambdaBotCommand(
                                        ChipmunkMod.CONFIG.bots.lambda.prefix + ((PlainTextContent) eachComponent.getContent()).string(),
                                        LambdaBotCommand.AccessLevel.valueOf(eachComponent.getSiblings().getFirst().getString())
                                );

                                if (!Boolean.parseBoolean(eachComponent.getSiblings().get(1).getString())) return command;

                                final List<Text> subList = eachComponent.getSiblings().subList(2, eachComponent.getSiblings().size());

                                for (Text aliasComponent : subList) {
                                    final String alias = aliasComponent.getString();

                                    command.aliases.add(alias);
                                }

                                return command;
                            }
                    )
                    .toList();
        } catch (Exception ignored) {}
    }
}
