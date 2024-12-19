package land.chipmunk.chipmunkmod.modules;

import com.google.common.hash.Hashing;
import land.chipmunk.chipmunkmod.ChipmunkMod;
import land.chipmunk.chipmunkmod.listeners.Listener;
import land.chipmunk.chipmunkmod.listeners.ListenerManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.minecraft.text.PlainTextContent;
import net.minecraft.text.Text;
import net.minecraft.text.TextContent;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class QBotAuth extends Listener {
    public final String ID = "qbot_verify";

    public static final QBotAuth INSTANCE = new QBotAuth();

    public QBotAuth () {
        ListenerManager.addListener(this);
    }

    public void init () {}

    @Override
    public void chatMessageReceived(Text text) {
        final String authKey = ChipmunkMod.CONFIG.bots.chomens.authKey;
        if (authKey == null) return;

        final TextContent message = text.getContent();
        if (!(message instanceof PlainTextContent)) return;

        final String ID = ((PlainTextContent) message).string();
        if (!ID.equals(this.ID)) return;

        final List<Text> children = text.getSiblings();

        if (children.size() != 2) return;
        if (!(children.getFirst().getContent() instanceof PlainTextContent)) return;

        final String hash = ((PlainTextContent) children.getFirst().getContent()).string();
        final long time = System.currentTimeMillis() / 10_000;

        final String actual = Hashing.sha256()
                // very pro hash input
                .hashString(authKey + time, StandardCharsets.UTF_8)
                .toString()
                .substring(0, 8);

        if (!hash.equals(actual)) return;
        if (!(children.get(1).getContent() instanceof PlainTextContent)) return;

        final String selector = ((PlainTextContent) children.get(1).getContent()).string();

        final String toSendHash = Hashing.sha256()
                // very pro hash input
                .hashString(authKey + authKey + time + time, StandardCharsets.UTF_8)
                .toString()
                .substring(0, 8);

        final Component toSend = Component.text(ID)
                .append(Component.text(toSendHash));

        final String toSendString = GsonComponentSerializer.gson().serialize(toSend);

        CommandCore.INSTANCE.run("minecraft:tellraw " + selector + " " + toSendString);
        CustomChat.INSTANCE.resetTotal();
    }
}
