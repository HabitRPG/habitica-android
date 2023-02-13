package com.habitrpg.common.habitica.api

class Server {
    private var addr: String

    constructor(addr: String) : this(addr, true) {}
    private constructor(addr: String, attachSuffix: Boolean) {
        if (attachSuffix) {
            if (addr.endsWith("/api/v4") || addr.endsWith("/api/v4/")) {
                this.addr = addr
            } else if (addr.endsWith("/")) {
                this.addr = addr + "api/v4/"
            } else {
                this.addr = "$addr/api/v4/"
            }
        } else {
            this.addr = addr
        }
    }

    constructor(server: Server) {
        addr = server.toString()
    }

    override fun toString(): String {
        return addr
    }
}
