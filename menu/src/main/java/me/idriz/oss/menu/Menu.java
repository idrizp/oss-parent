package me.idriz.oss.menu;

import me.idriz.oss.menu.listener.MenuListener;
import me.idriz.oss.menu.template.MenuTemplate;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;

public interface Menu {

    static final Map<Player, Menu> MENUS = new HashMap<>();

    Inventory getInventory();

    Map<Integer, MenuItem> getItems();

    MenuTemplate getMenuTemplate();

    void setMenuTemplate(MenuTemplate template);

    void setItem(int slot, MenuItem item);

    void addItem(MenuItem item);

    static void show(Player player, Menu menu) {
        close(player);
        player.openInventory(menu.getInventory());
        MENUS.put(player, menu);
    }

    static void close(Player player) {
        if(MENUS.containsKey(player)) {
            player.closeInventory();
            MENUS.remove(player);
        }
    }

    static void init(Plugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(new MenuListener(), plugin);
    }

}
