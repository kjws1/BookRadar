package com.example.bookradar

class Crawler {

    private val libraries = listOf<Library>(DongyangLibrary())
    suspend fun getBookInfos(isbns: List<String>): MutableList<BookInfo>? {
        val books: MutableList<BookInfo> = mutableListOf()
        for (library in libraries) {
            books.add(library.search(isbns) ?: continue)
        }
        if (books.isEmpty()) {
            return null
        }
        return books
    }
}
