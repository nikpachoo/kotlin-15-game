package com.glycin

import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

@Serializable
data class ExposedUser(val name: String, val score: Int, val email: String? = null)

data class LeaderboardSnapshot(val top: List<ExposedUser>, val total: Long)

data class SubmissionResult(val rank: Int, val snapshot: LeaderboardSnapshot)

class UserService(database: Database) {
    object Users : Table() {
        val id = integer("id").autoIncrement()
        val name = varchar("name", length = 50)
        val score = integer("score")
        val email = varchar("email", length = 255).nullable()

        override val primaryKey = PrimaryKey(id)
    }

    init {
        transaction(database) {
            SchemaUtils.create(Users)
        }
    }

    suspend fun read(id: Int): ExposedUser? = dbQuery {
        Users.selectAll()
            .where { Users.id eq id }
            .map { ExposedUser(it[Users.name], it[Users.score], it[Users.email]) }
            .singleOrNull()
    }

    suspend fun update(id: Int, user: ExposedUser) {
        dbQuery {
            Users.update({ Users.id eq id }) {
                it[name] = user.name
                it[score] = user.score
                it[email] = user.email
            }
        }
    }

    suspend fun delete(id: Int) {
        dbQuery {
            Users.deleteWhere { Users.id.eq(id) }
        }
    }

    suspend fun leaderboard(limit: Int = 10): LeaderboardSnapshot = dbQuery {
        LeaderboardSnapshot(top = topQuery(limit), total = Users.selectAll().count())
    }

    suspend fun submit(user: ExposedUser, limit: Int = 10): SubmissionResult = dbQuery {
        val newId = Users.insert {
            it[name] = user.name
            it[score] = user.score
            it[email] = user.email
        }[Users.id]
        SubmissionResult(
            rank = rankOfQuery(newId, user.score),
            snapshot = LeaderboardSnapshot(top = topQuery(limit), total = Users.selectAll().count()),
        )
    }

    private fun topQuery(limit: Int): List<ExposedUser> =
        Users.selectAll()
            .orderBy(Users.score to SortOrder.DESC, Users.id to SortOrder.ASC)
            .limit(limit)
            .map { ExposedUser(it[Users.name], it[Users.score], it[Users.email]) }

    private fun rankOfQuery(userId: Int, score: Int): Int =
        Users.selectAll()
            .where {
                (Users.score greater score) or
                    ((Users.score eq score) and (Users.id less userId))
            }
            .count()
            .toInt() + 1

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}
