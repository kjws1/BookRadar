package com.example.bookradar.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class BookModel(
        var title: String? = null,
        var contents: String? = null,
        var url: String? = null,
        var isbn: String? = null,
        var datetime: String? = null,
        var authors: List<String>? = null,
        var publisher: String? = null,
        var translators: List<String>? = null,
        var price: Int? = null,
        var sale_price: Int? = null,
        var thumbnail: String? = null
) : Parcelable
