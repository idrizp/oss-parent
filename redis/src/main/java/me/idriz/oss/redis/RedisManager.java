package me.idriz.oss.redis;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.idriz.oss.redis.bus.RedisBus;
import org.bukkit.plugin.java.JavaPlugin;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisException;

public class RedisManager {

    private final JedisPool jedisPool;
    private static Gson gson = new GsonBuilder().create();
    private RedisBus redisBus;

    public RedisManager(JavaPlugin plugin, JedisPool jedisPool) {
        this.jedisPool = jedisPool;
        try(Jedis jedis = jedisPool.getResource()) {
            this.redisBus = new RedisBus(jedisPool, plugin);
        } catch(JedisException e) {
            plugin.getLogger().severe("Plugin disabled due to misconfiguration of redis.");
            plugin.getServer().getPluginManager().disablePlugin(plugin);
        }

    }

    public JedisPool getJedisPool() {
        return jedisPool;
    }

    public RedisBus getRedisBus() {
        return redisBus;
    }

    public static Gson getGson() {
        return gson;
    }

    public static void setGson(Gson gson) {
        RedisManager.gson = gson;
    }

    public void clear() {
        redisBus.getListeners().clear();
        redisBus.getRegisteredChannels().clear();
    }
}
