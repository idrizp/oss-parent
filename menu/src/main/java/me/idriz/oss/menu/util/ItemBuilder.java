package me.idriz.oss.menu.util;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Arrays;
import java.util.List;

public final class ItemBuilder {

    private ItemStack item;
    private ItemMeta meta;

    private ItemBuilder(ItemStack item) {
        this.item = item;
        this.meta = item.getItemMeta();
    }

    public static ItemBuilder create(ItemStack item) {
        return new ItemBuilder(item);
    }

    public static ItemBuilder create(Material material, int amount) {
        return new ItemBuilder(new ItemStack(material, amount));
    }

    public static ItemBuilder create(Material material) {
        return create(material, 1);
    }

    public ItemBuilder withName(String name) {
        meta.setDisplayName(name);
        return this;
    }

    public ItemBuilder withLore(List<String> lore) {
        meta.setLore(lore);
        return this;
    }

    public ItemBuilder withLore(String... lore) {
        withLore(Arrays.asList(lore));
        return this;
    }

    public ItemBuilder withLoreLine(String line) {
        if(meta.getLore() == null || meta.getLore().isEmpty()) {
            withLore(line);
            return this;
        }
        List<String> currentLore = meta.getLore();
        currentLore.add(line);
        meta.setLore(currentLore);
        return this;
    }

    /**
     * This was removed because in 1.15 you can now use {@link Material} enumerations instead.
     * @param color
     * @return
     */
    @Deprecated
    public ItemBuilder withDyeColor(DyeColor color) {
        return this;
    }

    public ItemBuilder setSkullOwner(String skullOwner) {
        if(meta instanceof SkullMeta) {
            ((SkullMeta) meta).setOwner(skullOwner);
            return this;
        }
        return this;
    }

    public ItemBuilder unbreakable() {
        meta.setUnbreakable(true);
        return this;
    }

    public ItemStack build() {
        item.setItemMeta(meta);
        return item;
    }

}

