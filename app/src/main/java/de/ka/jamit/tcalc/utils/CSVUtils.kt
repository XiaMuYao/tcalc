package de.ka.jamit.tcalc.utils

import android.app.Application
import android.net.Uri
import com.opencsv.CSVReader
import com.opencsv.CSVWriter
import de.ka.jamit.tcalc.R
import de.ka.jamit.tcalc.repo.Repository
import de.ka.jamit.tcalc.repo.db.RecordDao
import io.reactivex.Completable
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStreamWriter


/**
 * Offers utility functions for handling CSV export and import.
 *
 * Created by Thomas Hofmann on 08.07.20
 **/
class CSVUtils(private val repository: Repository, private val app: Application) {

    /**
     * Imports a csv file into the database. Please consider to have this executed in a non-blocking fashion.
     */
    fun importCSV(uri: Uri): Completable {
        return Completable.create { emitter ->
            val oldUserId: Long? = repository.getCurrentlySelectedUser().id
            var newUserId: Long? = null

            try {
                app.contentResolver.openInputStream(uri)?.let { inputStream ->
                    var name = app.resources.getString(R.string.import_fallback_name)
                    val cut: Int? = uri.path?.lastIndexOf('/')
                    if (cut != null && cut != -1) {
                        name = uri.path?.substring(cut + 1)?.substringBefore(".") ?: name
                    }
                    repository.addUser(name) // auto selects this user
                    newUserId = repository.getCurrentlySelectedUser().id

                    val records = streamRecords(inputStream)
                    repository.addRecords(records)

                    repository.lastImportResult = Repository.ImportResult(name, records.size)
                }
                emitter.onComplete()
            } catch (exception: Exception) {
                newUserId?.let {
                    if (it != oldUserId) { // has a new entry been inserted? Delete it right away!
                        repository.deleteUser(it)
                    }
                }
                emitter.onError(exception)
            }
        }
    }

    /**
     * Streams records from an input stream.
     */
    fun streamRecords(inputStream: InputStream): List<RecordDao> {
        val reader = CSVReader(InputStreamReader(inputStream))
        var record: Array<String?>?
        val records = mutableListOf<RecordDao>()
        val timeSpanConverter = RecordDao.TimeSpanConverter()
        val categoryConverter = RecordDao.CategoryConverter()
        while (reader.readNext().also { record = it } != null) {
            val dao = RecordDao(id = 0,
                    key = record?.get(0) ?: "",
                    timeSpan = timeSpanConverter.convertToEntityProperty(record?.get(1)?.toInt()),
                    category = categoryConverter.convertToEntityProperty(record?.get(2)?.toInt()),
                    isConsidered = record?.get(3)?.toBoolean() ?: true,
                    isIncome = record?.get(4)?.toBoolean() ?: false,
                    value = record?.get(5)?.toFloat() ?: 0.0f,
                    userId = repository.getCurrentlySelectedUser().id)
            records.add(dao)
        }
        reader.close()
        return records
    }

    /**
     * Exports a csv file.
     */
    fun exportCSV(uri: Uri): Completable {
        return Completable.create { emitter ->
            try {
                val records = repository.getAllRecordsOfCurrentlySelectedUser()

                app.contentResolver.openOutputStream(uri)?.let {
                    val writer = CSVWriter(OutputStreamWriter(it))
                    val timeSpanConverter = RecordDao.TimeSpanConverter()
                    val categoryConverter = RecordDao.CategoryConverter()
                    records.forEach { record ->
                        val data = arrayOf(
                                record.key,
                                timeSpanConverter.convertToDatabaseValue(record.timeSpan).toString(),
                                categoryConverter.convertToDatabaseValue(record.category).toString(),
                                record.isConsidered.toString(),
                                record.isIncome.toString(),
                                record.value.toString())
                        writer.writeNext(data);
                    }
                    writer.close()
                }
                emitter.onComplete()
            } catch (exception: Exception) {
                emitter.onError(exception)
            }
        }
    }
}