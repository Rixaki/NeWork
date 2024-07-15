package com.example.nework.api

import com.example.nework.auth.AuthState
import com.example.nework.dto.Event
import com.example.nework.dto.Job
import com.example.nework.dto.Media
import com.example.nework.dto.Post
import com.example.nework.dto.User
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface AppApi {
    //POST COMMANDS
    @GET("posts/{id}/newer")
    suspend fun getNewerPost(@Path("id") id: Long): Response<List<Post>>

    @GET("posts/{id}/before")
    suspend fun getBeforePost(
        @Path("id") id: Long,
        @Query("count") count: Int
    ): Response<List<Post>>

    @GET("posts/{id}/after")
    suspend fun getAfterPost(
        @Path("id") id: Long,
        @Query("count") count: Int
    ): Response<List<Post>>

    @POST("posts")
    suspend fun savePost(@Body post: Post): Response<Post>

    @DELETE("posts/{id}")
    suspend fun deletePostById(@Path("id") id: Long): Response<Unit>

    @POST("posts/{id}/likes")
    suspend fun likePost(@Path("id") id: Long): Response<Post>

    @DELETE("posts/{id}/likes")
    suspend fun unlikePost(@Path("id") id: Long): Response<Post>

    @GET("posts/latest")
    suspend fun getLatestPost(@Query("count") count: Int): Response<List<Post>>

    //EVENT COMMANDS
    @GET("events/{id}/newer")
    suspend fun getNewerEvent(@Path("id") id: Long): Response<List<Event>>

    @GET("events/{id}/before")
    suspend fun getBeforeEvent(
        @Path("id") id: Long,
        @Query("count") count: Int
    ): Response<List<Event>>

    @GET("events/{id}/after")
    suspend fun getAfterEvent(
        @Path("id") id: Long,
        @Query("count") count: Int
    ): Response<List<Event>>

    @POST("events")
    suspend fun saveEvent(@Body event: Event): Response<Event>


    @DELETE("events/{id}")
    suspend fun deleteEventById(@Path("id") id: Long): Response<Unit>

    @POST("events/{id}/likes")
    suspend fun likeEvent(@Path("id") id: Long): Response<Event>

    @DELETE("events/{id}/likes")
    suspend fun unlikeEvent(@Path("id") id: Long): Response<Event>

    @POST("events/{id}/participants")
    suspend fun takePartEvent(@Path("id") id: Long): Response<Event>

    @DELETE("events/{id}/participants")
    suspend fun leaveEvent(@Path("id") id: Long): Response<Event>

    @GET("events/latest")
    suspend fun getLatestEvent(@Query("count") count: Int): Response<List<Event>>

    //JOB COMMANDS

    @GET("{id}/jobs")
    suspend fun getJobsByUser(@Path("id") id: Long): Response<List<Job>>

    @POST("my/jobs")
    suspend fun saveJob(@Body job: Job): Response<Job>

    @DELETE("my/jobs/{id}")
    suspend fun deleteJob(@Path("id") id: Long): Response<Unit>

    //ATTACHMENT COMMAND
    @Multipart
    @POST("media")
    suspend fun upload(@Part media: MultipartBody.Part): Response<Media>
    //@PartMap for few media objects
    //auto parse for 200-code with :Media

    //USER COMMANDS
    @GET("users")
    suspend fun getUsers(): Response<List<User>>

    @GET("users/{id}")
    suspend fun getUserById(@Path("id") id: Long): Response<User>


    //USER ACCESS COMMANDS
    //https://demonuts.com/android-login-registration-using-retrofit/
    @Multipart
    @POST("users/registration")
    suspend fun signUp(
        @Part("login") login: RequestBody,
        @Part("pass") pass: RequestBody,
        @Part("name") name: RequestBody,
        @Part avatar: MultipartBody.Part?,
    ): Response<AuthState>
    //throwable UserRegisteredException()

    @FormUrlEncoded
    @POST("users/authentication")
    suspend fun signIn(
        @Field("login") login: String,
        @Field("pass") pass: String,
    ): Response<AuthState>
    //throwable PasswordNotMatchException()
    //throwable NotFoundException() (not user)
}