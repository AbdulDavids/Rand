package com.dreamteam.rand.data.dao

import android.util.Log
import androidx.room.*
import com.dreamteam.rand.data.entity.Category
import kotlinx.coroutines.flow.Flow

// handles all category database operations
@Dao
interface CategoryDao {
    // get all categories for a user
    @Query("SELECT * FROM categories WHERE userId = :userId")
    fun getCategories(userId: String): Flow<List<Category>>

    // get categories of a specific type (income/expense)
    @Query("SELECT * FROM categories WHERE userId = :userId AND type = :type")
    fun getCategoriesByType(userId: String, type: String): Flow<List<Category>>

    // get a specific category
    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getCategory(id: Long): Category?

    // add a new category or replace if exists
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category): Long

    // add a new category or replace if exists
    @Upsert
    suspend fun upsertCategory(category: Category)

    // add multiple categories with conflict resolution
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<Category>)

    // update a category
    @Update
    suspend fun updateCategory(category: Category)

    // remove a category
    @Delete
    suspend fun deleteCategory(category: Category)

    // get system default categories
    @Query("""
        SELECT * FROM categories 
        WHERE userId = :userId 
        AND isDefault = 1
    """)
    fun getDefaultCategories(userId: String): Flow<List<Category>>

    // delete a user
    @Transaction
    @Query("DELETE FROM categories WHERE userId = :userId")
    suspend fun deleteAllUserCategories(userId: String)

    // sync categories local cache with firebase data
    @Transaction
    suspend fun syncCategories(userId: String, categories: List<Category>) {
        Log.d("CategoryDao", "Starting category sync for user $userId with ${categories.size} categories")
        deleteAllUserCategories(userId)
        Log.d("CategoryDao", "Deleted existing categories for user $userId")
        insertCategories(categories)
        Log.d("CategoryDao", "Inserted ${categories.size} categories for user $userId")
        val count = getCategoryCount(userId)
        Log.d("CategoryDao", "Final category count for user $userId: $count")
    }

    // get total count of categories for a user
    @Query("SELECT COUNT(*) FROM categories WHERE userId = :userId")
    suspend fun getCategoryCount(userId: String): Int
}