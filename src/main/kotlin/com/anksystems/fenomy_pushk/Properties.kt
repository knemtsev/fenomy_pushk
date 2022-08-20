package com.anksystems.fenomy_pushk

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties("push")
data class Properties @ConstructorBinding constructor(
    var poolSendPushSize: Int = 1000,
    var queueUpdateStatusSize: Int = 100_000,
    var queueSendPushSize: Int = 100_000,
    var dbUrl: String,
    var dbUser: String,
    var dbPassword: String,
    var dbDriver: String,
)