package com.anksystems.fenomy_pushk

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.context.annotation.Configuration

@ConstructorBinding
@ConfigurationProperties("push")
data class Properties @ConstructorBinding constructor(
    var sendWorkers: Int,
    var pgWorkers: Int,
    var dbUrl: String,
    var dbUser: String,
    var dbPassword: String,
    var dbDriver: String,
)