package org.example;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.bson.types.ObjectId;

public class TaskController {

    private final MongoService mongoService;

    public TaskController(MongoService mongoService) {
        this.mongoService = mongoService;
    }

    // Create new task
    public void createTask(RoutingContext ctx) {
        String userEmail = JwtUtil.extractEmail(ctx);
        JsonObject body = ctx.body().asJsonObject();

        String title = body.getString("title");
        String priority = body.getString("priority", "Medium");

        if (title == null || title.isEmpty()) {
            ctx.response().setStatusCode(400).end("Task title is required");
            return;
        }

        JsonObject task = new JsonObject()
                .put("title", title)
                .put("priority", priority)
                .put("completed", false)
                .put("userEmail", userEmail)
                .put("createdAt", System.currentTimeMillis());

        mongoService.getClient().insert("tasks", task, res -> {
            if (res.succeeded()) {
                ctx.response().setStatusCode(201).end("Task created successfully");
            } else {
                res.cause().printStackTrace(); // Log the actual error
                ctx.response().setStatusCode(500).end("Failed to create task");
            }
        });
    }

    // Get all tasks for the logged-in user
    public void getTasks(RoutingContext ctx) {
        String userEmail = JwtUtil.extractEmail(ctx);
        JsonObject query = new JsonObject().put("userEmail", userEmail);

        mongoService.getClient().find("tasks", query, res -> {
            if (res.succeeded()) {
                ctx.response()
                        .putHeader("Content-Type", "application/json")
                        .end(res.result().toString());
            } else {
                ctx.response().setStatusCode(500).end("Failed to fetch tasks");
            }
        });
    }

    // Update task's completed status
    public void updateTaskStatus(RoutingContext ctx) {
        String taskId = ctx.pathParam("id");
        JsonObject body = ctx.body().asJsonObject();
        Boolean completed = body.getBoolean("completed");

        if (taskId == null || completed == null) {
            ctx.response().setStatusCode(400).end("Invalid request");
            return;
        }

        JsonObject query = new JsonObject().put("_id", new ObjectId(taskId));
        JsonObject update = new JsonObject().put("$set", new JsonObject().put("completed", completed));

        mongoService.getClient().updateCollection("tasks", query, update, res -> {
            if (res.succeeded()) {
                ctx.response().end("Task updated successfully");
            } else {
                ctx.response().setStatusCode(500).end("Failed to update task");
            }
        });
    }
}
