package example.server

import com.github.kotlin.everywhere.server.Box
import example.MemberContext
import example.MemberResponse
import example.jooq.Tables
import java.util.*

class Member(val id: UUID)

operator inline fun <P, R> Box<MemberContext<P>, MemberResponse<R>>.invoke(crossinline handler: (member: Member, parameter: P) -> R) {
    invoke { (accessToken, data) ->
        val member = ctx
                .select(Tables.MEMBER.ID)
                .from(Tables.MEMBER)
                .join(Tables.ACCESS_TOKEN).on(Tables.ACCESS_TOKEN.MEMBER_ID.eq(Tables.MEMBER.ID))
                .where(Tables.ACCESS_TOKEN.ID.eq(accessToken))
                .map { Member(id = it[Tables.MEMBER.ID]) }
                .firstOrNull()
        if (member == null) {
            MemberResponse.InvalidAccessToken()
        } else {
            MemberResponse.Ok(handler(member, data))
        }
    }
}