package io.chingari

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_display_contacts.view.*


class ContactAdapter(var list: List<ContactModel> = listOf()) : RecyclerView.Adapter<ContactAdapter.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(
            R.layout.item_display_contacts,
            parent,
            false
        )
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(list[position])
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        @SuppressLint("SetTextI18n")
        fun bindItems(model: ContactModel) {
            Log.d("TAGS", " ${model.displayName}")
            Log.d("TAGS", " ${model.phoneNumbers}")
            Log.d("TAGS", " ${model.thumbnail}")
            itemView.run {
                if(model.thumbnail ==null) {
                    ivProfileUser.setImageResource(R.drawable.ic_launcher_background)
                }else{
                    ivProfileUser.setImageURI(model.thumbnail)
                }
                tvUserContactName.text= model.displayName
                tvNumber.text= model.phoneNumbers


                btnInvite.setOnClickListener {
                    context.sendSms(model.phoneNumbers,"Body of Message")
                  /*  val smsIntent = Intent(Intent.ACTION_VIEW)
                    val flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or
                            Intent.FLAG_ACTIVITY_CLEAR_TOP
                    smsIntent.flags = flags
                    smsIntent.type = "vnd.android-dir/mms-sms"
                    smsIntent.putExtra("address", model.phoneNumbers)
                    smsIntent.putExtra("sms_body", "Body of Message")
                    context.startActivity(smsIntent)*/
                }
            }

        }

        @SuppressLint("QueryPermissionsNeeded")
        fun Context.sendSms(phone: String?, sms: String?) {
            if (null != phone) {
                val i = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto: $phone"))
                i.putExtra("sms_body", sms)
                if (i.resolveActivity(packageManager) != null) {
                    startActivity(i)
                } else {
                    Toast.makeText(this, "SMS App not found", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}