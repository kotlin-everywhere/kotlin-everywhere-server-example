import com.github.kotlin.everywhere.server.Box
import com.github.kotlin.everywhere.server.runServer
import com.github.parksungmin.jooqs.Jooqs
import example.*
import example.jooq.Tables.*
import org.jooq.Condition
import org.jooq.impl.DSL
import java.util.*

class Member(val id: UUID)

operator inline fun <P, R> Box<MemberContext<P>, MemberResponse<R>>.invoke(crossinline handler: (member: Member, parameter: P) -> R) {
    invoke { (accessToken, data) ->
        val member = ctx
                .select(MEMBER.ID)
                .from(MEMBER)
                .join(ACCESS_TOKEN).on(ACCESS_TOKEN.MEMBER_ID.eq(MEMBER.ID))
                .where(ACCESS_TOKEN.ID.eq(accessToken))
                .map { Member(id = it[MEMBER.ID]) }
                .firstOrNull()
        if (member == null) {
            MemberResponse.InvalidAccessToken()
        } else {
            MemberResponse.Ok(handler(member, data))
        }
    }
}

fun Root.impl(): Root {
    todo.impl()
    member.impl()
    return this
}

fun MemberCrate.impl() {
    signUp { (loginId, loginPassword) ->
        transaction { ctx ->
            val count = DSL.count()
            val exists = ctx
                    .select(count)
                    .from(MEMBER)
                    .where(MEMBER.LOGIN_ID.eq(loginId))
                    .fetchOne()[count] > 0
            if (exists) {
                return@transaction false
            }

            ctx.insertInto(MEMBER)
                    .set(MEMBER.ID, UUID.randomUUID())
                    .set(MEMBER.LOGIN_ID, loginId)
                    .set(MEMBER.LOGIN_PASSWORD, loginPassword)
                    .execute()
            true
        }
    }

    signIn { (loginId, loginPassword) ->
        transaction { ctx ->
            val accessToken = UUID.randomUUID()
            val memberId = ctx
                    .select(MEMBER.ID)
                    .from(MEMBER)
                    .where(MEMBER.LOGIN_ID.eq(loginId), MEMBER.LOGIN_PASSWORD.eq(loginPassword))
                    .firstOrNull()
                    ?.get(MEMBER.ID) ?: return@transaction null

            ctx.insertInto(ACCESS_TOKEN)
                    .set(ACCESS_TOKEN.ID, accessToken)
                    .set(ACCESS_TOKEN.MEMBER_ID, memberId)
                    .execute()
            accessToken
        }
    }
}

fun TodoCrate.impl() {
    add { member, todo ->
        transaction { ctx ->
            if (Jooqs.exists(ctx, ctx.selectOne().from(TODO).where(TODO.ID.eq(todo.id)))) {
                TodoCrate.Add.DuplicatedId
            } else {
                ctx.insertInto(TODO)
                        .set(TODO.ID, todo.id)
                        .set(TODO.MEMBER_ID, member.id)
                        .set(TODO.TITLE, todo.title)
                        .set(TODO.COMPLETED_AT, todo.completedAt)
                        .execute()
                TodoCrate.Add.Success
            }
        }
    }

    update { member, todo ->
        val updatedRowCount = ctx
                .update(TODO)
                .set(TODO.TITLE, todo.title)
                .set(TODO.COMPLETED_AT, todo.completedAt)
                .where(TODO.ID.eq(todo.id), TODO.MEMBER_ID.eq(member.id))
                .execute()
        when (updatedRowCount) {
            1 -> TodoCrate.Update.Success
            else -> TodoCrate.Update.NotFound
        }
    }

    list { member, keyword ->
        val conditions = mutableListOf<Condition>()
        if (keyword?.isNotBlank() ?: false) {
            conditions.add(TODO.TITLE.like("%$keyword%"))
        }

        ctx.select(TODO.ID, TODO.TITLE, TODO.COMPLETED_AT)
                .from(TODO)
                .where(TODO.MEMBER_ID.eq(member.id), DSL.and(conditions))
                .fetch { TodoCrate.Todo(it[TODO.ID], it[TODO.TITLE], it[TODO.COMPLETED_AT]) }
    }

    delete { member, id ->
        ctx
                .delete(TODO)
                .where(TODO.MEMBER_ID.eq(member.id), TODO.ID.eq(id))
                .execute()
    }
}

fun main(args: Array<String>) {
    Root().impl().runServer(8080)
}