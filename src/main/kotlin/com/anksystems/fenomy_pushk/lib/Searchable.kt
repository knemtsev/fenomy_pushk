package com.anksystems.fenomy_pushk.lib

interface Searchable<T> {
    fun compare(e:T): Boolean
}