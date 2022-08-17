package com.anksystems.fenomy_pushk.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class LogService {
    companion object {
        val logger = LoggerFactory.getLogger("pushk");
    }

    fun i(msg: String) = logger.info(msg)
    fun d(msg: String) = logger.debug(msg)
    fun e(msg: String) = logger.error(msg)
}