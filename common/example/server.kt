package example

import com.github.kotlin.everywhere.server.Crate

class Root : Crate() {
    val todo by c(::TodoCrate)
    val member by c(::MemberCrate)
}
