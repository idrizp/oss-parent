package me.idriz.oss.menu;

import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.function.Function;

public interface PaginatedMenu extends Menu {

    Map<Integer, PaginatedMenu> getPages();

    PaginatedMenu getPage(int page);

    String getPageTitle();

    void setPageTitle(Function<PaginatedMenu, String> paginatedTitle);

    int getCurrentPage();

    void setCurrentPage(int currentPage);

    ItemStack getNextPageItem();

    ItemStack getBackPageItem();

    void setNextPageItem(ItemStack nextPageItem);

    void setBackPageItem(ItemStack backPageItem);

    int getNextPageItemSlot();

    int getBackPageItemSlot();

    void setNextPageItemSlot(int nextPageItemSlot);

    void setBackPageItemSlot(int backPageItemSlot);

    void setPages(Map<Integer, PaginatedMenu> pages);
}
