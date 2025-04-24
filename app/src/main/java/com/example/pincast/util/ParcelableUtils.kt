package com.example.pincast.util

import android.os.Parcel
import android.os.Parcelable
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object ParcelableUtils {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    
    @JvmStatic
    fun writeLocalDateTime(parcel: Parcel, dateTime: LocalDateTime?) {
        parcel.writeString(dateTime?.format(formatter))
    }
    
    @JvmStatic
    fun readLocalDateTime(parcel: Parcel): LocalDateTime? {
        val dateTimeString = parcel.readString()
        return if (dateTimeString != null) {
            LocalDateTime.parse(dateTimeString, formatter)
        } else {
            null
        }
    }
}

class LocalDateTimeParcelable(val value: LocalDateTime) : Parcelable {
    constructor(parcel: Parcel) : this(
        ParcelableUtils.readLocalDateTime(parcel) ?: LocalDateTime.now()
    )
    
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        ParcelableUtils.writeLocalDateTime(parcel, value)
    }
    
    override fun describeContents(): Int {
        return 0
    }
    
    companion object CREATOR : Parcelable.Creator<LocalDateTimeParcelable> {
        override fun createFromParcel(parcel: Parcel): LocalDateTimeParcelable {
            return LocalDateTimeParcelable(parcel)
        }
        
        override fun newArray(size: Int): Array<LocalDateTimeParcelable?> {
            return arrayOfNulls(size)
        }
    }
} 