package com.fajar.sub2storyapp.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.fajar.sub2storyapp.data.remote.api.ApiService
import com.fajar.sub2storyapp.data.remote.response.ListStoryItem

class StoryPaging(private val apiService: ApiService, private val token: String) :
    PagingSource<Int, ListStoryItem>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ListStoryItem> {
        return try {
            val page = params.key ?: INITIAL_PAGE_INDEX
            val response = apiService.getStories(
                token = token,
                page = page,
                size = params.loadSize
            )
            val data = response.listStory

            LoadResult.Page(
                data = data,
                prevKey = if (page == INITIAL_PAGE_INDEX) null else page - 1,
                nextKey = if (data.isNullOrEmpty()) null else page + 1
            )
        } catch (exception: Exception) {
            LoadResult.Error(exception)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, ListStoryItem>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    //Initial default page Index
    private companion object {
        const val INITIAL_PAGE_INDEX = 1
    }
}