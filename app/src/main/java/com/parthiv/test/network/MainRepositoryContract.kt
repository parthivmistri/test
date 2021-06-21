package com.parthiv.test.network

import com.parthiv.test.data.core.ApiResult
import com.parthiv.test.data.core.BaseResponse
import com.parthiv.test.data.user.User

interface MainRepositoryContract {
    suspend fun getUser(): ApiResult<BaseResponse<User>>
}