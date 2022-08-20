package com.anksystems.fenomy_pushk

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.PropertySource
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component
import java.io.IOException


//@PropertySource("application.properties")
@EnableConfigurationProperties(Properties::class)
@SpringBootApplication()
@ConfigurationPropertiesScan("com.anksystems.fenomy_pushk")
@Component
class FenomyPushkApplication

@Bean
@Throws(IOException::class)
fun firebaseMessaging(): FirebaseMessaging? {
    val googleCredentials = GoogleCredentials
        .fromStream(ClassPathResource("firebase-service-account.json").inputStream)
    val firebaseOptions = FirebaseOptions
        .builder()
        .setCredentials(googleCredentials)
        .build()
    val app = FirebaseApp.initializeApp(firebaseOptions, "fenomycom")
    return FirebaseMessaging.getInstance(app)
}

fun main(args: Array<String>) {
    runApplication<FenomyPushkApplication>(*args)
}
