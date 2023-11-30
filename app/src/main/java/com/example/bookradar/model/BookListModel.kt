package com.example.bookradar.model

data class BookListModel(
    val documents: MutableList<DocumentModel>,
    val meta: BookListMetaModel
)
