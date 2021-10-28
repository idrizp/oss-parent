package me.idriz.oss.menu.listener;


import me.idriz.oss.menu.Menu;
import me.idriz.oss.menu.MenuItem;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class MenuListener implements Listener {

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        Menu.MENUS.remove((Player) e.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Menu.MENUS.remove(e.getPlayer());
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if(Menu.MENUS.containsKey((Player) e.getWhoClicked())) {
            Player player = (Player) e.getWhoClicked();
            Menu menu = Menu.MENUS.get(player);

            if(menu.getItems().containsKey(e.getRawSlot())) {
                MenuItem item = menu.getItems().get(e.getRawSlot());
                if(item == null) {
                    if (!e.getAction().name().startsWith("PLACE")) {
                        return;
                    }
                    menu.onItemMove(e);
                    return;
                }
                if(item.isCancel()) {
                    e.setCancelled(true);
                }
                item.getClickConsumer().accept(player);
            }
        }
    }

}
