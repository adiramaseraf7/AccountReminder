package com.ptnw.accountreminder

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.ptnw.accountreminder.data.Account
import com.ptnw.accountreminder.data.AccountDatabase
import com.ptnw.accountreminder.ui.AccountAdapter
import com.ptnw.accountreminder.worker.ReminderWorker
import kotlinx.coroutines.launch
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var adapter: AccountAdapter
    private val dao by lazy { AccountDatabase.getDatabase(this).accountDao() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        adapter = AccountAdapter(
            onEdit   = { showDialog(it) },
            onDelete = { confirmDelete(it) }
        )

        findViewById<RecyclerView>(R.id.rvAccounts).apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
        }

        dao.getAll().observe(this) { adapter.submitList(it) }

        findViewById<FloatingActionButton>(R.id.fabAdd).setOnClickListener {
            showDialog(null)
        }

        ReminderWorker.schedule(this)
    }

    private fun showDialog(existing: Account?) {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_add_account, null)
        val etName    = view.findViewById<EditText>(R.id.etName)
        val etDesc    = view.findViewById<EditText>(R.id.etDesc)
        val etDays    = view.findViewById<EditText>(R.id.etReminderDays)
        val tvDate    = view.findViewById<TextView>(R.id.tvSelectedDate)
        val btnDate   = view.findViewById<Button>(R.id.btnPickDate)

        var selectedCal = Calendar.getInstance()

        existing?.let {
            etName.setText(it.name)
            etDesc.setText(it.description)
            etDays.setText(it.reminderDays.toString())
            selectedCal.timeInMillis = it.expiredDate
            tvDate.text = android.text.format.DateFormat.getDateFormat(this).format(it.expiredDate)
        }

        btnDate.setOnClickListener {
            DatePickerDialog(this,
                { _, y, m, d ->
                    selectedCal.set(y, m, d, 23, 59, 59)
                    tvDate.text = "$d/${m+1}/$y"
                },
                selectedCal.get(Calendar.YEAR),
                selectedCal.get(Calendar.MONTH),
                selectedCal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        AlertDialog.Builder(this)
            .setTitle(if (existing == null) "Tambah Akun" else "Edit Akun")
            .setView(view)
            .setPositiveButton("Simpan") { _, _ ->
                val name = etName.text.toString().trim()
                val desc = etDesc.text.toString().trim()
                val days = etDays.text.toString().toIntOrNull() ?: 14
                if (name.isEmpty()) { Toast.makeText(this, "Nama tidak boleh kosong", Toast.LENGTH_SHORT).show(); return@setPositiveButton }
                val account = Account(
                    id = existing?.id ?: 0,
                    name = name,
                    description = desc,
                    expiredDate = selectedCal.timeInMillis,
                    reminderDays = days
                )
                lifecycleScope.launch {
                    if (existing == null) dao.insert(account) else dao.update(account)
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun confirmDelete(account: Account) {
        AlertDialog.Builder(this)
            .setTitle("Hapus Akun")
            .setMessage("Hapus \"${account.name}\"?")
            .setPositiveButton("Hapus") { _, _ ->
                lifecycleScope.launch { dao.delete(account) }
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}
