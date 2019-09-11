package com.proto

import io.ktor.application.Application
import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.DefaultHeaders
import io.ktor.http.cio.websocket.*
import io.ktor.routing.routing
import io.ktor.sessions.*
import io.ktor.util.KtorExperimentalAPI
import io.ktor.util.generateNonce
import io.ktor.websocket.DefaultWebSocketServerSession
import io.ktor.websocket.WebSockets
import io.ktor.websocket.webSocket
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.consumeEach
import java.time.Duration
import kotlin.concurrent.thread

class MainApp

fun main(args: Array<String>) : Unit = io.ktor.server.netty.EngineMain.main(args)

@KtorExperimentalAPI
fun Application.socketModule() = runBlocking {
    var socketserver: DefaultWebSocketServerSession? = null
    val sessionService = SocketSessionService()
    install(DefaultHeaders)
    install(CallLogging)
    install(WebSockets) {
        pingPeriod      = Duration.ofSeconds(100)
        timeout         = Duration.ofSeconds(100)
        maxFrameSize    = Long.MAX_VALUE
    }
    install(Sessions) {
        cookie<ChatSession>("SESSION")
    }

    intercept(ApplicationCallPipeline.Features) {
        if (call.sessions.get<ChatSession>() == null) {
            val sessionID = generateNonce()
            println("generated Session: $sessionID")
            call.sessions.set(ChatSession(sessionID))
        }
    }

    routing {
        webSocket("/home") {
            val chatSession = call.sessions.get<ChatSession>()
            println("request session: $chatSession")

            if (chatSession == null) {
                close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "empty Session"))
                return@webSocket
            }
            send(Frame.Text("connected to server"))
            sessionService.addLiveSocket(chatSession.session, this)
            sessionService.checkLiveSocket()
        }
    }

    thread(start = true, name = "socket-monitor") {
        launch {
            sessionService.checkLiveSocket()
        }
    }
}

