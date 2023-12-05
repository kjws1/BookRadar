package com.example.bookradar

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bookradar.databinding.BorrowedBookItemBinding
import com.example.bookradar.databinding.EditLayoutBinding
import com.example.bookradar.databinding.FragmentBorrowBinding
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

internal interface DBContract {
    companion object {
        const val TABLE_NAME = "MEMO_T"
        private const val COL_ID = "ID"
        private const val COL_TITLE = "TITLE"
        private const val COL_LIBRARY = "LIBRARY"
        private const val COL_BORROWDATE = "BORROW"
        private const val COL_DUEDATE = "DUE"
        private const val COL_IMAGE = "IMAGE"
        const val SQL_CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " +
                TABLE_NAME + "(" +
                COL_ID + " INTEGER NOT NULL PRIMARY KEY, " +
                COL_TITLE + " TEXT NOT NULL, " +
                COL_LIBRARY + " TEXT NOT NULL, " +
                COL_BORROWDATE + " TEXT NOT NULL, " +
                COL_DUEDATE + " TEXT NOT NULL," +
                COL_IMAGE + " TEXT NOT NULL" + ")"
        const val SQL_DROP_TABLE = "DROP TABLE IF EXISTS $TABLE_NAME"
        const val SQL_LOAD = "SELECT * FROM $TABLE_NAME"

        fun insertMemo(context: Context, id: Int, memo: Memo) {
            val db = DBHelper(context).writableDatabase
            val value = ContentValues().apply {
                put("id", id)
                put("title", memo.title)
                put("library", memo.library)
                put("borrow", memo.borrow)
                put("due", memo.due)
                put("image", memo.image)
            }
            db.insert(TABLE_NAME, null, value)
        }

        fun updateMemo(context: Context, id: Int, memo: Memo) {
            val db = DBHelper(context).writableDatabase
            val value = ContentValues().apply {
                put("title", memo.title)
                put("library", memo.library)
                put("borrow", memo.borrow)
                put("due", memo.due)
                put("image", memo.image)
            }
            db.update(
                TABLE_NAME, value,
                "id=$id", null
            )
        }
    }
}

data class Memo(
    val title: String,
    val library: String,
    val borrow: String,
    val due: String,
    var image: String = ""
)

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

class MemoViewHolder(private val adapter: MemoAdapter, val binding: BorrowedBookItemBinding) :
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
    private val fragment: BorrowFragment,
    val datas: MutableList<MutableMap<String, String>>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MemoViewHolder(
            this,
            BorrowedBookItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = datas[position]
        (holder as MemoViewHolder).itemPos = position
        holder.itemId = item["id"]!!.toInt()
        val binding = holder.binding
        val funcDuration = BorrowFragment()
        binding.run {
            textTitleValue.text = item["title"]
            textLibraryValue.text = item["library"]
            textBorrow.text = item["borrow"]
            textDue.text = item["due"]
            if (item["image"] != null && item["image"]!!.isNotBlank()) {
                Glide.with(fragment)
                    .load(item["image"])
                    .into(imageViewBookCover)
            }
            val borrowDate = item["borrow"]?.let {
                LocalDate.parse(
                    it,
                    DateTimeFormatter.ISO_LOCAL_DATE
                )
            }!!
            val dueDate = item["due"]?.let {
                LocalDate.parse(
                    it,
                    DateTimeFormatter.ISO_LOCAL_DATE
                )
            }!!
            val duration = funcDuration.calculateDuration(borrowDate, dueDate)

            textDurationValue.text = duration.toString()
        }
    }

    override fun getItemCount(): Int {
        return datas.size
    }

    fun delItem(itemPos: Int) {
        if (itemPos != -1) {
            fragment.delMemo(datas[itemPos]["id"]!!.toInt())
            datas.removeAt(itemPos)
            notifyItemRemoved(itemPos)
        }
    }

    fun editItem(itemPos: Int) {
        if (itemPos != -1) {
            val memo = Memo(
                datas[itemPos]["title"]!!,
                datas[itemPos]["library"]!!,
                datas[itemPos]["borrow"]!!,
                datas[itemPos]["due"]!!,
                datas[itemPos]["image"]!!
            )
            fragment.showManualEntry({ m ->
                fragment.editMemo(itemPos, m)
            }, memo)
        }
    }
}

@Suppress("DEPRECATION")
class BorrowFragment : Fragment() {
    lateinit var binding: FragmentBorrowBinding
    private var adapter: MemoAdapter? = null
    private var itemID: Int = 0
    private var dbHelper: DBHelper? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBorrowBinding.inflate(inflater, container, false)

        val bInfo = arguments?.getParcelable<BookInfo>("item")
        if (bInfo != null) {
            val memo = Memo(
                bInfo.book!!.title!!,
                bInfo.library!!.getName(requireContext()),
                LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE).toString(),
                LocalDate.now().plusDays(7).format(DateTimeFormatter.ISO_LOCAL_DATE).toString(),
                bInfo.book!!.thumbnail!!
            )
            showManualEntry({ m ->
                addMemo(m)
            }, memo)
        }


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
            item["image"] = cursor.getString(5)!!
            dataList.add(item)
            itemID = cursor.getInt(0)
        }
        cursor.close()

        adapter = MemoAdapter(this, dataList)
        binding.borrowingBookList.adapter = adapter

        binding.fab.setOnClickListener {
            showManualEntry({ memo ->
                addMemo(memo)
            })
        }

        return binding.root
    }

    /**
     * Calculate duration between two dates in days
     * @param fromTime start date
     * @param toTime end date
     * @return duration between two dates in days
     */
    fun calculateDuration(fromTime: LocalDate, toTime: LocalDate): Long {
        fromTime.until(toTime, java.time.temporal.ChronoUnit.DAYS)
        return fromTime.until(toTime, java.time.temporal.ChronoUnit.DAYS)
    }

    private fun addMemo(memo: Memo) {
        val item = mutableMapOf<String, String>()
        itemID++
        item["id"] = itemID.toString()
        item["title"] = memo.title
        item["library"] = memo.library
        item["borrow"] = memo.borrow
        item["due"] = memo.due
        item["image"] = memo.image
        adapter!!.datas.add(item)
        adapter!!.notifyItemChanged(itemID)

        DBContract.insertMemo(requireContext(), itemID, memo)
    }

    private fun calendarClickListener(
        eBinding: EditLayoutBinding,
        targetView: EditText
    ): View.OnClickListener {
        return View.OnClickListener {
            val today = LocalDate.now()
            DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    val setDate = Calendar.getInstance()
                    setDate.set(year, month, dayOfMonth)

                    // check if target view is borrow date or due date and if it's borrow date, check if it's after due date, and if it's due date, check if it's before borrow date
                    if (targetView == eBinding.editBorrowDate) {
                        val dueDate = LocalDate.parse(
                            eBinding.editTextDueDate.text,
                            DateTimeFormatter.ISO_LOCAL_DATE
                        )
                        if (dueDate.isBefore(
                                LocalDate.of(
                                    setDate.get(Calendar.YEAR),
                                    setDate.get(Calendar.MONTH) + 1,
                                    setDate.get(Calendar.DAY_OF_MONTH)
                                )
                            )
                        ) {
                            Toast.makeText(
                                activity,
                                getString(R.string.error_due_date_before_borrow_date),
                                Toast.LENGTH_SHORT
                            ).show()
                            return@DatePickerDialog
                        }
                    } else if (targetView == eBinding.editTextDueDate) {
                        val borrowDate = LocalDate.parse(
                            eBinding.editBorrowDate.text,
                            DateTimeFormatter.ISO_LOCAL_DATE
                        )
                        if (borrowDate.isAfter(
                                LocalDate.of(
                                    setDate.get(Calendar.YEAR),
                                    setDate.get(Calendar.MONTH) + 1,
                                    setDate.get(Calendar.DAY_OF_MONTH)
                                )
                            )
                        ) {
                            Toast.makeText(
                                activity,
                                getString(R.string.error_borrow_date_after_due_date),
                                Toast.LENGTH_SHORT
                            ).show()
                            return@DatePickerDialog
                        }
                    }

                    targetView.setText(
                        LocalDate.of(
                            setDate.get(Calendar.YEAR),
                            setDate.get(Calendar.MONTH) + 1,
                            setDate.get(Calendar.DAY_OF_MONTH)
                        ).format(DateTimeFormatter.ISO_LOCAL_DATE)
                    )

                    // calculate duration and update number picker
                    if (eBinding.editTextDueDate.text.isNotEmpty() && eBinding.editBorrowDate.text.isNotEmpty()) {
                        val borrowDate = LocalDate.parse(
                            eBinding.editBorrowDate.text,
                            DateTimeFormatter.ISO_LOCAL_DATE
                        )
                        val dueDate =
                            LocalDate.parse(
                                eBinding.editTextDueDate.text,
                                DateTimeFormatter.ISO_LOCAL_DATE
                            )
                        val duration = calculateDuration(borrowDate, dueDate)
                        eBinding.numberPickerDuration.value = duration.toInt()
                    }
                }, today.year, today.monthValue - 1, today.dayOfMonth
            ).show()

        }
    }

    /**
     * Show alert dialog to manually enter the borrowed book information
     * @param callback callback function to handle the result
     * @param memo default values to show
     */
    fun showManualEntry(callback: (Memo) -> Unit, memo: Memo? = null) {
        val eBinding = EditLayoutBinding.inflate(layoutInflater).apply {
            // configure duration number picker
            numberPickerDuration.run {
                // min and max value of duration
                minValue = 1
                maxValue = 100

                setOnValueChangedListener { _, _, newVal ->
                    val borrowDate = LocalDate.parse(
                        editBorrowDate.text,
                        DateTimeFormatter.ISO_LOCAL_DATE
                    )
                    val dueDate = borrowDate.plusDays(newVal.toLong())
                    editTextDueDate.setText(dueDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
                }
            }
            imageButtonCalendarBorrowDate.setOnClickListener(
                calendarClickListener(
                    this,
                    editBorrowDate
                )
            )
            imageButtonCalendarDueDate.setOnClickListener(
                calendarClickListener(
                    this,
                    editTextDueDate
                )
            )

            // use provided values if exists
            if (memo != null) {
                editTextBookTitle.setText(memo.title)
                editLibrary.setText(memo.library)
                editBorrowDate.setText(memo.borrow)
                editTextDueDate.setText(memo.due)
                numberPickerDuration.value = calculateDuration(
                    LocalDate.parse(memo.borrow, DateTimeFormatter.ISO_LOCAL_DATE),
                    LocalDate.parse(memo.due, DateTimeFormatter.ISO_LOCAL_DATE)
                ).toInt()
            } else {
                // set the default date of due date to today
                editBorrowDate.setText(
                    LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE).toString()
                )
                editTextDueDate.setText(
                    LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE).toString()
                )
            }
        }

        // show alert dialog
        AlertDialog.Builder(activity).run {
            setTitle(getString(R.string.manual_entry))
            setView(eBinding.root)
            setPositiveButton("Ok") { _, _ ->
                val title = eBinding.editTextBookTitle.text.toString()
                val library = eBinding.editLibrary.text.toString()
                val borrow = eBinding.editBorrowDate.text.toString()
                val due = eBinding.editTextDueDate.text.toString()
                callback(Memo(title, library, borrow, due, memo?.image ?: ""))
            }
            setNegativeButton("Cancel", null)
            show()
        }

    }

    fun delMemo(id: Int) {
        val db = dbHelper?.writableDatabase
        db?.delete(DBContract.TABLE_NAME, "id=$id", null)
    }

    fun editMemo(pos: Int, memo: Memo) {
        adapter!!.datas[pos]["title"] = memo.title
        adapter!!.datas[pos]["library"] = memo.library
        adapter!!.datas[pos]["borrow"] = memo.borrow
        adapter!!.datas[pos]["due"] = memo.due
        adapter!!.notifyItemChanged(pos)

        DBContract.updateMemo(requireContext(), pos, memo)
    }
}
