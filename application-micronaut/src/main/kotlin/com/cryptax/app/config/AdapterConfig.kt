package com.cryptax.app.config

import com.cryptax.cache.CacheService
import com.cryptax.cache.HazelcastService
import com.cryptax.config.AppProps
import com.cryptax.domain.port.EmailService
import com.cryptax.domain.port.IdGenerator
import com.cryptax.domain.port.PriceService
import com.cryptax.domain.port.SecurePassword
import com.cryptax.email.SendGridEmailService
import com.cryptax.id.JugIdGenerator
import com.fasterxml.jackson.databind.ObjectMapper
import com.hazelcast.core.HazelcastInstance
import io.micronaut.context.annotation.Factory
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Factory
class AdapterConfig {

    @Inject
    lateinit var properties: AppProps

    @Singleton
    fun httpClient(): OkHttpClient {
        val connectionPool = ConnectionPool(properties.http.maxIdleConnections, properties.http.keepAliveDuration, TimeUnit.MINUTES)
        val builder = OkHttpClient.Builder().connectionPool(connectionPool)
        return builder.build()
    }

    @Singleton
    fun emailService(): EmailService {
        return SendGridEmailService(httpClient(), properties.email)
    }

    @Singleton
    fun securePassword(): SecurePassword {
        return com.cryptax.security.SecurePassword()
    }

    @Singleton
    fun idGenerator(): IdGenerator {
        return JugIdGenerator()
    }

    @Singleton
    fun cacheService(hazelcastInstance: HazelcastInstance): CacheService {
        return HazelcastService(hazelcastInstance)
    }

    @Singleton
    fun priceService(client: OkHttpClient, objectMapper: ObjectMapper, cache: CacheService): PriceService {
        return com.cryptax.price.PriceService(client, objectMapper, cache)
    }
}