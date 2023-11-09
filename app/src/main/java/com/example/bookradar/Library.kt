package com.example.bookradar

import it.skrape.core.htmlDocument
import it.skrape.fetcher.BrowserFetcher
import it.skrape.fetcher.response
import it.skrape.fetcher.skrape
import it.skrape.selects.html5.dd
import it.skrape.selects.html5.li
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

abstract class Library {
    abstract val name: String
    abstract val location: String
    abstract val books: MutableList<Book>

    abstract fun search(title: String): MutableList<Book>

}

data class BookInfo(
    val book: Book,
    var availability: Boolean,
    var loc: String,
)


class DongyangLibrary : Library() {
    private val baseUrl =
        "https://lib.dongyang.ac.kr/search/tot/result?st=KWRD&si=TOTAL&oi=DISP06&os=DESC&cpp=100&q="

    override val name = "동양미래대학교 도서관"
    override val location = "GV29+26 Seoul"
    override val books = mutableListOf<Book>()

    override fun search(title: String): MutableList<Book> {
        val encodedTitle = URLEncoder.encode(title, StandardCharsets.UTF_8.toString())
        val book = Book()
        val books = mutableListOf<Book>()
        val doc = skrape(BrowserFetcher) {
            request {
                url = baseUrl + encodedTitle
            }
            response {
                this
            }
        }
        val bElements = doc.htmlDocument {
            li {
                withClass = "items"
                findAll {
                    this
                }
            }
        }
        bElements.forEach {
            book.title = it.dd {
                withClass = "title"
                findFirst {
                    text
                }
            }
            val infos = it.dd {
                withClass = "info"
                findAll { this }
            }
            book.author = infos[0].text
            book.publisher = infos[1].text
            book.year = infos[3].text.toUInt()
            books.add(book.copy())
        }
        return books
    }
}

fun main() {
    val lib = DongyangLibrary()
    val books = lib.search("파이썬")
    println(books)
}