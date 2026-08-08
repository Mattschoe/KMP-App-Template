package com.mattschoe.apptemplate

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform