package com.cryptax.app

import com.cryptax.app.verticle.RestVerticle
import com.cryptax.config.Config
import com.cryptax.config.DefaultConfig
import com.cryptax.di.KodeinConfig
import io.vertx.core.AsyncResult
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.kotlin.ext.dropwizard.DropwizardMetricsOptions
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager
import org.kodein.di.Kodein
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object Main {

    init {
        System.setProperty("vertx.logger-delegate-factory-class-name", "io.vertx.core.logging.Log4j2LogDelegateFactory")
        System.setProperty("hazelcast.logging.type", "log4j2")
        System.setProperty("log4j.configurationFile", "log4j2-" + Config.profile() + ".xml")
    }

    private val log: Logger = LoggerFactory.getLogger(Main::class.java)

    @JvmStatic
    fun main(args: Array<String>) {
        val dropwizardOptions = DropwizardMetricsOptions(baseName = "cryptax", enabled = true)

        val mgr = HazelcastClusterManager()
        Vertx.clusteredVertx(VertxOptions().setClusterManager(mgr).setMetricsOptions(dropwizardOptions)) { ar ->
            when {
                ar.succeeded() -> {
                    val vertx: Vertx = ar.result()
                    val appConfig = DefaultConfig()
                    val kodeinDefaultModule = KodeinConfig(properties = appConfig.properties, vertx = vertx).kodeinModule
                    val kodein = Kodein { import(kodeinDefaultModule) }

                    vertx.deployVerticle(RestVerticle(appConfig, kodein)) { arRest: AsyncResult<String> ->
                        when {
                            arRest.succeeded() -> log.info("${RestVerticle::class.java.simpleName} deployed")
                            arRest.failed() -> log.error("Could not deploy ${RestVerticle::class.java.simpleName}", arRest.cause())
                        }
                    }
                }
                else -> log.error("Could not deploy start clustered vertx", ar.cause())
            }
        }
    }
}