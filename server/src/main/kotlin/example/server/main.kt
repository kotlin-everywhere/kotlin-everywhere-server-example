package example.server

import com.github.kotlin.everywhere.server.runServer
import example.Root

fun Root.impl(): Root {
    todo.impl()
    member.impl()
    return this
}


fun main(args: Array<String>) {
    Root().impl().runServer(8080)
}