package com.johnmaronga.bookflow.data.remote.api

import com.johnmaronga.bookflow.data.remote.dto.GoogleBooksResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface BookApiService {

    /**
     * Search for books using Google Books API
     * @param query Search query (title, author, ISBN, etc.)
     * @param maxResults Maximum number of results (default: 40, max: 40)
     * @param startIndex Starting index for pagination (default: 0)
     */
    @GET("volumes")
    suspend fun searchBooks(
        @Query("q") query: String,
        @Query("maxResults") maxResults: Int = 40,
        @Query("startIndex") startIndex: Int = 0
    ): Response<GoogleBooksResponse>

    /**
     * Get a specific book by its ID
     * @param volumeId The Google Books volume ID
     */
    @GET("volumes/{volumeId}")
    suspend fun getBookById(
        @Path("volumeId") volumeId: String
    ): Response<GoogleBooksResponse>

    /**
     * Search books by category
     * @param category Category name
     * @param maxResults Maximum number of results
     */
    @GET("volumes")
    suspend fun searchBooksByCategory(
        @Query("q") category: String = "subject:fiction",
        @Query("maxResults") maxResults: Int = 20,
        @Query("orderBy") orderBy: String = "relevance"
    ): Response<GoogleBooksResponse>

    /**
     * Get trending/popular books
     * @param maxResults Maximum number of results
     */
    @GET("volumes")
    suspend fun getTrendingBooks(
        @Query("q") query: String = "bestseller",
        @Query("orderBy") orderBy: String = "relevance",
        @Query("maxResults") maxResults: Int = 20
    ): Response<GoogleBooksResponse>

    /**
     * Search books by author
     * @param author Author name
     * @param maxResults Maximum number of results
     */
    @GET("volumes")
    suspend fun searchBooksByAuthor(
        @Query("q") author: String,
        @Query("maxResults") maxResults: Int = 20
    ): Response<GoogleBooksResponse>

    companion object {
        const val BASE_URL = "https://www.googleapis.com/books/v1/"
    }
}
