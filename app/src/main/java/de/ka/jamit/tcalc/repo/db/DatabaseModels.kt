package de.ka.jamit.tcalc.repo.db

import androidx.annotation.DrawableRes
import androidx.annotation.Keep
import de.ka.jamit.tcalc.R
import io.objectbox.annotation.Convert
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.converter.PropertyConverter

@Keep
@Entity
data class RecordDao(
        @Id var id: Long = 142547635L,
        val key: String,
        val value: Float = 0.0f,
        @Convert(converter = TimeSpanConverter::class, dbType = Int::class)
        val timeSpan: TimeSpan = TimeSpan.MONTHLY,
        @Convert(converter = CategoryConverter::class, dbType = Int::class)
        val category: Category = Category.COMMON,
        val isConsidered: Boolean = true,
        val isIncome: Boolean = false,
        val userId: Long) {

    enum class TimeSpan(val id: Int) {
        MONTHLY(0), QUARTERLY(1), YEARLY(2);
    }

    class TimeSpanConverter : PropertyConverter<TimeSpan, Int> {
        override fun convertToDatabaseValue(entityProperty: TimeSpan?): Int {
            return entityProperty?.id ?: 0
        }

        override fun convertToEntityProperty(databaseValue: Int?): TimeSpan {
            val entry = TimeSpan.values().find { it.id == databaseValue }
            return entry ?: TimeSpan.MONTHLY
        }
    }

    enum class Category(val id: Int, @DrawableRes val resId: Int) {
        COMMON(0, R.drawable.ic_home),
        HOUSE(1, R.drawable.ic_profile),
        SAVING(2, R.drawable.ic_settings),
        CAR(3, R.drawable.ic_home)
    }

    class CategoryConverter : PropertyConverter<Category, Int> {
        override fun convertToDatabaseValue(entityProperty: Category?): Int {
            return entityProperty?.id ?: 0
        }

        override fun convertToEntityProperty(databaseValue: Int?): Category {
            val entry = Category.values().find { it.id == databaseValue }
            return entry ?: Category.COMMON
        }
    }
}

@Keep
@Entity
data class UserDao(
        @Id var id: Long = 122547635L,
        val name: String = "",
        val selected: Boolean = false
)



