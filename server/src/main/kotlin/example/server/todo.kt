package example.server

import com.github.parksungmin.jooqs.Jooqs
import example.TodoCrate
import example.jooq.Tables
import org.jooq.Condition
import org.jooq.impl.DSL

fun TodoCrate.impl() {
    add { member, todo ->
        transaction { ctx ->
            if (Jooqs.exists(ctx, ctx.selectOne().from(Tables.TODO).where(Tables.TODO.ID.eq(todo.id)))) {
                TodoCrate.Add.DuplicatedId
            } else {
                ctx.insertInto(Tables.TODO)
                        .set(Tables.TODO.ID, todo.id)
                        .set(Tables.TODO.MEMBER_ID, member.id)
                        .set(Tables.TODO.TITLE, todo.title)
                        .set(Tables.TODO.COMPLETED_AT, todo.completedAt)
                        .execute()
                TodoCrate.Add.Success
            }
        }
    }

    update { member, todo ->
        val updatedRowCount = ctx
                .update(Tables.TODO)
                .set(Tables.TODO.TITLE, todo.title)
                .set(Tables.TODO.COMPLETED_AT, todo.completedAt)
                .where(Tables.TODO.ID.eq(todo.id), Tables.TODO.MEMBER_ID.eq(member.id))
                .execute()
        when (updatedRowCount) {
            1 -> TodoCrate.Update.Success
            else -> TodoCrate.Update.NotFound
        }
    }

    list { member, keyword ->
        val conditions = mutableListOf<Condition>()
        if (keyword?.isNotBlank() ?: false) {
            conditions.add(Tables.TODO.TITLE.like("%$keyword%"))
        }

        ctx.select(Tables.TODO.ID, Tables.TODO.TITLE, Tables.TODO.COMPLETED_AT)
                .from(Tables.TODO)
                .where(Tables.TODO.MEMBER_ID.eq(member.id), DSL.and(conditions))
                .fetch { TodoCrate.Todo(it[Tables.TODO.ID], it[Tables.TODO.TITLE], it[Tables.TODO.COMPLETED_AT]) }
    }

    delete { member, id ->
        ctx
                .delete(Tables.TODO)
                .where(Tables.TODO.MEMBER_ID.eq(member.id), Tables.TODO.ID.eq(id))
                .execute()
    }
}
