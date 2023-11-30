package com.example.bookradar

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.bookradar.databinding.BorrowingBookItemBinding
import com.example.bookradar.databinding.EditLayoutBinding
import com.example.bookradar.databinding.FragmentBorrowBinding
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

/**
 * A fragment representing a list of Items.
 */

internal interface DBContract {
    companion object {
        const val TABLE_NAME = "MEMO_T"
        const val COL_ID = "ID"
        const val COL_TITLE = "TITLE"
        const val COL_LIBRARY = "LIBRARY"
        const val COL_BORROWDATE = "BORROW"
        const val COL_DUEDATE = "DUE"
        const val SQL_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " +
                TABLE_NAME + "(" +
                COL_ID + " INTEGER NOT NULL PRIMARY KEY, " +
                COL_TITLE + " TEXT NOT NULL, " +
                COL_LIBRARY + " TEXT NOT NULL, " +
                COL_BORROWDATE + " TEXT NOT NULL, " +
                COL_DUEDATE + " TEXT NOT NULL)"
        const val SQL_DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME
        const val SQL_LOAD = "SELECT * FROM " + TABLE_NAME
    }
}

internal class DBHelper(context: Context?) :
    SQLiteOpenHelper(context, DB_FILE, null, DB_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(DBContract.SQL_CREATE_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(DBContract.SQL_DROP_TABLE)
        onCreate(db)
    }

    companion object {
        const val DB_FILE = "memo_t4.db"
        const val DB_VERSION = 1
    }
}

class MemoViewHolder(val adapter: MemoAdapter, val binding: BorrowingBookItemBinding) :
    RecyclerView.ViewHolder(binding.root) {
    var itemPos = -1
    var itemId = 0

    init {
        binding.buttonDel.setOnClickListener {
            if (itemPos != -1)
                adapter.delItem(itemPos)
        }

        itemView.setOnLongClickListener {
            adapter.editItem(itemPos)
            return@setOnLongClickListener true
        }
    }
}

class MemoAdapter(
    val fragment: BorrowFragment,
    val datas: MutableList<MutableMap<String, String>>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MemoViewHolder(
            this,
            BorrowingBookItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = datas[position]
        (holder as MemoViewHolder).itemPos = position
        holder.itemId = datas[position].get("id")!!.toInt()
        val binding = holder.binding
        val funcDuration = BorrowFragment()
        with(binding) {
            textTitle.text = "책제목: " + datas[position].get("title")
            textLibrary.text = "도서관: " + datas[position].get("library")
            textBorrow.text = datas[position].get("borrow")
            textDue.text = datas[position].get("due")
            val borrowDate = datas[position]["borrow"]?.let {
                LocalDate.parse(
                    it,
                    DateTimeFormatter.ISO_LOCAL_DATE
                )
            }!!
            val dueDate = datas[position]["due"]?.let {
                LocalDate.parse(
                    it,
                    DateTimeFormatter.ISO_LOCAL_DATE
                )
            }!!
            val duration = funcDuration.calculateDuration(borrowDate, dueDate)

            textDuration.text = "기간: ${duration}일"
        }
    }

    override fun getItemCount(): Int {
        return datas.size
    }

    fun delItem(itemPos: Int) {
        if (itemPos != -1) {
            fragment.delMemo(datas[itemPos]["id"]!!.toInt())
            datas.removeAt(itemPos)
            notifyDataSetChanged()
        }
    }

    fun editItem(itemPos: Int) {
        if (itemPos != -1) {
            fragment.editMemo(
                itemPos,
                datas[itemPos]["title"]!!,
                datas[itemPos]["library"]!!,
                datas[itemPos]["borrow"]!!,
                datas[itemPos]["due"]!!
            )
        }
    }
}

class BorrowFragment : Fragment() {
    lateinit var binding: FragmentBorrowBinding
    private var adapter: MemoAdapter? = null
    private var itemID: Int = 0
    private var dbHelper: DBHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentBorrowBinding.inflate(inflater, container, false)

        val dataList = mutableListOf<MutableMap<String, String>>()

        dbHelper = DBHelper(activity)
        val db = dbHelper?.readableDatabase
        val cursor = db?.rawQuery(DBContract.SQL_LOAD, null, null)
        while (cursor?.moveToNext()!!) {
            val item = mutableMapOf<String, String>()
            item["id"] = cursor.getInt(0).toString()
            item["title"] = cursor.getString(1)!!
            item["library"] = cursor.getString(2)!!
            item["borrow"] = cursor.getString(3)!!
            item["due"] = cursor.getString(4)!!
            dataList.add(item)
            itemID = cursor.getInt(0)
        }

        adapter = MemoAdapter(this, dataList)
        binding.borrowingBookList.adapter = adapter

        binding.fab.setOnClickListener {
            showManualEntry()
        }

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun calculateDuration(fromTime: LocalDate, toTime: LocalDate): Long {
        return fromTime.until(toTime, java.time.temporal.ChronoUnit.DAYS)
    }

    private fun addMemo(title: String, library: String, borrow: String, due: String) {
        val db = dbHelper?.writableDatabase

        val item = mutableMapOf<String, String>()
        itemID++
        item["id"] = itemID.toString()
        item["title"] = title
        item["library"] = library
        item["borrow"] = borrow
        item["due"] = due
        (binding.borrowingBookList.adapter as MemoAdapter).datas.add(item)

        val value = ContentValues()
        value.put("id", itemID)
        value.put("title", title)
        value.put("library", library)
        value.put("borrow", borrow)
        value.put("due", due)
        db?.insert(DBContract.TABLE_NAME, null, value)

        (binding.borrowingBookList.adapter as MemoAdapter).notifyDataSetChanged()

    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun calendarClickListener(
        eBinding: EditLayoutBinding,
        targetView: TextView
    ): View.OnClickListener {
        return View.OnClickListener {
            val today = LocalDate.now()
            var setDate: Calendar? = null
            DatePickerDialog(
                requireContext(),
                { view, year, month, dayOfMonth ->
                    setDate = Calendar.getInstance()
                    setDate?.set(year, month, dayOfMonth)

                    targetView.text = LocalDate.of(
                        setDate!!.get(Calendar.YEAR),
                        setDate!!.get(Calendar.MONTH) + 1,
                        setDate!!.get(Calendar.DAY_OF_MONTH)
                    ).format(DateTimeFormatter.ISO_LOCAL_DATE)

                    if (eBinding.editTextDueDate.text.isNotEmpty() && eBinding.editTextBorrowDate.text.isNotEmpty()) {
                        val borrowDate = LocalDate.parse(
                            eBinding.editTextBorrowDate.text,
                            DateTimeFormatter.ISO_LOCAL_DATE
                        )
                        val dueDate =
                            LocalDate.parse(
                                eBinding.editTextDueDate.text,
                                DateTimeFormatter.ISO_LOCAL_DATE
                            )
                        val duration = calculateDuration(borrowDate, dueDate)
                        eBinding.spinnerDuration.value = duration.toInt()
                    }
                }, today.year, today.monthValue, today.dayOfMonth
            ).show()

        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showManualEntry() {
        val eBinding = EditLayoutBinding.inflate(layoutInflater)
        val builder = AlertDialog.Builder(activity)
        // set the default date to borrow date today
        if (eBinding.editTextBorrowDate.text.isNullOrEmpty()) {
            eBinding.editTextBorrowDate.setText(
                LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE).toString()
            )
        }
        // set the default date of due date to today
        if (eBinding.editTextDueDate.text.isNullOrEmpty()) {
            eBinding.editTextDueDate.setText(
                LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE).toString()
            )
        }

        eBinding.spinnerDuration.apply {
            maxValue = 31
            minValue = 1

            setOnValueChangedListener { _, _, newVal ->
                val borrowDate = LocalDate.parse(
                    eBinding.editTextBorrowDate.text,
                    DateTimeFormatter.ISO_LOCAL_DATE
                )
                val dueDate = borrowDate.plusDays(newVal.toLong())
                eBinding.editTextDueDate.setText(dueDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
            }
        }
        eBinding.imageButtonCalendarBorrowDate.setOnClickListener(
            calendarClickListener(
                eBinding,
                eBinding.editTextBorrowDate
            )
        )
        eBinding.imageButtonCalendarDueDate.setOnClickListener(
            calendarClickListener(
                eBinding,
                eBinding.editTextDueDate
            )
        )

        builder.setTitle("메모 입력")
        builder.setView(eBinding.root)
        builder.setPositiveButton("Ok") { p0, p1 ->
            val title = eBinding.editTextBookTitle.text.toString()
            val library = eBinding.editTextLibrary.text.toString()
            val borrow = eBinding.editTextBorrowDate.text.toString()
            val due = eBinding.editTextDueDate.text.toString()
            addMemo(title, library, borrow, due)
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    fun delMemo(id: Int) {
        val db = dbHelper?.writableDatabase
        db?.delete(DBContract.TABLE_NAME, "id=$id", null)
    }

    fun editMemo(pos: Int, title: String, library: String, borrow: String, due: String) {
        val eBinding = EditLayoutBinding.inflate(layoutInflater)
        eBinding.editTextBookTitle.setText(title)
        eBinding.editTextLibrary.setText(library)
        eBinding.editTextBorrowDate.setText(borrow)
        eBinding.editTextDueDate.setText(due)
        val builder = AlertDialog.Builder(activity)
        builder.setTitle("메모 입력")
        builder.setView(eBinding.root)
        builder.setPositiveButton("Ok") { p0, p1 ->
            val db = dbHelper?.writableDatabase
            val title = eBinding.editTextBookTitle.text.toString()
            val library = eBinding.editTextLibrary.text.toString()
            val borrow = eBinding.editTextBorrowDate.text.toString()
            val due = eBinding.editTextDueDate.text.toString()

            adapter!!.datas[pos]["title"] = title
            adapter!!.datas[pos]["library"] = library
            adapter!!.datas[pos]["borrow"] = borrow
            adapter!!.datas[pos]["due"] = due

            val value = ContentValues()
            value.put("title", title)
            value.put("library", library)
            value.put("borrow", borrow)
            value.put("due", due)
            db?.update(
                DBContract.TABLE_NAME, value,
                "id=${(adapter as MemoAdapter).datas[pos].get("id")}", null
            )

            (binding.borrowingBookList.adapter as MemoAdapter).notifyDataSetChanged()
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

}
