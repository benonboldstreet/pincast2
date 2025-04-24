package com.example.pincast.data.cache

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

/**
 * Entity for storing CID metadata
 */
@Entity(tableName = "cid_metadata")
data class CidMetadata(
    @PrimaryKey val cid: String,
    val name: String,
    val size: Long = 0,
    val mimeType: String = "",
    val accessCount: Int = 0,
    val lastAccessed: LocalDateTime = LocalDateTime.now(),
    val isFavorite: Boolean = false,
    val tags: String = "", // Comma-separated tags
    val localPath: String? = null, // Path to local cached file if available
    val notes: String = ""
)

/**
 * Data Access Object for CID metadata
 */
@Dao
interface CidMetadataDao {
    @Query("SELECT * FROM cid_metadata ORDER BY lastAccessed DESC")
    fun getAllMetadata(): Flow<List<CidMetadata>>
    
    @Query("SELECT * FROM cid_metadata WHERE cid = :cid")
    suspend fun getMetadata(cid: String): CidMetadata?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(metadata: CidMetadata)
    
    @Update
    suspend fun update(metadata: CidMetadata)
    
    @Delete
    suspend fun delete(metadata: CidMetadata)
    
    @Query("DELETE FROM cid_metadata WHERE cid = :cid")
    suspend fun deleteByCid(cid: String)
    
    @Query("UPDATE cid_metadata SET accessCount = accessCount + 1, lastAccessed = :timestamp WHERE cid = :cid")
    suspend fun incrementAccessCount(cid: String, timestamp: LocalDateTime = LocalDateTime.now())
    
    @Query("SELECT * FROM cid_metadata WHERE isFavorite = 1 ORDER BY lastAccessed DESC")
    fun getFavorites(): Flow<List<CidMetadata>>
    
    @Query("UPDATE cid_metadata SET isFavorite = :isFavorite WHERE cid = :cid")
    suspend fun setFavorite(cid: String, isFavorite: Boolean)
    
    @Query("SELECT * FROM cid_metadata WHERE name LIKE '%' || :query || '%' OR tags LIKE '%' || :query || '%' ORDER BY lastAccessed DESC")
    fun search(query: String): Flow<List<CidMetadata>>
    
    @Query("SELECT * FROM cid_metadata WHERE cid IN (:cids)")
    suspend fun getMetadataForCids(cids: List<String>): List<CidMetadata>
    
    @Query("SELECT COUNT(*) FROM cid_metadata")
    suspend fun getCount(): Int
}

/**
 * Type converters for Room database
 */
class Converters {
    @TypeConverter
    fun fromLocalDateTime(value: LocalDateTime?): String? {
        return value?.toString()
    }
    
    @TypeConverter
    fun toLocalDateTime(value: String?): LocalDateTime? {
        return value?.let { LocalDateTime.parse(it) }
    }
}

/**
 * Room database for CID metadata
 */
@Database(entities = [CidMetadata::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class CidDatabase : RoomDatabase() {
    abstract fun cidMetadataDao(): CidMetadataDao
    
    companion object {
        @Volatile
        private var INSTANCE: CidDatabase? = null
        
        fun getDatabase(context: Context): CidDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CidDatabase::class.java,
                    "cid_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
} 