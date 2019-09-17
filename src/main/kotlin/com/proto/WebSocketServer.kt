package com.proto

import io.ktor.http.cio.websocket.CloseReason
import io.ktor.http.cio.websocket.close
import io.ktor.routing.Routing
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import io.ktor.util.KtorExperimentalAPI
import io.ktor.websocket.webSocket
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlin.concurrent.thread


@KtorExperimentalAPI
@ExperimentalCoroutinesApi
class WebSocketServer {

    fun createWebSocket(root: String, routing: Routing) {
        println("creating web socket server")
        routing.installSocketRoute(root)
    }

    private fun Routing.installSocketRoute(root: String) {
        val base            = "/message/so"
        val socketsWeb      = SocketSessionService()

        webSocket("$root$base/{type}") {

            call.parameters["type"] ?: throw Exception("missing type")
            val session = call.sessions.get<ChatSession>()

            if (session == null) {
                println( "WEB-SOCKET:: client session is null" )
                close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "No Session"))
                return@webSocket
            }

            socketsWeb.addLiveSocket(session.id, this)

            thread(start= true, name = "thread-live-socket") {
                launch {
                    socketsWeb.checkLiveSocket()
                }
            }
        }
    }
}
