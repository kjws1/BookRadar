package com.example.bookradar.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class DocumentModel(
    val title: String,
    val contents: String,
    val url: String,
    val isbn: String,
    val datetime: String,
    val authors: List<String>,
    val publisher: String,
    val translators: List<String>,
    val price: Int,
    val sale_price: Int,
    val thumbnail: String
) : Parcelable
