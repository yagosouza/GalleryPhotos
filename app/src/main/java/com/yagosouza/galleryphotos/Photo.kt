package com.yagosouza.galleryphotos

data class Photo(
    val id: Long,
    val name: String,
    val path: String,
    val dateAdded: Long
)