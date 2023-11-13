package com.example.bookradar

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import it.skrape.core.htmlDocument
import it.skrape.fetcher.BrowserFetcher
import it.skrape.fetcher.response
import it.skrape.fetcher.skrape
import org.jsoup.Jsoup
import org.jsoup.parser.Parser
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
        if (!filename.endsWith("json")) {
            throw Exception("File is not Json")
        }
        val gson = Gson()
        val json = gson.toJson(books)

        val file = File(context.filesDir, filename)

        try {
            file.writeText(json)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    protected fun loadBooksFromJson(context: Context): List<BookInfo> {
        if (!filename.endsWith("json")) {
            throw Exception("File is not Json")
        }
        val file = File(context.filesDir, filename)

        if (file.exists()) {
            val json = file.readText()
            val typeToken = object : TypeToken<List<BookInfo>>() {}.type
            return Gson().fromJson(json, typeToken)
        }

        return emptyList()
    }
}

data class BookInfo(
    var book: Book? = null,
    var urlImage: String? = null,
    var availability: Boolean = false,
    var loc: String = "",
    var id: String = ""
)


class DongyangLibrary : Library() {
    private val baseUrl = "https://lib.dongyang.ac.kr/"
    private val searchUrl =
        "search/tot/result?st=KWRD&si=TOTAL&oi=DISP06&os=DESC&cpp=10&q="

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
                url = baseUrl + searchUrl + encodedTitle
            }
            response {
                this
            }
        }
        val bElements = doc.htmlDocument {
            "#divContent > div > div.briefContent > div.result > form > fieldset > ul" {
                findAll("li")
            }
        }
        for (el in bElements) {
            book.title = el.findFirst("dd.title > a").text
            book.author = el.findFirst("dd:nth-child(10)").text
            book.publisher = el.findFirst("dd:nth-child(12)").text
            bInfo.loc = el.findFirst("dd:nth-child(14)").text
            book.year = el.findFirst("dd:nth-child(16)").text.toUInt()
            bInfo.id = el.findFirst("a").eachHref[0].split("?")[0].split("/").last()
            book.isbn = getIsbn(bInfo.id)
            bInfo.book = book.copy()
            books.add(bInfo.copy())
            println(bInfo)
        }
        this@DongyangLibrary.books.addAll(books.toMutableList())
        return this@DongyangLibrary.books
    }

    private fun getIsbn(id: String): List<String> {
        val url = baseUrl + "search/prevDetail/" + id
        val doc = Jsoup.connect(url).get().parser(Parser.xmlParser())
        return doc.selectXpath("//profile[name='ISBN']/value").text().split("<br/>").dropLast(1)
    }
}


fun main() {
    val lib = DongyangLibrary()
    lib.search("파이썬")
}