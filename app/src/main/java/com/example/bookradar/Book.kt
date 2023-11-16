package com.example.bookradar

data class Book(
    var title: String = "",
    var author: String = "",
    var year: UInt = 0U,
    var isbn: List<String> = listOf(),
    var publisher: String ="",
    var lang: String = ""
)