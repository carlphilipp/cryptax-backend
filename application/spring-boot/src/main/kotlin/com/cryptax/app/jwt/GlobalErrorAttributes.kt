package com.cryptax.app.jwt

import org.springframework.boot.web.reactive.error.DefaultErrorAttributes
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest

@Component
class GlobalErrorAttributes : DefaultErrorAttributes(false) {

    var status = HttpStatus.BAD_REQUEST
    var message = "please provide a name"

    override fun getErrorAttributes(request: ServerRequest, includeStackTrace: Boolean): Map<String, Any> {
        val map = super.getErrorAttributes(request, includeStackTrace)
        map.put("status", status)
        map.put("message", message)
        return map
    }
}