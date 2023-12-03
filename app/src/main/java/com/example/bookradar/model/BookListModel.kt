package com.example.bookradar.model

data class BookListModel(
        val documents: MutableList<BookModel>,
        val meta: BookListMetaModel
)
