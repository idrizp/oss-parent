package me.idriz.oss.menu.template;


import me.idriz.oss.menu.Menu;
import me.idriz.oss.menu.MenuItem;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class MenuTemplate {

    private Map<Character, MenuItem> characterToItemMap = new HashMap<>();

    private final String joined;

    private MenuTemplate(String... arr) {
        this.joined = Arrays.stream(arr).collect(Collectors.joining());
    }

    public static MenuTemplate create(String... arr) {
        return new MenuTemplate(arr);
    }

    public MenuTemplate where(char character, MenuItem item) {
        characterToItemMap.put(character, item);
        return this;
    }

    public MenuTemplate apply(Menu menu) {
        for(int i = 0; i < joined.length(); i++) {
            char character = joined.charAt(i);
            if(character == ' ' || character == '_')
                continue;
            else {
                if(characterToItemMap.get(character) == null) menu.getItems().put(i, null);
                else menu.setItem(i, characterToItemMap.get(character));
            }
        }
        return this;
    }
}
