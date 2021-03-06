package com.cryptax.db.cloud.datastore

import com.cryptax.domain.entity.User
import com.cryptax.domain.port.UserRepository
import com.google.cloud.datastore.Datastore
import com.google.cloud.datastore.DatastoreException
import com.google.cloud.datastore.Entity
import com.google.cloud.datastore.Query
import com.google.cloud.datastore.StructuredQuery
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class CloudDatastoreUserRepository(datastore: Datastore) : UserRepository, CloudDatastore(datastore) {

    override fun create(user: User): Single<User> {
        return Single.fromCallable {
            log.debug("Create a user $user")
            datastore.put(toEntity(user))
            user
        }
            .subscribeOn(Schedulers.io())
    }

    override fun findById(id: String): Maybe<User> {
        return Maybe.defer {
            log.debug("Get a user by id [$id]")
            val record = datastore.get(datastore.newKeyFactory().setKind(kind).newKey(id))
            when (record) {
                null -> Maybe.empty<User>()
                else -> Maybe.just(toUser(record))
            }
        }
            .subscribeOn(Schedulers.io())
    }

    override fun findByEmail(email: String): Maybe<User> {
        return Maybe.defer {
            val query = Query.newEntityQueryBuilder()
                .setKind(kind)
                .setFilter(StructuredQuery.PropertyFilter.eq("email", email))
                .build()
            val queryResults = datastore.run(query)
            when (queryResults.hasNext()) {
                false -> Maybe.empty<User>()
                true -> Maybe.just(toUser(queryResults.next()))
            }
        }
            .subscribeOn(Schedulers.io())
    }

    override fun updateUser(user: User): Single<User> {
        return Single.fromCallable {
            log.debug("Update a user $user")
            datastore.update(toEntity(user))
            user
        }
            .subscribeOn(Schedulers.io())
    }

    override fun ping(): Boolean {
        log.debug("Ping user repository")
        return try {
            datastore.run(Query.newGqlQueryBuilder("SELECT email FROM $kind LIMIT 1").setAllowLiteral(true).build())
            true
        } catch (e: DatastoreException) {
            log.error("Could not ping Google Cloud", e)
            false
        }
    }

    private fun toEntity(user: User): Entity {
        return Entity.newBuilder(datastore.newKeyFactory().setKind(kind).newKey(user.id))
            .set("email", user.email)
            .set("password", user.password.joinToString(separator = ""))
            .set("lastName", user.lastName)
            .set("firstName", user.firstName)
            .set("allowed", user.allowed)
            .build()
    }

    private fun toUser(entity: Entity): User {
        return User(
            id = entity.key.name,
            email = entity.getString("email"),
            password = entity.getString("password").toCharArray(),
            lastName = entity.getString("lastName"),
            firstName = entity.getString("firstName"),
            allowed = entity.getBoolean("allowed")
        )
    }

    companion object {
        private val log: Logger = LoggerFactory.getLogger(CloudDatastoreUserRepository::class.java)
        private val kind = User::class.java.simpleName
    }
}
