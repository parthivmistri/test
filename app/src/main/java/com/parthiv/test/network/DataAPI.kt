package com.parthiv.test.network

import com.parthiv.test.data.core.BaseResponse
import com.parthiv.test.data.user.User
import retrofit2.Response
import retrofit2.http.GET

interface DataAPI {

    @GET("v1/dashboard/get_user_settings/")
    suspend fun getData(): Response<BaseResponse<User>>
}