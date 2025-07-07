package org.example;

import io.vertx.core.Vertx;
import io.vertx.redis.client.Redis;
import io.vertx.redis.client.RedisAPI;
import io.vertx.redis.client.RedisOptions;

/**
 * Service class to create and expose RedisAPI instance.
 */
public class RedisService {
    private final RedisAPI redisAPI;

    public RedisService(Vertx vertx) {
        // 🔗 Connect to local Redis server on port 6379
        Redis client = Redis.createClient(vertx, new RedisOptions()
                .setConnectionString("redis://localhost:6379"));

        this.redisAPI = RedisAPI.api(client);
    }

    public RedisAPI getRedis() {
        return redisAPI;
    }
}
