package com.example.lesson_043

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle

import android.widget.AdapterView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.lesson_043.databinding.ActivityMainBinding
import android.Manifest
import android.content.ContentProviderOperation
import android.content.pm.PackageManager
import android.provider.ContactsContract
import android.provider.ContactsContract.CommonDataKinds.Phone
import android.provider.ContactsContract.CommonDataKinds.StructuredName
import android.provider.ContactsContract.RawContacts
import android.util.Log

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var customAdapter: CustomAdapter? = null
    private var contactModelList: MutableList<ContactModel>? = null

    @SuppressLint("Range")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) !=
            PackageManager.PERMISSION_GRANTED)
        {
            permissionContact.launch(Manifest.permission.READ_CONTACTS)
            customAdapter?.notifyDataSetChanged()
        } else {
            getContacts()
        }
    }

    @SuppressLint("Range")
    private fun getContacts() {
        contactModelList = ArrayList()
        val phones = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,null,null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        )

        binding.headerTV.text=phones!!.moveToNext().toString()

        while (phones!!.moveToNext()) {
            val name =
                phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
            val phoneNumber =
                phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
            val contactModel = ContactModel(name, phoneNumber)
            contactModelList?.add(contactModel)
        }
        phones.close()

        customAdapter = CustomAdapter(this, contactModelList!!)
        binding.listViewLV.adapter = customAdapter
        binding.listViewLV.onItemClickListener = AdapterView.OnItemClickListener{parent, view, positon, id ->
            val person = (contactModelList as ArrayList<ContactModel>)[positon]
            val number = person.phone
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                permissionOfCall.launch(Manifest.permission.CALL_PHONE)
            } else {
                callTheNumber(number)
            }
        }
    }

    private fun callTheNumber(number: String?) {
        val intent = Intent(Intent.ACTION_CALL)
        intent.data = Uri.parse("tel:$number")
        startActivity(intent)
    }

    private val permissionContact = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ){
        isGranted ->
        if (isGranted) {
            Toast.makeText(this@MainActivity, "Получен доступ к контактам.", Toast.LENGTH_SHORT).show()
            getContacts()
        } else {
            Toast.makeText(this@MainActivity, "В разрешении отказано.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        binding.addBTN.setOnClickListener{
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_CONTACTS) !=
                PackageManager.PERMISSION_GRANTED)
            {
                permissionWriteContact.launch(Manifest.permission.WRITE_CONTACTS)
            } else {
                addContact()
                customAdapter!!.notifyDataSetChanged()
                getContacts()
            }
        }
    }

    private fun addContact() {
        val newContactName = binding.newContactNameET.text.toString()
        val newContactPhone = binding.newContactPhoneET.text.toString()
        val listCPO = ArrayList<ContentProviderOperation>()

        listCPO.add(
            ContentProviderOperation.newInsert(RawContacts.CONTENT_URI)
                .withValue(RawContacts.ACCOUNT_TYPE, null)
                .withValue(RawContacts.ACCOUNT_NAME, null)
                .build()
        )

        listCPO.add(
            ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE)
                .withValue(StructuredName.DISPLAY_NAME, newContactName)
                .build()
        )
        listCPO.add(
            ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE)
                .withValue(Phone.NUMBER, newContactPhone)
                .withValue(Phone.TYPE, Phone.TYPE_MOBILE)
                .build()
        )
        Toast.makeText(this@MainActivity, "$newContactName добавлен в список контактов", Toast.LENGTH_SHORT).show()
        try {
            contentResolver.applyBatch(ContactsContract.AUTHORITY, listCPO)
        } catch(e: Exception) {
            Log.e("Exception ", e.message!!)
        }
    }

    private val permissionOfCall = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ){
            isGranted ->
        if (isGranted) {
            Toast.makeText(this@MainActivity, "Получен доступ к звонкам.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this@MainActivity, "В разрешении отказано.", Toast.LENGTH_SHORT).show()
        }
    }

    private val permissionWriteContact = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(this@MainActivity, "Получен доступ к записи контактов", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this@MainActivity, "В разрешении отказано...", Toast.LENGTH_SHORT).show()
        }
    }
}



