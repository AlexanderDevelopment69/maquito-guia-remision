package com.maquitop.guiaremision.data.database

import android.content.Context
import androidx.room.*
import com.maquitop.guiaremision.data.model.Converters
import com.maquitop.guiaremision.data.model.GuiaRemision
import kotlinx.coroutines.flow.Flow

// ---- DAO ----
@Dao
interface GuiaDao {

    @Query("SELECT * FROM guias ORDER BY fechaCreacion DESC")
    fun getAllGuias(): Flow<List<GuiaRemision>>

    @Query("SELECT * FROM guias WHERE id = :id")
    suspend fun getGuiaById(id: Long): GuiaRemision?

    @Query("SELECT * FROM guias WHERE clienteNombre LIKE '%' || :query || '%' OR numeroGuia LIKE '%' || :query || '%' ORDER BY fechaCreacion DESC")
    fun searchGuias(query: String): Flow<List<GuiaRemision>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGuia(guia: GuiaRemision): Long

    @Update
    suspend fun updateGuia(guia: GuiaRemision)

    @Delete
    suspend fun deleteGuia(guia: GuiaRemision)

    @Query("SELECT COUNT(*) FROM guias")
    suspend fun getCount(): Int

    @Query("SELECT MAX(CAST(SUBSTR(numeroGuia, -4) AS INTEGER)) FROM guias WHERE numeroGuia LIKE 'G-%'")
    suspend fun getMaxNumero(): Int?
}

// ---- Database ----
@Database(entities = [GuiaRemision::class], version = 3, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun guiaDao(): GuiaDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "maquitop_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
