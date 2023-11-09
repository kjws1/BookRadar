package com.example.bookradar

class Crawler {
    init {
    }

    companion object {
        val libraries = listOf<Library>(DongyangLibrary())


    }

}

fun main() {
    println(Crawler.libraries[0].search("파이썬"))

}
