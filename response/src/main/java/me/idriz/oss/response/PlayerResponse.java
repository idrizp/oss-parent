package me.idriz.oss.response;

import org.bukkit.entity.Player;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public final class PlayerResponse {

    private static final Map<Player, PlayerResponse> responseMap = new ConcurrentHashMap<>();

    public static Map<Player, PlayerResponse> getResponseMap() {
        return responseMap;
    }

    private final Player  target;
    private PlayerResponse next;
    private final Consumer<String> responseConsumer;
    private final boolean isChained;

    private PlayerResponse(Player target, Consumer<String> responseConsumer, boolean isChained) {
        this.target = target;
        this.responseConsumer = responseConsumer;
        this.isChained = isChained;
        if(!isChained) responseMap.put(target, this);
    }

    private PlayerResponse(Player target, Consumer<String> responseConsumer) {
        this(target, responseConsumer, false);
    }


    public static PlayerResponse start(Player target, Consumer<String> responseConsumer) {
        return new PlayerResponse(target, responseConsumer);
    }

    public PlayerResponse chain(Consumer<String> consumer) {
        this.next = new PlayerResponse(target, consumer, true);
        return next;
    }

    public Player getTarget() {
        return target;
    }

    public PlayerResponse getNext() {
        return next;
    }

    public void setNext(PlayerResponse next) {
        this.next = next;
    }

    public Consumer<String> getResponseConsumer() {
        return responseConsumer;
    }

    public boolean isChained() {
        return isChained;
    }
}
