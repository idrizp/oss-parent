package me.idriz.oss.redis.bus;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import me.idriz.oss.redis.RedisManager;
import me.idriz.oss.redis.bus.annotation.RedisHandler;
import me.idriz.oss.redis.bus.payload.Payload;
import org.apache.commons.lang.ArrayUtils;
import org.bukkit.plugin.java.JavaPlugin;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class RedisBus {

    private final Gson gson;
    private final Multimap<Object, Method> listeners = ArrayListMultimap.create();
    private final Set<String> registeredChannels = new HashSet<>();
    private JedisPool jedisPool;
    private final JavaPlugin plugin;

    public RedisBus(JedisPool jedisPool, JavaPlugin plugin) {
        this.gson = RedisManager.getGson();
        try {
            this.jedisPool = jedisPool;
        } catch(CompletionException e) {
            plugin.getLogger().log(Level.SEVERE, "Plugin disabled due to mis-configuration of redis.");
            plugin.getServer().getPluginManager().disablePlugin(plugin);
        }
        this.plugin = plugin;
    }

    public Gson getGson() {
        return gson;
    }


    public void registerListener(Object object) {
        if (Arrays.stream(object.getClass().getDeclaredMethods()).noneMatch(method -> method.isAnnotationPresent(RedisHandler.class)))
            return;
        Set<Method> methods = Arrays.stream(object.getClass().getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(RedisHandler.class))
                .filter(method -> method.getParameters().length == 1)
                .collect(Collectors.toSet());

        methods.forEach(method -> registeredChannels.addAll(Arrays.asList(method.getAnnotation(RedisHandler.class).value())));
        listeners.putAll(object, methods);
    }

    public Multimap<Object, Method> getListeners() {
        return listeners;
    }

    public Set<String> getRegisteredChannels() {
        return registeredChannels;
    }

    public JedisPool getJedisPool() {
        return jedisPool;
    }



    public void publishPayload(String channel, Payload payload) {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.publish(channel, gson.toJson(payload));
        }
    }

    public void init() {
        CompletableFuture.runAsync(() -> {
            try (Jedis jedis = jedisPool.getResource()) {
                jedis.subscribe(new JedisPubSub() {

                    @Override
                    public void onSubscribe(String channel, int subscribedChannels) {
                        System.out.println("REDIS: Subscribed to channel: " + channel);
                    }

                    @Override
                    public void onMessage(String channel, String message) {
                        getListeners().entries()
                                .stream()
                                .filter(entry -> ArrayUtils.contains(entry.getValue().getAnnotation(RedisHandler.class).value(), channel))
                                .forEach(entry -> {
                                    try {
                                        entry.getValue().invoke(entry.getKey(), gson.fromJson(message, entry.getValue().getParameterTypes()[0]));
                                    } catch (IllegalAccessException | InvocationTargetException e) {
                                        e.printStackTrace();
                                    }
                                });
                    }
                }, getRegisteredChannels().toArray(new String[0]));
            }
        }).exceptionally(throwable -> {
            throwable.printStackTrace();
            plugin.getLogger().log(Level.SEVERE, "Plugin disabled due to mis-configuration of Redis.");
            plugin.getServer().getPluginManager().disablePlugin(plugin);
            return null;
        });
    }

}
