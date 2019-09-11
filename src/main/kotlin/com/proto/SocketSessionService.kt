package com.proto

import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.WebSocketSession
import io.ktor.util.cio.ChannelWriteException
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import java.nio.channels.ClosedChannelException
import java.util.concurrent.ConcurrentHashMap

class SocketSessionService {
    private val liveSocket = ConcurrentHashMap<String, WebSocketSession>()
    var checking: Boolean = true

    fun addLiveSocket(key: String, socket: WebSocketSession) {
        println("adding live socket")
        liveSocket[key] = socket
    }

    suspend fun checkLiveSocket() {
        while (checking) {
            delay(3000)
            liveSocket.forEach { (key, socket) ->
                try {
                    runBlocking {
                        if ( !socket.outgoing.isClosedForSend) {
                            println("socket is open")
                            socket.send(Frame.Text("ping"))
                        } else {
                            println("socket closed for send")
                            liveSocket.remove(key)
                        }
                    }
                }
                catch (e: ChannelWriteException) {
                    println("socket exception: ${e.message}")
                    liveSocket.remove(key)
                }
                catch (e: ClosedChannelException) {
                    println("socket exception: ${e.message}")
                    liveSocket.remove(key)
                }
                catch (e: Throwable) {
                    println("socket error: ${e.message}")
                    liveSocket.remove(key)
                }
            }
            println(liveSocket)
        }
    }
}