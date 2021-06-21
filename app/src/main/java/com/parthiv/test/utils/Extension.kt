package com.parthiv.test.utils

import com.google.gson.JsonSyntaxException
import com.parthiv.test.data.core.ApiError
import com.parthiv.test.data.core.ApiResult
import com.parthiv.test.data.core.IvoryApiError
import com.parthiv.test.data.core.Success
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Response

suspend fun <T : Any> handleApi(
    call: suspend () -> Response<T>,
    errorMessage: String = "Some errors occurred, Please try again later"
): ApiResult<T> {
    try {
        val response = call()
        if (response.isSuccessful) {
            response.body()?.let { return Success(it) }
        }
        response.errorBody()?.let { return handleErrorBody(it, errorMessage) }
        return ApiError(IvoryApiError(errorMessage))
    } catch (e: Exception) {
        return ApiError(IvoryApiError(errorMessage))
    }
}

private fun handleErrorBody(it: ResponseBody, errorMessage: String) = try {
    val errorString = it.string()
    val errorObject = JSONObject(errorString)
    ApiError(
        IvoryApiError(
            errorMessage = if (errorObject.has("message")) {
                errorObject.getString("message")
            } else {
                "Error occurred, Try again Later"
            }
        )
    )
} catch (ignored: JsonSyntaxException) {
    ApiError(IvoryApiError(errorMessage))
}