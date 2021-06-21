package com.parthiv.test.network

import com.parthiv.test.utils.handleApi
import javax.inject.Inject

class MainRepository @Inject constructor(private val dataApi: DataAPI) : MainRepositoryContract {

    override suspend fun getUser() =
        handleApi({ dataApi.getData() }, "Failed to fetch user profile")
}