package com.example.bookradar

import android.content.Context
import android.content.pm.PackageManager
import it.skrape.fetcher.HttpFetcher
import it.skrape.fetcher.Method
import it.skrape.fetcher.request.UrlBuilder
import it.skrape.fetcher.response
import it.skrape.fetcher.skrape

class Crawler {

    val libraries = listOf<Library>(DongyangLibrary())
    suspend fun search(keyword: String): MutableList<BookInfo> {
        return libraries[0].search(keyword)
    }

    fun searchBook(keyword: String, context: Context) {
        val api = context.packageManager.getApplicationInfo(
            context.packageName,
            PackageManager.GET_META_DATA
        ).metaData.getString("kakao_api")
        val doc = skrape(HttpFetcher) {
            request {
                url {
                    protocol = UrlBuilder.Protocol.HTTPS
                    host = "dapi.kakao.com"
                    path = "/v3/search/book"
                    queryParam {
                        "query" to keyword
                    }
                }
                method = Method.GET
                headers = mapOf("Authorization" to "KakaoAK $api")
            }
            response{
                this
            }
        }


    }

}
