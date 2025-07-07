package org.example;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.mongo.MongoClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.redis.client.RedisAPI;

import java.util.List;

public class UserController {
    private final MongoClient mongoClient;
    private final EmailService emailService;
    private final RedisAPI redis;

    public UserController(Vertx vertx, Router router, MongoService mongoService, EmailService emailService, RedisService redisService) {
        this.mongoClient = mongoService.getClient();
        this.emailService = emailService;
        this.redis = redisService.getRedis();

        router.post("/register").handler(this::registerUser);
        router.post("/login").handler(this::loginUser);
        router.post("/refresh").handler(this::refreshToken);
        router.post("/logout").handler(this::logoutUser);
    }

    // ✅ Register
    public void registerUser(RoutingContext ctx) {
        JsonObject body = ctx.body().asJsonObject();
        String email = body.getString("email");
        String name = body.getString("name");

        if (email == null || name == null) {
            ctx.response().setStatusCode(400).end(new JsonObject().put("error", "Name and Email are required").encode());
            return;
        }

        mongoClient.findOne("users", new JsonObject().put("email", email), null, res -> {
            if (res.succeeded()) {
                if (res.result() != null) {
                    ctx.response().setStatusCode(409).end(new JsonObject().put("error", "User already registered").encode());
                } else {
                    String rawPassword = PasswordUtil.generateRandomPassword();
                    String hashed = PasswordUtil.hashPassword(rawPassword);

                    JsonObject newUser = new JsonObject()
                            .put("email", email)
                            .put("name", name)
                            .put("password", hashed)
                            .put("createdAt", System.currentTimeMillis());

                    mongoClient.insert("users", newUser, insertRes -> {
                        if (insertRes.succeeded()) {
                            String message = "Hi " + name + ",\n\n" +
                                    "Welcome to the ToDo App! 🎉\n\n" +
                                    "Here is your login password: " + rawPassword + "\n\n" +
                                    "Use this to log in and start organizing your tasks efficiently.\n\n" +
                                    "👉 https://localhost:8888\n\n" +
                                    "Cheers,\nYour ToDo App Team";

                            emailService.sendEmail(email, "🔑 Your ToDo App Login Password", message);

                            ctx.response().setStatusCode(201).end(new JsonObject().put("message", "User registered. Check your email for login password").encode());
                        } else {
                            ctx.response().setStatusCode(500).end(new JsonObject().put("error", "Database insert failed").encode());
                        }
                    });
                }
            } else {
                ctx.response().setStatusCode(500).end(new JsonObject().put("error", "Database error").encode());
            }
        });
    }

    // ✅ Login
    public void loginUser(RoutingContext ctx) {
        JsonObject body = ctx.body().asJsonObject();
        String email = body.getString("email");
        String password = body.getString("password");

        if (email == null || password == null) {
            ctx.response().setStatusCode(400).end(new JsonObject().put("error", "Email and password required").encode());
            return;
        }

        mongoClient.findOne("users", new JsonObject().put("email", email), null, res -> {
            if (res.succeeded()) {
                JsonObject user = res.result();
                if (user == null) {
                    ctx.response().setStatusCode(401).end(new JsonObject().put("error", "Invalid credentials").encode());
                } else {
                    String hashed = user.getString("password");
                    if (PasswordUtil.verifyPassword(password, hashed)) {
                        String accessToken = JwtUtil.generateAccessToken(email);
                        String refreshToken = JwtUtil.generateRefreshToken(email);


                        redis.setex(email, String.valueOf(7 * 24 * 60 * 60), refreshToken, redisRes -> {
                            if (redisRes.succeeded()) {
                                JsonObject response = new JsonObject()
                                        .put("accessToken", accessToken)
                                        .put("refreshToken", refreshToken);
                                ctx.response().putHeader("Content-Type", "application/json").end(response.encode());
                            } else {
                                ctx.response().setStatusCode(500).end(new JsonObject().put("error", "Redis error").encode());
                            }
                        });
                    } else {
                        ctx.response().setStatusCode(401).end(new JsonObject().put("error", "Invalid credentials").encode());
                    }
                }
            } else {
                ctx.response().setStatusCode(500).end(new JsonObject().put("error", "Database error").encode());
            }
        });
    }

    // ✅ Refresh Token
    public void refreshToken(RoutingContext ctx) {
        JsonObject body = ctx.body().asJsonObject();
        String refreshToken = body.getString("refreshToken");

        if (refreshToken == null || refreshToken.isEmpty()) {
            ctx.response().setStatusCode(400).end(new JsonObject().put("error", "Missing refresh token").encode());
            return;
        }

        try {
            String email = JwtUtil.extractEmailFromRefreshToken(refreshToken);

            redis.get(email).onSuccess(redisValue -> {
                if (redisValue == null || !redisValue.toString().equals(refreshToken)) {
                    ctx.response().setStatusCode(403).end(new JsonObject().put("error", "Invalid refresh token").encode());
                } else {
                    String newAccessToken = JwtUtil.generateAccessToken(email);
                    ctx.response()
                            .putHeader("Content-Type", "application/json")
                            .end(new JsonObject().put("accessToken", newAccessToken).encode());
                }
            }).onFailure(err -> {
                ctx.response().setStatusCode(500).end(new JsonObject().put("error", "Redis error: " + err.getMessage()).encode());
            });
        } catch (Exception e) {
            ctx.response().setStatusCode(400).end(new JsonObject().put("error", "Invalid token payload").encode());
        }
    }

    // ✅ Logout
    public void logoutUser(RoutingContext ctx) {
        JsonObject body = ctx.body().asJsonObject();
        String refreshToken = body.getString("refreshToken");

        if (refreshToken == null || refreshToken.isEmpty()) {
            ctx.response().setStatusCode(400).end(new JsonObject().put("error", "Missing refresh token").encode());
            return;
        }

        try {
            String email = JwtUtil.extractEmailFromRefreshToken(refreshToken);

            redis.get(email).onSuccess(redisValue -> {
                if (redisValue == null || !redisValue.toString().equals(refreshToken)) {
                    ctx.response().setStatusCode(403).end(new JsonObject().put("error", "Invalid refresh token").encode());
                } else {
                    redis.del(List.of(email)).onSuccess(delRes -> {
                        ctx.response().setStatusCode(200).end(new JsonObject().put("message", "Logged out successfully").encode());
                    }).onFailure(err -> {
                        ctx.response().setStatusCode(500).end(new JsonObject().put("error", "Redis error: " + err.getMessage()).encode());
                    });
                }
            }).onFailure(err -> {
                ctx.response().setStatusCode(500).end(new JsonObject().put("error", "Redis error: " + err.getMessage()).encode());
            });
        } catch (Exception e) {
            ctx.response().setStatusCode(400).end(new JsonObject().put("error", "Invalid token payload").encode());
        }
    }
}
