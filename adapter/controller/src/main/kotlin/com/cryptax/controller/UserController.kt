package com.cryptax.controller

import com.cryptax.controller.model.UserWeb
import com.cryptax.usecase.CreateUser
import com.cryptax.usecase.FindUser
import com.cryptax.usecase.LoginUser
import io.vertx.core.buffer.Buffer
import io.vertx.core.http.HttpServerResponse
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.ext.auth.jwt.JWTOptions

class UserController(private val createUser: CreateUser, private val findUser: FindUser, private val loginUser: LoginUser) {

	companion object {
		private val JWT_OPTIONS = JWTOptions(algorithm = "ES512", issuer = "Cryptax")
	}

	fun createUser(routingContext: RoutingContext) {
		val response = routingContext.response()
		val body = routingContext.body
		// TODO remove that check when this is validated upstream
		if (isNull(body)) {
			sendError(400, response)
		} else {
			val userWeb = body.toJsonObject().mapTo(UserWeb::class.java)
			val user = createUser.create(userWeb.toUser())
			val result = JsonObject.mapFrom(UserWeb.toUserWeb(user))
			sendSuccess(result, response)
		}
	}

	fun login(routingContext: RoutingContext, jwtProvider: JWTAuth) {
		val email = routingContext.request().getParam("email")
		val password = routingContext.request().getParam("password").toCharArray()

		val userId = loginUser.login(email, password).id
		val result = JsonObject().put("id", userId)
		val token = jwtProvider.generateToken(result, JWT_OPTIONS)
		result.put("token", token)
		sendSuccess(result, routingContext.response())
	}

	fun findUser(routingContext: RoutingContext) {
		val response = routingContext.response()
		val userId = routingContext.request().getParam("userId")
		if (routingContext.user().principal().getString("id") == userId) {
			val user = findUser.findById(userId)
			if (user != null) {
				val result = JsonObject.mapFrom(UserWeb.toUserWeb(user))
				sendSuccess(result, response)
			} else {
				sendError(404, response)
			}
		} else {
			sendError(401, response)
		}
	}

	fun findAllUser(routingContext: RoutingContext) {
		val users = findUser.findAllUsers()

		val result: JsonArray = users
			.map { user -> JsonObject.mapFrom(UserWeb.toUserWeb(user)) }
			.fold(mutableListOf<JsonObject>()) { accumulator, item ->
				accumulator.add(item)
				accumulator
			}
			.fold(JsonArray()) { accumulator, item ->
				accumulator.add(item)
				accumulator
			}

		routingContext.response()
			.putHeader("content-type", "application/json")
			.end(result.encodePrettily())
	}


	private fun isNull(buffer: Buffer?): Boolean {
		return buffer == null || "" == buffer.toString()
	}

	private fun sendError(statusCode: Int, response: HttpServerResponse) {
		response
			.putHeader("content-type", "application/json")
			.setStatusCode(statusCode)
			.end()
	}

	private fun sendSuccess(body: JsonObject, response: HttpServerResponse) {
		response
			.putHeader("content-type", "application/json")
			.end(body.encodePrettily())
	}
}