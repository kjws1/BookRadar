package com.example.bookradar

class Crawler  {

    val libraries = listOf<Library>(DongyangLibrary())
    suspend fun search(keyword:String):MutableList<BookInfo> {
        return libraries[0].search(keyword)
    }

}
