package org.example;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

public class JwtMiddleware implements Handler<RoutingContext> {

    @Override
    public void handle(RoutingContext ctx) {
        String authHeader = ctx.request().getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            ctx.response().setStatusCode(401).end("Unauthorized: Missing token");
            return;
        }

        String token = authHeader.substring(7); // Remove "Bearer "

        if (!JwtUtil.validateAccessToken(token)) {
            ctx.response().setStatusCode(401).end("Unauthorized: Invalid or expired token");
            return;
        }

        ctx.next(); // Token is valid → continue to next handler
    }
}
