package example

import com.github.kotlin.everywhere.json.decode.*
import com.github.kotlin.everywhere.json.decode.Decoders.field
import com.github.kotlin.everywhere.json.encode.Encoder
import com.github.kotlin.everywhere.json.encode.Encoders
import com.github.kotlin.everywhere.server.Box
import com.github.kotlin.everywhere.server.Crate
import java.sql.Timestamp
import java.util.*

val uuidEncoder: Encoder<UUID> = { Encoders.string(it.toString()) }

fun parseUuid(uuid: String): Result<String, UUID> {
    try {

        return Ok(UUID.fromString(uuid))
    } catch (e: Exception) {
        return Err(e.message ?: e.toString())
    }
}

val uuidDecoder: Decoder<UUID> = { Decoders.string(it).andThen(::parseUuid) }

val timestampDecoder: Decoder<Timestamp> = { Decoders.long(it).map(::Timestamp) }

val timestampEncoder: Encoder<Timestamp> = { Encoders.long(it.time) }

class Root : Crate() {
    val todo by c(::TodoCrate)
    val member by c(::MemberCrate)
}

class MemberCrate : Crate() {
    data class SignForm(val loginId: String, val loginPassword: String) {
        companion object {
            val decoder = map(
                    field("loginId", Decoders.string),
                    field("loginPassword", Decoders.string),
                    ::SignForm
            )
        }
    }

    val signUp by b(SignForm.decoder, Encoders.bool)
    val signIn by b(SignForm.decoder, Encoders.nullable(uuidEncoder))
}

class TodoCrate : Crate() {
    class Todo(val id: UUID, val title: String, val completedAt: Timestamp?) {
        companion object {
            val decoder = map(
                    field("id", uuidDecoder),
                    field("title", Decoders.string),
                    field("completedAt", Decoders.nullable(timestampDecoder)),
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

fun <T : Crate, P, R> T.mb(decoder: Decoder<P>, encoder: Encoder<R>): Box.BoxDelegate<MemberContext<P>, MemberResponse<R>> {
    return b(MemberContext.decoder(decoder), MemberResponse.encoder(encoder))
}

data class MemberContext<out T>(val accessToken: UUID, val data: T) {
    companion object {
        fun <T> decoder(dataDecoder: Decoder<T>): Decoder<MemberContext<T>> {
            return map(
                    field("accessToken", uuidDecoder),
                    field("data", dataDecoder),
                    ::MemberContext
            )
        }
    }
}

@kotlin.Suppress("unused")
sealed class MemberResponse<T> {
    class InvalidAccessToken<T> : MemberResponse<T>()
    class Ok<T>(val data: T) : MemberResponse<T>()

    companion object {
        fun <T> encoder(dataEncoder: Encoder<T>): Encoder<MemberResponse<T>> {
            return { response ->
                when (response) {
                    is MemberResponse.Ok<T> -> Encoders.object_("__type__" to Encoders.string("Ok"), "data" to dataEncoder(response.data))
                    is MemberResponse.InvalidAccessToken -> Encoders.object_("__type__" to Encoders.string("InvalidAccessToken"))
                }
            }
        }
    }
}

