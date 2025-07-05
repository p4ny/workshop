package com.example

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    // ติดตั้งปลั๊กอิน ContentNegotiation
    install(ContentNegotiation) {
        json()
    }

    // เรียกใช้ฟังก์ชันที่กำหนด endpoints จากไฟล์อื่น
    configureRouting()
}