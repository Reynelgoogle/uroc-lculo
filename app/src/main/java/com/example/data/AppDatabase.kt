package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "patient_cases")
data class PatientCase(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val timestamp: Long = System.currentTimeMillis(),
    val aiSummary: String? = null,
    val aiSummaryStatus: String = "NONE" // NONE, LOADING, SUCCESS, ERROR
)

@Entity(tableName = "calculation_records")
data class CalculationRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val caseId: Long? = null, // Associated case, if any
    val calculatorType: String, // IMC, HIDRATACION, IPSS, ECOG, KPS, SENIC, NNIS_NHSN
    val inputParameters: String, // Formatted descriptive parameter text
    val result: String, // Formatted result and WHO/Severity classification
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface UroDao {
    // Patient Case Queries
    @Query("SELECT * FROM patient_cases ORDER BY timestamp DESC")
    fun getAllCases(): Flow<List<PatientCase>>

    @Query("SELECT * FROM patient_cases WHERE id = :caseId LIMIT 1")
    suspend fun getCaseById(caseId: Long): PatientCase?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCase(patientCase: PatientCase): Long

    @Update
    suspend fun updateCase(patientCase: PatientCase)

    @Query("DELETE FROM patient_cases WHERE id = :caseId")
    suspend fun deleteCaseById(caseId: Long)

    @Query("DELETE FROM patient_cases")
    suspend fun deleteAllCases()

    // Calculation Record Queries
    @Query("SELECT * FROM calculation_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<CalculationRecord>>

    @Query("SELECT * FROM calculation_records WHERE caseId = :caseId ORDER BY timestamp DESC")
    fun getRecordsForCase(caseId: Long): Flow<List<CalculationRecord>>

    @Query("SELECT * FROM calculation_records WHERE caseId = :caseId ORDER BY timestamp DESC")
    suspend fun getRecordsForCaseSync(caseId: Long): List<CalculationRecord>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: CalculationRecord): Long

    @Query("DELETE FROM calculation_records WHERE id = :id")
    suspend fun deleteRecordById(id: Long)

    @Query("DELETE FROM calculation_records")
    suspend fun deleteAllRecords()
}

@Database(entities = [PatientCase::class, CalculationRecord::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun uroDao(): UroDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "uro_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class UroRepository(private val uroDao: UroDao) {
    val allCases: Flow<List<PatientCase>> = uroDao.getAllCases()
    val allRecords: Flow<List<CalculationRecord>> = uroDao.getAllRecords()

    suspend fun getCaseById(caseId: Long): PatientCase? = uroDao.getCaseById(caseId)

    suspend fun insertCase(patientCase: PatientCase): Long = uroDao.insertCase(patientCase)

    suspend fun updateCase(patientCase: PatientCase) = uroDao.updateCase(patientCase)

    suspend fun deleteCaseById(caseId: Long) = uroDao.deleteCaseById(caseId)

    suspend fun deleteAllCases() = uroDao.deleteAllCases()

    fun getRecordsForCase(caseId: Long): Flow<List<CalculationRecord>> = uroDao.getRecordsForCase(caseId)

    suspend fun getRecordsForCaseSync(caseId: Long): List<CalculationRecord> = uroDao.getRecordsForCaseSync(caseId)

    suspend fun insertRecord(record: CalculationRecord): Long = uroDao.insertRecord(record)

    suspend fun deleteRecordById(id: Long) = uroDao.deleteRecordById(id)

    suspend fun deleteAllRecords() = uroDao.deleteAllRecords()
}
