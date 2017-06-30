package example

import com.github.kotlin.everywhere.json.decode.*
import com.github.kotlin.everywhere.json.encode.Encoder
import com.github.kotlin.everywhere.json.encode.Encoders
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