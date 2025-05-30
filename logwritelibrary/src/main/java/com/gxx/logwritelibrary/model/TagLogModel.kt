package com.gxx.logwritelibrary.model

import android.os.Parcel
import android.os.Parcelable

class TagLogModel(
    val tag:String,
    val message:String,
    val json: String,
    val createTime:Long,
    val time:String
    ): Parcelable {

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(tag)
        dest.writeString(message)
        dest.writeString(json)
        dest.writeLong(createTime)
        dest.writeString(time)
    }

    companion object CREATOR : Parcelable.Creator<TagLogModel> {
        override fun createFromParcel(parcel: Parcel): TagLogModel {
            return TagLogModel(
                parcel.readString() ?: "",
                parcel.readString() ?: "",
                parcel.readString() ?: "",
                parcel.readLong(),
                parcel.readString() ?: ""
            )
        }

        override fun newArray(size: Int): Array<TagLogModel?> {
            return arrayOfNulls(size)
        }
    }

}