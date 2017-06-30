package example

import com.github.kotlin.everywhere.json.decode.Decoders
import com.github.kotlin.everywhere.json.decode.map
import com.github.kotlin.everywhere.json.encode.Encoders
import com.github.kotlin.everywhere.server.Crate


class MemberCrate : Crate() {
    data class SignForm(val loginId: String, val loginPassword: String) {
        companion object {
            val decoder = map(
                    Decoders.field("loginId", Decoders.string),
                    Decoders.field("loginPassword", Decoders.string),
                    ::SignForm
            )
        }
    }

    val signUp by b(SignForm.decoder, Encoders.bool)
    val signIn by b(SignForm.decoder, Encoders.nullable(uuidEncoder))
}