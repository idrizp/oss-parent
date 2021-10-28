package me.idriz.oss.menu.impl;

import java.util.function.Consumer;
import me.idriz.oss.menu.Menu;
import me.idriz.oss.menu.MenuItem;
import me.idriz.oss.menu.PaginatedMenu;
import me.idriz.oss.menu.template.MenuTemplate;
import me.idriz.oss.menu.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class SimplePaginatedMenu implements PaginatedMenu {

    private final String title;
    private final int rows;
    private Map<Integer, PaginatedMenu> pages = new HashMap<>();
    private Map<Integer, MenuItem> items = new HashMap<>();
    private int currentPage = 0;
    private Function<PaginatedMenu, String> function;
    private int backPageItemSlot, nextPageItemSlot;
    private ItemStack nextPageItem, backPageItem;

    private Inventory inventory;
    private boolean isPage;

    private MenuTemplate menuTemplate;
    private Consumer<InventoryClickEvent> itemMoveConsumer = e -> e.setCancelled(true);
    private Function<PaginatedMenu, String> pageTitle;

    public SimplePaginatedMenu(String title, int rows) {
        this(title, 0, rows, false);
    }

    private SimplePaginatedMenu(String title, int page, int rows, boolean isPage) {
        this.title = title;
        this.rows = rows;

        this.isPage = isPage;
        this.currentPage = page;

        this.nextPageItem = ItemBuilder.create(Material.ARROW).withName(ChatColor.AQUA + ">>").build();
        this.backPageItem = ItemBuilder.create(Material.ARROW).withName(ChatColor.RED + "<<").build();

        this.nextPageItemSlot = (rows * 9) - 1;
        this.backPageItemSlot = (rows * 9) - 9;

        this.inventory = Bukkit.createInventory(null, (9 * rows), title);
        if (!isPage) pages.put(currentPage, this);


    }

    public void onItemMove(Consumer<InventoryClickEvent> itemMoveConsumer) {
        this.itemMoveConsumer = itemMoveConsumer;
    }

    @Override
    public Map<Integer, PaginatedMenu> getPages() {
        return pages;
    }

    @Override
    public void setPages(Map<Integer, PaginatedMenu> pages) {
        this.pages = pages;
    }

    @Override
    public PaginatedMenu getPage(int page) {
        return pages.get(page);
    }

    @Override
    public String getPageTitle() {
        return this.pageTitle.apply(this);
    }

    @Override
    public void setPageTitle(Function<PaginatedMenu, String> pageTitle) {
        this.pageTitle = pageTitle;
    }

    @Override
    public int getCurrentPage() {
        return currentPage;
    }

    @Override
    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    @Override
    public ItemStack getNextPageItem() {
        return nextPageItem;
    }

    @Override
    public void setNextPageItem(ItemStack nextPageItem) {
        this.nextPageItem = nextPageItem;
    }

    @Override
    public ItemStack getBackPageItem() {
        return backPageItem;
    }

    @Override
    public void setBackPageItem(ItemStack backPageItem) {
        this.backPageItem = backPageItem;
    }

    @Override
    public int getNextPageItemSlot() {
        return nextPageItemSlot;
    }

    @Override
    public void setNextPageItemSlot(int nextPageItemSlot) {
        this.nextPageItemSlot = nextPageItemSlot;
    }

    @Override
    public int getBackPageItemSlot() {
        return backPageItemSlot;
    }

    @Override
    public void setBackPageItemSlot(int backPageItemSlot) {
        this.backPageItemSlot = backPageItemSlot;
    }

    @Override
    public Inventory getInventory() {
        if(isPage && pageTitle != null) {
            ItemStack[] contents = inventory.getContents();
            inventory = Bukkit.createInventory(null, (rows * 9), pageTitle.apply(this));
            inventory.setContents(contents);
            return inventory;
        }
        return inventory;
    }

    @Override
    public Map<Integer, MenuItem> getItems() {
        return items;
    }

    @Override
    public MenuTemplate getMenuTemplate() {
        return menuTemplate;
    }

    @Override
    public void onItemMove(InventoryClickEvent event) {
        itemMoveConsumer.accept(event);
    }


    @Override
    public void setMenuTemplate(MenuTemplate template) {
        this.menuTemplate = template;
        menuTemplate.apply(this);
    }

    private void forceSetItem(int slot, MenuItem item) {
        items.put(slot, item);
        inventory.setItem(slot, item.getItem());
    }

    @Override
    public void setItem(int slot, MenuItem item) {
        if (slot > rows * 9) {
            if (pages.get(currentPage + 1) != null) {
                pages.get(currentPage + 1).setItem(slot - rows * 9, item);
                return;
            }
            SimplePaginatedMenu newPage = new SimplePaginatedMenu(title, currentPage + 1, rows, true);

            pages.put(currentPage + 1, newPage);

            newPage.setPageTitle(pageTitle);
            newPage.setPages(this.pages);
            newPage.setNextPageItemSlot(nextPageItemSlot);
            newPage.setBackPageItemSlot(backPageItemSlot);
            if(menuTemplate != null) newPage.setMenuTemplate(menuTemplate);
            newPage.addItem(item);

            forceSetItem(nextPageItemSlot, new MenuItem(nextPageItem, player -> {
                Menu.show(player, newPage);
            }));

            newPage.forceSetItem(backPageItemSlot, new MenuItem(backPageItem, clicker -> {
                Menu.show(clicker, this);
            }));

            return;
        } else {
            inventory.setItem(slot, item.getItem());
            items.put(slot, item);
        }
    }

    @Override
    public void addItem(MenuItem item) {
        for (int i = 0; i < inventory.getSize(); i++) {
            if (!items.containsKey(i)) {
                setItem(i, item);
                return;
            }
        }
        if (pages.get(currentPage + 1) == null) {
            setItem((rows * 9) + 1, item);
            return;
        }
        pages.get(currentPage + 1).addItem(item);
    }

    private boolean isEqual(ItemStack first, ItemStack next) {
        return first.getType() == next.getType() && first.hasItemMeta() == next.hasItemMeta() && first.getItemMeta().equals(next.getItemMeta());
    }
}
