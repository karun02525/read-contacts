package io.chingari.lib


import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.provider.ContactsContract
import io.chingari.lib.ColumnMapper.mapDisplayName
import io.chingari.lib.ColumnMapper.mapEmail
import io.chingari.lib.ColumnMapper.mapInVisibleGroup
import io.chingari.lib.ColumnMapper.mapPhoneNumber
import io.chingari.lib.ColumnMapper.mapPhoto
import io.chingari.lib.ColumnMapper.mapStarred
import io.chingari.lib.ColumnMapper.mapThumbnail
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import java.util.*


class RxContacts private constructor(context: Context) {

    companion object {
        private val PROJECTION = arrayOf(
            ContactsContract.Data.CONTACT_ID,
            ContactsContract.Data.DISPLAY_NAME_PRIMARY,
            ContactsContract.Data.STARRED,
            ContactsContract.Data.PHOTO_URI,
            ContactsContract.Data.PHOTO_THUMBNAIL_URI,
            ContactsContract.Data.DATA1,
            ContactsContract.Data.MIMETYPE,
            ContactsContract.Data.IN_VISIBLE_GROUP
        )

        fun fetch(context: Context): Observable<Contact> {
            return Observable.create { e -> RxContacts(context).fetch(e) }
        }
    }

    private val mResolver: ContentResolver = context.contentResolver


    @SuppressLint("UseSparseArrays")
    private fun fetch(emitter: ObservableEmitter<Contact>) {
        val contacts = HashMap<Long, Contact>()
        val cursor = createCursor()
        cursor!!.moveToFirst()
        val idColumnIndex = cursor.getColumnIndex(ContactsContract.Data.CONTACT_ID)
        val inVisibleGroupColumnIndex =
            cursor.getColumnIndex(ContactsContract.Data.IN_VISIBLE_GROUP)
        val displayNamePrimaryColumnIndex =
            cursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME_PRIMARY)
        val starredColumnIndex = cursor.getColumnIndex(ContactsContract.Data.STARRED)
        val photoColumnIndex = cursor.getColumnIndex(ContactsContract.Data.PHOTO_URI)
        val thumbnailColumnIndex = cursor.getColumnIndex(ContactsContract.Data.PHOTO_THUMBNAIL_URI)
        val mimetypeColumnIndex = cursor.getColumnIndex(ContactsContract.Data.MIMETYPE)
        val dataColumnIndex = cursor.getColumnIndex(ContactsContract.Data.DATA1)
        while (!cursor.isAfterLast) {
            val id = cursor.getLong(idColumnIndex)
            var contact = contacts[id]
            if (contact ==
                null
            ) {
                contact = Contact(id)
                mapInVisibleGroup(cursor, contact, inVisibleGroupColumnIndex)
                mapDisplayName(cursor, contact, displayNamePrimaryColumnIndex)
                mapStarred(cursor, contact, starredColumnIndex)
                mapPhoto(cursor, contact, photoColumnIndex)
                mapThumbnail(cursor, contact, thumbnailColumnIndex)
                contacts[id] = contact
            }
            val mimetype = cursor.getString(mimetypeColumnIndex)
            when (mimetype) {
                ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE -> {
                    mapEmail(cursor, contact, dataColumnIndex)
                }
                ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE -> {
                    mapPhoneNumber(cursor, contact, dataColumnIndex)
                }
            }
            cursor.moveToNext()
        }
        cursor.close()
        for (key in contacts.keys) {
            emitter.onNext(contacts[key]!!)
        }
        emitter.onComplete()
    }

    private fun createCursor(): Cursor? {
        return mResolver.query(ContactsContract.Data.CONTENT_URI, PROJECTION, null, null, ContactsContract.Data.CONTACT_ID)
    }


}
