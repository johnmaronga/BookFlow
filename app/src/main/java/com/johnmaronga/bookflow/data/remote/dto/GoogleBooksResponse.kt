package com.johnmaronga.bookflow.data.remote.dto

import com.google.gson.annotations.SerializedName

data class GoogleBooksResponse(
    @SerializedName("kind")
    val kind: String?,
    @SerializedName("totalItems")
    val totalItems: Int?,
    @SerializedName("items")
    val items: List<BookItemDto>?
)

data class BookItemDto(
    @SerializedName("id")
    val id: String,
    @SerializedName("volumeInfo")
    val volumeInfo: VolumeInfoDto,
    @SerializedName("saleInfo")
    val saleInfo: SaleInfoDto?
)

data class VolumeInfoDto(
    @SerializedName("title")
    val title: String,
    @SerializedName("authors")
    val authors: List<String>?,
    @SerializedName("description")
    val description: String?,
    @SerializedName("imageLinks")
    val imageLinks: ImageLinksDto?,
    @SerializedName("industryIdentifiers")
    val industryIdentifiers: List<IndustryIdentifierDto>?,
    @SerializedName("publishedDate")
    val publishedDate: String?,
    @SerializedName("pageCount")
    val pageCount: Int?,
    @SerializedName("categories")
    val categories: List<String>?,
    @SerializedName("averageRating")
    val averageRating: Float?,
    @SerializedName("ratingsCount")
    val ratingsCount: Int?
)

data class ImageLinksDto(
    @SerializedName("smallThumbnail")
    val smallThumbnail: String?,
    @SerializedName("thumbnail")
    val thumbnail: String?
)

data class IndustryIdentifierDto(
    @SerializedName("type")
    val type: String,
    @SerializedName("identifier")
    val identifier: String
)

data class SaleInfoDto(
    @SerializedName("country")
    val country: String?,
    @SerializedName("saleability")
    val saleability: String?
)
