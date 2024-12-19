package land.chipmunk.chipmunkmod.modules;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import land.chipmunk.chipmunkmod.data.MutablePlayerListEntry;
import land.chipmunk.chipmunkmod.listeners.Listener;
import land.chipmunk.chipmunkmod.listeners.ListenerManager;
import land.chipmunk.chipmunkmod.mixin.ClientPlayNetworkHandlerAccessor;
import land.chipmunk.chipmunkmod.mixin.PlayerListEntryAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.CommandSuggestionsS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRemoveS2CPacket;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static land.chipmunk.chipmunkmod.util.network.ServerUtil.serverHasCommand;

public class Players extends Listener {
    public List<MutablePlayerListEntry> list = new ArrayList<>();

    public static Players INSTANCE = new Players(MinecraftClient.getInstance());

    private final MinecraftClient client;

    public Players (MinecraftClient client) {
        this.client = client;
        ListenerManager.addListener(this);

        TabComplete.INSTANCE.init();
    }

    public void init () {}

    @Override
    public void packetReceived (Packet<?> packet) {
        if (packet instanceof PlayerListS2CPacket) packetReceived((PlayerListS2CPacket) packet);
        else if (packet instanceof PlayerRemoveS2CPacket) packetReceived((PlayerRemoveS2CPacket) packet);
    }

    public void packetReceived (PlayerListS2CPacket packet) {
        try {
            for (PlayerListS2CPacket.Action action : packet.getActions()) {
                for (PlayerListS2CPacket.Entry entry : packet.getEntries()) {
                    if (action == PlayerListS2CPacket.Action.ADD_PLAYER) addPlayer(entry);
//                else if (action == PlayerListS2CPacket.Action.INITIALIZE_CHAT) initializeChat(entry);
                    else if (action == PlayerListS2CPacket.Action.UPDATE_GAME_MODE) updateGamemode(entry);
//                else if (action == PlayerListS2CPacket.Action.UPDATE_LISTED) updateListed(entry);
                    else if (action == PlayerListS2CPacket.Action.UPDATE_LATENCY) updateLatency(entry);
                    else if (action == PlayerListS2CPacket.Action.UPDATE_DISPLAY_NAME) updateDisplayName(entry);
                }
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

    public void packetReceived (PlayerRemoveS2CPacket packet) {
        try {
            for (UUID uuid : packet.profileIds()) {
                removePlayer(uuid);
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

    public final MutablePlayerListEntry getEntry (UUID uuid) {
        try {
            for (MutablePlayerListEntry candidate : list) {
                if (candidate.profile.getId().equals(uuid)) {
                    return candidate;
                }
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }

        return null;
    }

    public final MutablePlayerListEntry getEntry (String username) {
        for (MutablePlayerListEntry candidate : list) {
            if (candidate.profile.getName().equals(username)) {
                return candidate;
            }
        }

        return null;
    }

    public final MutablePlayerListEntry getEntry (Text displayName) {
        for (MutablePlayerListEntry candidate : list) {
            if (candidate.displayName != null && candidate.displayName.equals(displayName)) {
                return candidate;
            }
        }

        return null;
    }

    private MutablePlayerListEntry getEntry (PlayerListS2CPacket.Entry other) {
        return getEntry(other.profileId());
    }

    private void addPlayer (PlayerListS2CPacket.Entry newEntry) {
        try {
            final MutablePlayerListEntry duplicate = getEntry(newEntry);
            if (duplicate != null) {
                removeFromPlayerList(duplicate.profile.getId());
                list.remove(duplicate);
            }

            final MutablePlayerListEntry entry = new MutablePlayerListEntry(newEntry);

            list.add(entry);
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

    private void updateGamemode (PlayerListS2CPacket.Entry newEntry) {
        try {
            final MutablePlayerListEntry target = getEntry(newEntry);
            if (target == null) return;

            target.gamemode = newEntry.gameMode();

            final ClientPlayNetworkHandlerAccessor accessor = ((ClientPlayNetworkHandlerAccessor) MinecraftClient.getInstance().getNetworkHandler());

            if (accessor == null) return;

            final PlayerListEntryAccessor entryAccessor = (PlayerListEntryAccessor) accessor.playerListEntries().get(newEntry.profileId());

            entryAccessor.setGameMode(newEntry.gameMode());
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

    private void updateLatency (PlayerListS2CPacket.Entry newEntry) {
        final MutablePlayerListEntry target = getEntry(newEntry);
        if (target == null) return;

        target.latency = newEntry.latency();

        final ClientPlayNetworkHandlerAccessor accessor = ((ClientPlayNetworkHandlerAccessor) MinecraftClient.getInstance().getNetworkHandler());

        if (accessor == null) return;

        final PlayerListEntryAccessor entryAccessor = (PlayerListEntryAccessor) accessor.playerListEntries().get(newEntry.profileId());

        entryAccessor.setLatency(newEntry.latency());
    }

    private void updateDisplayName (PlayerListS2CPacket.Entry newEntry) {
        final MutablePlayerListEntry target = getEntry(newEntry);
        if (target == null) return;

        target.displayName = newEntry.displayName();

        final ClientPlayNetworkHandlerAccessor accessor = ((ClientPlayNetworkHandlerAccessor) MinecraftClient.getInstance().getNetworkHandler());

        if (accessor == null) return;

        accessor.playerListEntries().get(newEntry.profileId()).setDisplayName(newEntry.displayName());
    }

    private void removePlayer (UUID uuid) {
        try {
            final MutablePlayerListEntry target = getEntry(uuid);
            if (target == null) return;

            if (!serverHasCommand("scoreboard")) {
                removeFromPlayerList(uuid);
                return;
            }

            final CompletableFuture<CommandSuggestionsS2CPacket> future = TabComplete.INSTANCE.complete("/scoreboard players add ");

            if (future == null) return;

            future.thenApply(packet -> {
                final Suggestions matches = packet.getSuggestions();
                final String username = target.profile.getName();

                for (int i = 0; i < matches.getList().size(); i++) {
                    final Suggestion suggestion = matches.getList().get(i);

                    final Message tooltip = suggestion.getTooltip();
                    if (tooltip != null || !suggestion.getText().equals(username)) continue;

                    return true;
                }

                list.remove(target);

                removeFromPlayerList(uuid);

                for (MutablePlayerListEntry entry : list) {
                    if (!entry.profile.getId().equals(uuid)) continue;

                    addToPlayerList(new PlayerListEntry(entry.profile, false));
                }

                return true;
            });
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

    public void addToPlayerList (PlayerListEntry entry) {
        client.getSocialInteractionsManager().setPlayerOnline(entry);

        final ClientPlayNetworkHandlerAccessor accessor = ((ClientPlayNetworkHandlerAccessor) MinecraftClient.getInstance().getNetworkHandler());

        if (accessor == null) return;

        accessor.playerListEntries().put(entry.getProfile().getId(), entry);

        accessor.listedPlayerListEntries().add(entry);
    }

    private void removeFromPlayerList (UUID uuid) {
        client.getSocialInteractionsManager().setPlayerOffline(uuid);

        final ClientPlayNetworkHandlerAccessor accessor = ((ClientPlayNetworkHandlerAccessor) MinecraftClient.getInstance().getNetworkHandler());

        if (accessor == null) return;

        final PlayerListEntry playerListEntry = accessor.playerListEntries().remove(uuid);

        if (playerListEntry != null) {
            accessor.listedPlayerListEntries().remove(playerListEntry);
        }
    }
}
