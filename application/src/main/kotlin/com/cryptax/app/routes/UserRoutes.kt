package com.cryptax.app.routes

import com.cryptax.app.routes.Failure.failureHandler
import com.cryptax.app.routes.Routes.sendSuccess
import com.cryptax.config.Config
import com.cryptax.controller.model.UserWeb
import com.cryptax.validation.RestValidation.createUserValidation
import com.cryptax.validation.RestValidation.getUserValidation
import com.cryptax.validation.RestValidation.jsonContentTypeValidation
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.JWTAuthHandler

fun handleUserRoutes(config: Config, router: Router, jwtAuthHandler: JWTAuthHandler) {

    val userController = config.userController()

    // Create user
    router.post("/users")
        .handler(jsonContentTypeValidation)
        .handler(bodyHandler)
        .handler(createUserValidation)
        .handler { routingContext ->
            val userWeb = routingContext.body.toJsonObject().mapTo(UserWeb::class.java)
            val result = JsonObject.mapFrom(userController.createUser(userWeb))
            sendSuccess(result, routingContext.response())
        }
        .failureHandler(failureHandler)

    // Get user with user id and JWT token
    router.get("/users/:userId")
        .handler(jwtAuthHandler)
        .handler(getUserValidation)
        .handler { routingContext ->
            val userId = routingContext.request().getParam("userId")
            // Previous validation 'insure' (95%) the user exists
            val userWeb = userController.findUser(userId)!!
            val result = JsonObject.mapFrom(userWeb)
            sendSuccess(result, routingContext.response())
        }
        .failureHandler(failureHandler)

    // Get all users with JWT token
    router.get("/users")
        .handler(jwtAuthHandler)
        .handler { routingContext ->
            val result = userController.findAllUsers()
                .map { JsonObject.mapFrom(it) }
                .fold(mutableListOf<JsonObject>()) { accumulator, item ->
                    accumulator.add(item)
                    accumulator
                }
                .fold(JsonArray()) { accumulator, item ->
                    accumulator.add(item)
                    accumulator
                }
            sendSuccess(result, routingContext.response())
        }
        .failureHandler(failureHandler)
}

