package com.pedrotlf.barcalc.domain

import kotlinx.serialization.Serializable

@Serializable
data class Person(
    val id: Int,
    val name: String,
)
