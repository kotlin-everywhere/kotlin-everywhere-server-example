import example.RootContainer
import example.TodoContainer
import example.ctx
import example.jooq.Tables.TODO
import minek.kotlin.everywhere.server.runServer
import org.jooq.impl.DSL
import java.util.*

fun RootContainer.impl(): RootContainer {
    todo.impl()
    return this
}

fun TodoContainer.impl() {
    list {
        ctx.select(TODO.ID, TODO.TITLE, TODO.CREATED_AT)
                .from(TODO)
                .fetch { TodoContainer.Todo(id = it[TODO.ID], title = it[TODO.TITLE], completedAt = it[TODO.CREATED_AT]) }
    }

    add {
        val id = UUID.randomUUID()
        ctx.insertInto(TODO)
                .set(TODO.ID, id)
                .set(TODO.TITLE, it.title)
                .execute()
        id
    }

    markCompleted {
        ctx.update(TODO)
                .set(TODO.COMPLETED_AT, DSL.currentTimestamp())
                .where(TODO.ID.eq(it))
                .returning(TODO.COMPLETED_AT)
                .fetchOptional()
                .map { it.completedAt }
    }
}

fun main(args: Array<String>) {
    RootContainer().impl().runServer(8080)
}