package org.example;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;

public class MainApp extends AbstractVerticle {

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new MainApp());
    }

    @Override
    public void start() {
        System.out.println("🚀 Inside start()");
        Vertx vertx = Vertx.vertx();

        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());

        router.route().handler(CorsHandler.create("*")
                .allowedMethod(HttpMethod.POST)
                .allowedMethod(HttpMethod.GET)
                .allowedMethod(HttpMethod.PUT)
                .allowedMethod(HttpMethod.DELETE)
                .allowedHeader("Content-Type")
                .allowedHeader("Authorization"));

        // Services
        MongoService mongoService = new MongoService(vertx);
        EmailService emailService = new EmailService(vertx);
        RedisService redisService = new RedisService(vertx);

        // Controllers
        UserController userController = new UserController(vertx, router, mongoService, emailService, redisService);
        TaskController taskController = new TaskController(mongoService, emailService);
        JwtMiddleware jwtMiddleware = new JwtMiddleware();



        // 🔐 Protected Task Routes
        router.post("/tasks").handler(jwtMiddleware).handler(taskController::createTask);         // Create task
        router.get("/tasks").handler(jwtMiddleware).handler(taskController::getTasks);            // Get all tasks
        router.put("/tasks/:id").handler(jwtMiddleware).handler(taskController::updateTaskStatus); // Update completed status

        // Start server
        HttpServer server = vertx.createHttpServer();
        server.requestHandler(router).listen(8888, http -> {
            if (http.succeeded()) {
                System.out.println("✅ Server running at http://localhost:8888");
            } else {
                System.out.println("❌ Failed to start server: " + http.cause().getMessage());
            }
        });
    }
}
