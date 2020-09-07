package me.idriz.oss.response;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

public final class PlayerResponseHandler implements Listener {

    private PlayerResponseHandler() {}

    public static void init(Plugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(new PlayerResponseHandler(), plugin);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChat(AsyncPlayerChatEvent e) {
        if(PlayerResponse.getResponseMap().containsKey(e.getPlayer())) {

            e.setCancelled(true);
            PlayerResponse response = PlayerResponse.getResponseMap().get(e.getPlayer());
            response.getResponseConsumer()
                    .accept(e.getMessage());

            if(response.getNext() != null) {
                PlayerResponse.getResponseMap().put(e.getPlayer(), response.getNext());
                return;
            }

                PlayerResponse.getResponseMap().remove(e.getPlayer());

        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        PlayerResponse.getResponseMap().remove(e.getPlayer());
    }


}
