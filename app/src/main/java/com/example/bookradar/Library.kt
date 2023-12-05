package com.example.bookradar

import android.content.Context
import android.os.Parcelable
import com.example.bookradar.model.BookModel
import com.google.android.gms.maps.model.LatLng
import it.skrape.core.htmlDocument
import it.skrape.fetcher.BrowserFetcher
import it.skrape.fetcher.response
import it.skrape.fetcher.skrape
import it.skrape.selects.ElementNotFoundException
import kotlinx.parcelize.Parcelize
import java.time.LocalTime

abstract class Library : Parcelable {
    abstract val nameResId: Int
    abstract val location: LatLng
    abstract val books: MutableList<BookInfo>
    abstract val openingHour: OpeningHour

    abstract suspend fun search(isbns: List<String>): BookInfo?
    abstract fun getName(context: Context): String
}

@Parcelize
data class BookInfo(
    var library: Library? = null,
    var book: BookModel? = null,
    var availability: Boolean = false,
    var loc: String = "",
    var id: String = ""
) : Parcelable

@Parcelize
data class OpeningHour(
    val open: LocalTime,
    val close: LocalTime
) : Parcelable


@Parcelize
class DongyangLibrary(
    private val baseUrl: String = "https://lib.dongyang.ac.kr",
    private val searchUrl: String = "/search/tot/result?st=FRNT&commandType=advanced&mId=&si=6&q=",
    override val openingHour: OpeningHour = OpeningHour(LocalTime.of(8, 0), LocalTime.of(22, 0)),
    override val nameResId: Int = R.string.dongyang_library,
    override val location: LatLng = LatLng(37.500062, 126.868063),
    override val books: MutableList<BookInfo> = mutableListOf()
) : Library() {
    override fun getName(context: Context): String {
        return context.getString(nameResId)
    }

    override suspend fun search(isbns: List<String>): BookInfo? {
        val book = BookModel()
        val bInfo = BookInfo(this, book)
        val listIsbn13 = isbns.filter { it.length == 13 }
        if (listIsbn13.isEmpty()) {
            return null
        }
        val doc = skrape(BrowserFetcher) {
            request {
                // TODO: try every single isbn until it has any result, at the moment, only trying the first isbn
                url = baseUrl + searchUrl + listIsbn13[0]
            }
            response {
                this
            }
        }
        try {
            doc.htmlDocument {
                "dd.title > a" {
                    findFirst {
                        skrape(BrowserFetcher) {
                            val link = attribute("href")
                            request {
                                url = baseUrl + link
                            }
                            response {
                                htmlDocument {
                                    "table.searchTable > tbody > tr" {
                                        findFirst {
                                            bInfo.availability = "span.status" {
                                                findFirst { classNames.contains("available") }
                                            }
                                            bInfo.id = "td.accessionNo" { findFirst { text } }
                                            bInfo.loc = "td.location" { findFirst { text } }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: ElementNotFoundException) {
            return null
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
        if (bInfo.loc.isBlank()) {
            return null
        }
        return bInfo
    }
}