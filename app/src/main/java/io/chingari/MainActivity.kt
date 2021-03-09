package io.chingari

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.chingari.lib.Contact
import io.chingari.lib.RxContacts
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private val compositeDisposable = CompositeDisposable()
    private val mContactAdapter by lazy { ContactAdapter() }
    var list: ArrayList<ContactModel> = arrayListOf()

    private val CONTACT_REQUEST_CODE = 101


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }

    override fun onStart() {
        super.onStart()
        setupPermissions()
    }


    private fun setupPermissions() {
        val permission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.i("TAG", "Permission to record denied")
            makeRequest()
        } else{
            requestContacts()
        }
    }

    private fun makeRequest() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_CONTACTS),
            CONTACT_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            CONTACT_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Log.i("TAG", "Permission has been denied by user")
                } else {
                    requestContacts()
                    Log.i("TAG", "Permission has been granted by user")
                }
            }
        }
    }
    
    private fun requestContacts() {
        compositeDisposable.add(
            RxContacts.fetch(this)
                .filter { m -> m.inVisibleGroup == 1 }
                .toSortedList { obj, other -> obj.compareTo(other) }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ contacts ->
                    getContactsList(contacts)
                }, { it ->
                    //Handle exception
                })
        )
    }

    private fun getContactsList(contacts: MutableList<Contact>) {
        contacts.forEach {
            // Log.d("TAGS", " ${it.displayName}")
            // Log.d("TAGS", " ${it.phoneNumbers}")
            val photoUri = it.thumbnail
            Log.d("TAGS", " $photoUri")
            for (i in it.phoneNumbers) {
                if (it.thumbnail == null)
                    list.add(ContactModel(it.displayName!!, i, null))
                else list.add(ContactModel(it.displayName!!, i, it.thumbnail))
            }
        }
        mContactAdapter.list = list
        rv_contacts.adapter = mContactAdapter
        mContactAdapter.notifyDataSetChanged()
    }


    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.dispose()
    }
}