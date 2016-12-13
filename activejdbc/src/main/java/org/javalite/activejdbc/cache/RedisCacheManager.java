package org.javalite.activejdbc.cache;


import org.javalite.activejdbc.InitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.*;
import java.util.Properties;

import static org.javalite.common.Convert.toInteger;

/**
 * Redis cache manager. Will store caches in Redis server. By default will connect to Redis running on
 * local host.
 * <p>
 * If redis server is located elsewhere, provide a property file called <code>activejdbc-redis.properties</code>
 * with two properties: <code>redist-host</code> and <code>redist-port</code>.
 * </p>
 * <p>
 * The properties file needs to be at the root of classpath.
 * </p>
 * <p><strong>Limitation:</strong> Does not support {@link #flush(CacheEvent)} with value 'ALL'.</p>
 *
 * @author Igor Polevoy on 12/7/15.
 */
public class RedisCacheManager extends CacheManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(RedisCacheManager.class);

    private JedisPool jedisPool;


    public RedisCacheManager() {
        Properties props = new Properties();
        InputStream in = getClass().getResourceAsStream("/activejdbc-redis.properties");
        if (in == null) {
            jedisPool = new JedisPool("localhost");
        } else {
            try {
                props.load(in);
                String host = props.getProperty("redis-host");
                String port = props.getProperty("redis-port");
                jedisPool = new JedisPool(host, toInteger(port));
            } catch (Exception e) {
                throw new InitException("Failed to configure connection to Redis server", e);
            }
        }
    }

    @Override
    public Object getCache(String group, String key) {
        try (Jedis jedis = jedisPool.getResource()){
            byte[] bytes = jedis.hget(group.getBytes(), key.getBytes());

            if (bytes == null) {
                return null;
            } else {
                ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes));
                return in.readObject();
            }

        } catch (Exception e) {
            throw new CacheException("Failed to read object from Redis", e);
        }
    }

    @Override
    public void addCache(String group, String key, Object cache) {
        try (Jedis jedis = jedisPool.getResource()){
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            ObjectOutput objectOutput = new ObjectOutputStream(bout);
            objectOutput.writeObject(cache);
            objectOutput.close();
            byte[] bytes = bout.toByteArray();
            jedis.hset(group.getBytes(), key.getBytes(), bytes);

        } catch (Exception e) {
            LOGGER.error("Failed to add object to cache with group: " + group + " and key: " + key, e);
        }
    }

    @Override
    public void doFlush(CacheEvent event) {
        if (event.getType().equals(CacheEvent.CacheEventType.ALL)) {
            throw new UnsupportedOperationException("Flushing all caches not supported");
        } else if (event.getType().equals(CacheEvent.CacheEventType.GROUP)) {

            try(Jedis jedis = jedisPool.getResource()){
                jedis.del(event.getGroup().getBytes());
            }
        }
    }

    @Override
    public Object getImplementation() {
        return this.jedisPool;
    }
}
