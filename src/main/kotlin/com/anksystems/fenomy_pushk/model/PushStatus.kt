package com.anksystems.fenomy_pushk.model

enum class PushStatus(val status: String) {
    NEW("new"),
    PROCESSED("processed"),
    SUBMITTED("submitted"),
    FAILED("failed")
}