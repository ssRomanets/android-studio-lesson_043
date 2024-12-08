package com.example.lesson_043

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

class CustomAdapter(
    private val context: Context,
    private val contactModelList: MutableList<ContactModel>
): BaseAdapter() {

    override fun getCount(): Int {
        return contactModelList.size
    }

    override fun getItem(position: Int): Any {
        return contactModelList[position]
    }

    override fun getItemId(position: Int): Long {
        return  0
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val holder: ViewHolder

        if (convertView == null) {
            holder = ViewHolder()

            val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = inflater.inflate(R.layout.list_item, null, true)
            holder.nameTV = convertView!!.findViewById(R.id.nameTV) as TextView
            holder.phoneTV = convertView!!.findViewById(R.id.phoneTV) as TextView
            convertView.tag = holder
        } else {
            holder = convertView.tag as ViewHolder
        }
        holder.nameTV!!.text = contactModelList[position].name
        holder.phoneTV!!.text = contactModelList[position].phone
        return  convertView
    }

    private inner class ViewHolder {
        var nameTV: TextView? = null
        var phoneTV: TextView? = null
    }
}