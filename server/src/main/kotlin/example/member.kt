package example

import example.jooq.Tables
import org.jooq.impl.DSL
import java.util.*

fun MemberCrate.impl() {
    signUp { (loginId, loginPassword) ->
        transaction { ctx ->
            val count = DSL.count()
            val exists = ctx
                    .select(count)
                    .from(Tables.MEMBER)
                    .where(Tables.MEMBER.LOGIN_ID.eq(loginId))
                    .fetchOne()[count] > 0
            if (exists) {
                return@transaction false
            }

            ctx.insertInto(Tables.MEMBER)
                    .set(Tables.MEMBER.ID, UUID.randomUUID())
                    .set(Tables.MEMBER.LOGIN_ID, loginId)
                    .set(Tables.MEMBER.LOGIN_PASSWORD, loginPassword)
                    .execute()
            true
        }
    }

    signIn { (loginId, loginPassword) ->
        transaction { ctx ->
            val accessToken = UUID.randomUUID()
            val memberId = ctx
                    .select(Tables.MEMBER.ID)
                    .from(Tables.MEMBER)
                    .where(Tables.MEMBER.LOGIN_ID.eq(loginId), Tables.MEMBER.LOGIN_PASSWORD.eq(loginPassword))
                    .firstOrNull()
                    ?.get(Tables.MEMBER.ID) ?: return@transaction null

            ctx.insertInto(Tables.ACCESS_TOKEN)
                    .set(Tables.ACCESS_TOKEN.ID, accessToken)
                    .set(Tables.ACCESS_TOKEN.MEMBER_ID, memberId)
                    .execute()
            accessToken
        }
    }
}
