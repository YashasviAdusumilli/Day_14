package org.example;

import io.vertx.core.Vertx;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.core.json.JsonObject;

public class MongoService {

    private final MongoClient mongoClient;

    public MongoService(Vertx vertx) {
        // 📦 MongoDB Configuration
        JsonObject config = new JsonObject()
                .put("connection_string", "mongodb://localhost:27017") // Local MongoDB
                .put("db_name", "todo_app"); // Database Name

        this.mongoClient = MongoClient.createShared(vertx, config);
    }

    /**
     * Get shared MongoClient instance.
     */
    public MongoClient getClient() {
        return mongoClient;
    }
}
