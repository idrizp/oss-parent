package me.idriz.oss.menu;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

public class MenuItem {

    private final Consumer<Player> clickConsumer;
    private final ItemStack item;
    private boolean cancel;

    public MenuItem(ItemStack item, Consumer<Player> clickConsumer) {
        this.clickConsumer = clickConsumer;
        this.item = item;
        this.cancel = true;
    }

    public MenuItem setCancel(boolean cancel) {
        this.cancel = cancel;
        return this;
    }

    public boolean isCancel() {
        return cancel;
    }

    public Consumer<Player> getClickConsumer() {
        return clickConsumer;
    }

    public ItemStack getItem() {
        return item;
    }
}
