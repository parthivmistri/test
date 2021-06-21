package com.parthiv.test.data.core

import com.google.gson.annotations.SerializedName

class IvoryApiError(
    @SerializedName("message") val errorMessage: String = "Problem occurred while performing action",
) : RuntimeException(errorMessage)