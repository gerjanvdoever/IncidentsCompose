package com.example.incidentscompose.data.model

data class PaginatedItemResponse<T>(
    val data: List<T>,
    val totalCount: Int
)
