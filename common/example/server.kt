package example

import minek.kotlin.everywhere.server.Container
import minek.kotlin.everywhere.server.f
import java.sql.Timestamp
import java.util.*

class RootContainer : Container() {
    val todo = TodoContainer()
}

class TodoContainer : Container() {
    data class Todo(val id: UUID, val title: String, val completedAt: Timestamp?)

    val list = f<Unit, List<Todo>>()

    class AddForm(val title: String)

    val add = f <AddForm, UUID>()

    val markCompleted = f<UUID, Optional<Timestamp>>()
}


