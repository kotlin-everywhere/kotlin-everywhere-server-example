package example

import com.github.kotlin.everywhere.json.decode.Decoders
import com.github.kotlin.everywhere.json.decode.map
import com.github.kotlin.everywhere.json.encode.Encoder
import com.github.kotlin.everywhere.json.encode.Encoders
import com.github.kotlin.everywhere.server.Crate
import java.sql.Timestamp
import java.util.*

class TodoCrate : Crate() {
    class Todo(val id: UUID, val title: String, val completedAt: Timestamp?) {
        companion object {
            val decoder = map(
                    Decoders.field("id", uuidDecoder),
                    Decoders.field("title", Decoders.string),
                    Decoders.field("completedAt", Decoders.nullable(timestampDecoder)),
                    ::Todo
            )

            val encoder = { it: Todo ->
                Encoders.object_(
                        "id" to uuidEncoder(it.id),
                        "title" to Encoders.string(it.title),
                        "completedAt" to Encoders.nullable(timestampEncoder)(it.completedAt)
                )
            }
            var listEncoder = { it: List<Todo> -> Encoders.array(it.map { Todo.encoder(it) }) }
        }
    }

    sealed class Add {
        object Success : Add()
        object DuplicatedId : Add()

        companion object {
            val encoder: Encoder<Add> = { add ->
                Encoders.string(when (add) {
                    Success -> "Success"
                    DuplicatedId -> "DuplicatedId"
                })
            }
        }
    }

    sealed class Update {
        object Success : Update()
        object NotFound : Update()

        companion object {
            val encoder: Encoder<Update> = { update ->
                Encoders.string(when (update) {
                    Update.Success -> "Success"
                    Update.NotFound -> "DuplicatedId"
                })
            }
        }
    }

    val add by mb(Todo.decoder, Add.encoder)
    val list by mb(Decoders.nullable(Decoders.string), Todo.listEncoder)
    val update by mb(Todo.decoder, Update.encoder)
    val delete by mb(uuidDecoder, Encoders.unit)
}
