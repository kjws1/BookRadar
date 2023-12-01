package com.example.bookradar

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.example.bookradar.model.BookModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import it.skrape.core.htmlDocument
import it.skrape.fetcher.BrowserFetcher
import it.skrape.fetcher.response
import it.skrape.fetcher.skrape
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.parser.Parser
import java.io.File
import java.io.IOException
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

abstract class Library {
    abstract val name: String
    abstract val location: String
    abstract val books: MutableList<BookInfo>
    abstract val filename: String

    abstract suspend fun search(isbns: List<String>): MutableList<BookInfo>

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
        var book: BookModel? = null,
        var availability: Boolean = false,
        var loc: String = "",
        var id: String = ""
)


class DongyangLibrary : Library() {
    private val baseUrl = "https://lib.dongyang.ac.kr/"
    private val searchUrl =
            "search/tot/result?st=FRNT&commandType=advanced&mId=&si=6&b0=and&weight0=&si=2&q=&b1=and&weight1=&si=3&q=&weight2=&lmt0=TOTAL&_lmt0=on&lmtsn=000000000001&lmtst=OR&_lmt0=on&_lmt0=on&_lmt0=on&_lmt0=on&inc=TOTAL&_inc=on&_inc=on&_inc=on&_inc=on&lmt1=TOTAL&lmtsn=000000000003&lmtst=OR&rf=&rt=&range=000000000021&cpp=10&msc=10000&q="

    override val name = "동양미래대학교 도서관"
    override val location = "GV29+26 Seoul"
    override val books = mutableListOf<BookInfo>()
    override val filename: String = "book_dongyang.json"

    override suspend fun search(isbns: List<String>): MutableList<BookInfo> {
        val book = BookModel()
        val bInfo = BookInfo(book)
        val books = mutableListOf<BookInfo>()
        val doc = skrape(BrowserFetcher) {
            request {
                // TODO: try every single isbn until it has any result, at the moment, only trying the first isbn
                url = baseUrl + searchUrl + isbns[0]
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
        this@DongyangLibrary.books.addAll(books.toMutableList())
        return this@DongyangLibrary.books
    }

    private fun getIsbn(id: String): List<String> {
        val url = baseUrl + "search/prevDetail/" + id
        val doc = Jsoup.connect(url).get().parser(Parser.xmlParser())
        return doc.selectXpath("//profile[name='ISBN']/value").text().split("<br/>").dropLast(1)
    }
    private fun getBitmapFromUrl(src: String): Bitmap? {
        try {
            val url = URL(src)
                val connection = url.openConnection()
                connection.doInput = true
                connection.connect()
                val input = connection.getInputStream()
                return BitmapFactory.decodeStream(input)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }
}