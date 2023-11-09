package com.example.bookradar

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import it.skrape.core.htmlDocument
import it.skrape.fetcher.BrowserFetcher
import it.skrape.fetcher.response
import it.skrape.fetcher.skrape
import it.skrape.selects.html5.a
import it.skrape.selects.html5.dd
import it.skrape.selects.html5.div
import it.skrape.selects.html5.h3
import it.skrape.selects.html5.li
import it.skrape.selects.html5.p
import it.skrape.selects.html5.tbody
import org.jsoup.select.Collector.findFirst
import java.io.File
import java.io.IOException
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

abstract class Library {
    abstract val name: String
    abstract val location: String
    abstract val books: MutableList<BookInfo>
    abstract val filename: String

    abstract fun search(title: String): MutableList<BookInfo>

    protected fun saveBooksToJson(context: Context) {
        if (!filename.endsWith("json")){
            throw Exception("File is not Json")
        }
        val gson = Gson()
        val json = gson.toJson(books)

        val file = File(context.filesDir, filename)

        try{
            file.writeText(json)
        }catch(e:IOException){
            e.printStackTrace()
        }
    }

    protected fun loadBooksFromJson(context: Context): List<BookInfo>{
        if (!filename.endsWith("json")){
            throw Exception("File is not Json")
        }
        val file = File(context.filesDir, filename)

        if(file.exists()){
            val json = file.readText()
            val typeToken = object:TypeToken<List<BookInfo>>() {}.type
            return Gson().fromJson(json, typeToken)
        }

        return emptyList()
    }
}

data class BookInfo(
    var book: Book? = null,
    var availability: Boolean = false,
    var loc: String = "",
    var pageUrl: String = ""
)


class DongyangLibrary : Library() {
    private val baseUrl = "https://lib.dongyang.ac.kr/"
    private val searchUrl =
        "https://lib.dongyang.ac.kr/search/tot/result?st=KWRD&si=TOTAL&oi=DISP06&os=DESC&cpp=100&q="

    override val name = "동양미래대학교 도서관"
    override val location = "GV29+26 Seoul"
    override val books = mutableListOf<BookInfo>()
    override val filename: String = "book_dongyang.json"

    override fun search(title: String): MutableList<BookInfo> {
        val encodedTitle = URLEncoder.encode(title, StandardCharsets.UTF_8.toString())
        val book = Book()
        val bInfo = BookInfo()
        val books = mutableListOf<BookInfo>()
        val doc = skrape(BrowserFetcher) {
            request {
                url = searchUrl + encodedTitle
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
        for (el in bElements) {
            el.dd {
                withClass = "info"
                findByIndex(1) {
                    book.publisher = text
                }
                findByIndex(3) {
                    book.year = text.toUInt()
                }

            }
            val href = el.a {
                findFirst {
                    eachHref[0]
                }
            }
            val bDoc = skrape(BrowserFetcher) {
                request {
                    url = baseUrl + href
                }
                response { this }
            }
            bDoc.htmlDocument {
                div {
                    withClass = "profileHeader"
                    findFirst {
                        h3 {
                            findFirst { book.title = text }
                        }
                        p {
                            findFirst { book.author = text }
                        }
                    }
                }
                tbody {
                    "th:contains(ISBN) + td" {

                        findFirst {
                            book.isbn = text.split(" ").map {
                                it.filter(Char::isDigit).toULong()
                            }
                            if(this@DongyangLibrary.books.any{it.book?.isbn == book.isbn}){
                                return@findFirst
                                TODO("have to make it so that if there is already a books with the same isbn, continue the loop")
                            }

                        }
                    }
                    "th:contains(언어) + td" {
                        findFirst { book.lang = text }
                    }

                }
                "tr.first" {
                    findFirst {
                        ".status" {
                            findFirst {
                                bInfo.availability = text.contains("가능")
                            }
                        }
                        ".location" {
                            findFirst {
                                bInfo.loc = text
                            }
                        }
                        ".callNum" {
                            findFirst {
                                bInfo.loc += " $text"
                            }
                        }

                    }
                }

            }
            bInfo.book = book
            bInfo.pageUrl = bDoc.baseUri
            books.add(bInfo.copy())
        }
        this@DongyangLibrary.books.addAll(books.toMutableList())
        return books
    }

}

fun main() {
    val lib = DongyangLibrary()
    val books = lib.search("파이썬")
    println(books)
}