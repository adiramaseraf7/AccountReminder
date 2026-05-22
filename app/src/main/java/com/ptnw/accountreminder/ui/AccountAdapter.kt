package com.ptnw.accountreminder.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ptnw.accountreminder.R
import com.ptnw.accountreminder.data.Account
import java.text.SimpleDateFormat
import java.util.*

class AccountAdapter(
    private val onEdit: (Account) -> Unit,
    private val onDelete: (Account) -> Unit
) : ListAdapter<Account, AccountAdapter.ViewHolder>(DiffCallback) {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView        = view.findViewById(R.id.tvName)
        val tvDesc: TextView        = view.findViewById(R.id.tvDesc)
        val tvExpired: TextView     = view.findViewById(R.id.tvExpired)
        val tvDaysLeft: TextView    = view.findViewById(R.id.tvDaysLeft)
        val btnEdit: ImageButton    = view.findViewById(R.id.btnEdit)
        val btnDelete: ImageButton  = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_account, parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val account = getItem(position)
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale("id"))
        val now = System.currentTimeMillis()
        val daysLeft = ((account.expiredDate - now) / (1000 * 60 * 60 * 24)).toInt()

        holder.tvName.text = account.name
        holder.tvDesc.text = account.description
        holder.tvExpired.text = "Expired: ${sdf.format(Date(account.expiredDate))}"

        when {
            daysLeft < 0  -> { holder.tvDaysLeft.text = "EXPIRED"; holder.tvDaysLeft.setTextColor(Color.parseColor("#f38ba8")) }
            daysLeft == 0 -> { holder.tvDaysLeft.text = "Hari ini!"; holder.tvDaysLeft.setTextColor(Color.parseColor("#f38ba8")) }
            daysLeft <= account.reminderDays -> { holder.tvDaysLeft.text = "$daysLeft hari lagi"; holder.tvDaysLeft.setTextColor(Color.parseColor("#fab387")) }
            else          -> { holder.tvDaysLeft.text = "$daysLeft hari lagi"; holder.tvDaysLeft.setTextColor(Color.parseColor("#a6e3a1")) }
        }

        holder.btnEdit.setOnClickListener { onEdit(account) }
        holder.btnDelete.setOnClickListener { onDelete(account) }
    }

    object DiffCallback : DiffUtil.ItemCallback<Account>() {
        override fun areItemsTheSame(a: Account, b: Account) = a.id == b.id
        override fun areContentsTheSame(a: Account, b: Account) = a == b
    }
}
