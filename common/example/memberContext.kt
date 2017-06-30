package example

import com.github.kotlin.everywhere.json.decode.Decoder
import com.github.kotlin.everywhere.json.decode.Decoders
import com.github.kotlin.everywhere.json.decode.map
import com.github.kotlin.everywhere.json.encode.Encoder
import com.github.kotlin.everywhere.json.encode.Encoders
import com.github.kotlin.everywhere.server.Box
import com.github.kotlin.everywhere.server.Crate
import java.util.*

fun <T : Crate, P, R> T.mb(decoder: Decoder<P>, encoder: Encoder<R>): Box.BoxDelegate<MemberContext<P>, MemberResponse<R>> {
    return b(MemberContext.decoder(decoder), MemberResponse.encoder(encoder))
}

data class MemberContext<out T>(val accessToken: UUID, val data: T) {
    companion object {
        fun <T> decoder(dataDecoder: Decoder<T>): Decoder<MemberContext<T>> {
            return map(
                    Decoders.field("accessToken", uuidDecoder),
                    Decoders.field("data", dataDecoder),
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
