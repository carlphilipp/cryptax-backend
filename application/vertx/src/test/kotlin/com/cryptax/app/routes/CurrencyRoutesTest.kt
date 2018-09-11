package com.cryptax.app.routes

import com.cryptax.app.config.TestConfig
import com.cryptax.app.config.kodein
import com.cryptax.app.initUserAndGetToken
import com.cryptax.app.setupRestAssured
import com.cryptax.app.verticle.RestVerticle
import io.restassured.RestAssured.given
import io.restassured.http.ContentType
import io.restassured.http.Header
import io.vertx.core.Vertx
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import java.util.concurrent.TimeUnit

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(VertxExtension::class)
@DisplayName("Currency routes integration tests")
class CurrencyRoutesTest {

    @BeforeAll
    internal fun beforeAll() {
        setupRestAssured()
    }

    @BeforeEach
    fun beforeEach(vertx: Vertx, testContext: VertxTestContext) {
        vertx.deployVerticle(RestVerticle(TestConfig(), kodein()), testContext.succeeding { _ -> testContext.completeNow() })
        testContext.awaitCompletion(1, TimeUnit.SECONDS)
    }

    @Test
    @DisplayName("Get one user")
    fun getOneUser(testContext: VertxTestContext) {
        // given
        val token = initUserAndGetToken()

        // @formatter:off
        given().
            log().ifValidationFails().
            contentType(ContentType.JSON).
            header(Header("Authorization", "Bearer ${token.getString("token")}")).
        get("/currencies").
        then().
            log().ifValidationFails().
/*            assertThat().body("id", equalTo(token.getString("id"))).
            assertThat().body("email", equalTo(user.email)).
            assertThat().body("password", nullValue()).
            assertThat().body("lastName", equalTo(user.lastName)).
            assertThat().body("firstName", equalTo(user.firstName)).*/
            assertThat().statusCode(200)
        // @formatter:on

        testContext.completeNow()
    }
}