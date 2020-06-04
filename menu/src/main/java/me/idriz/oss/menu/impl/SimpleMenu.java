package me.idriz.oss.menu.impl;

import me.idriz.oss.menu.Menu;
import me.idriz.oss.menu.MenuItem;
import me.idriz.oss.menu.template.MenuTemplate;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;

public class SimpleMenu implements Menu {

    private Inventory inventory;
    private final String title;
    private final int rows;
    private final Map<Integer, MenuItem> items;
    private MenuTemplate template;

    public SimpleMenu(String title, int rows) {
        this.title = title;
        this.rows = rows;
        this.items = new HashMap<>();
        this.inventory = Bukkit.createInventory(null, rows * 9, title);
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    @Override
    public Map<Integer, MenuItem> getItems() {
        return items;
    }

    @Override
    public MenuTemplate getMenuTemplate() {
        return template;
    }

    @Override
    public void setMenuTemplate(MenuTemplate template) {
        this.template = template;
        template.apply(this);
    }

    @Override
    public void setItem(int slot, MenuItem item) {
        inventory.setItem(slot, item.getItem());
        items.put(slot, item);
    }

    @Override
    public void addItem(MenuItem item) {
        for(int i = 0; i < inventory.getSize(); i++) {
            if(!items.containsKey(i)) {
                setItem(i, item);
                return;
            }
        }
    }
}
