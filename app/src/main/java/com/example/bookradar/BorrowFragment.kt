package com.example.bookradar

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.bookradar.databinding.EditLayoutBinding
import com.example.bookradar.databinding.FragmentBorrowBinding
import com.example.bookradar.databinding.MemoBinding
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

class MemoViewHolder(val adapter: MemoAdapter, val binding: MemoBinding) : RecyclerView.ViewHolder(binding.root) {
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

class MemoAdapter(val fragment:BorrowFragment, val datas:MutableList<MutableMap<String,String>>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var backColor:Int = 1 //Color.rgb(0xFE,0xFB, 0xE5)
    var divide:Boolean = true
    var line:Boolean = true

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MemoViewHolder(this, MemoBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as MemoViewHolder).itemPos = position
        holder.itemId = datas[position].get("id")!!.toInt()
        val binding = holder.binding
        with(binding) {
            /*if(line)
                memo.setBackgroundResource(R.drawable.shape_line)
            else
                memo.setBackgroundResource(R.drawable.shape_noline)
            if(divide)
                //divider.visibility = View.VISIBLE
            else
                //divider.visibility = View.INVISIBLE
            textMemo.setBackgroundColor(when(backColor) {
                3 -> Color.rgb(0xE2, 0xF4, 0xFC)
                2 -> Color.rgb(0xEB, 0xE3, 0xFB)
                else -> Color.rgb(0xFE, 0xFB, 0xE5)
            })*/
            textTitle.text = datas[position].get("title")
            textLibrary.text = datas[position].get("library")
            textBorrow.text = datas[position].get("borrow")
            textDue.text = datas[position].get("due")
        }
    }

    override fun getItemCount(): Int {
        return datas.size
    }

    fun delItem(itemPos:Int) {
        if(itemPos != -1) {
            fragment.delItem(datas[itemPos].get("id")?.toInt()!!)
            datas.removeAt(itemPos)
            notifyDataSetChanged()
        }
    }

    fun editItem(itemPos:Int) {
        if(itemPos != -1) {
            fragment.editMemo(itemPos, datas[itemPos].get("title")!!, datas[itemPos].get("library")!!, datas[itemPos].get("borrow")!!, datas[itemPos].get("due")!!)
        }
    }
}

class BorrowFragment : Fragment() {
    lateinit var binding : FragmentBorrowBinding
    var adapter:MemoAdapter? = null
    var itemID:Int = 0
    private var dbHelper:DBHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //val view = inflater.inflate(R.layout.fragment_memo_list, container, false)
        binding = FragmentBorrowBinding.inflate(inflater, container, false)

        val dataList = mutableListOf<MutableMap<String,String>>()
        /*var item = mutableMapOf<String,String>()

         item.put("id", "0")
         item .put("memo", "다음주 중간고사")
         dataList.add(item)

         item = mutableMapOf<String,String>()
         item.put("id", "1")
         item.put("memo", "오늘 엑스포")
         dataList.add(item)*/

        dbHelper = DBHelper(activity)
        val db = dbHelper?.readableDatabase
        val cursor = db?.rawQuery(DBContract.SQL_LOAD, null, null)
        while(cursor?.moveToNext()!!) {
            var item = mutableMapOf<String,String>()
            item.put("id", cursor?.getInt(0)?.toString()!!)
            item.put("title", cursor?.getString(1)!!)
            item.put("library", cursor?.getString(2)!!)
            item.put("borrow", cursor?.getString(3)!!)
            item.put("due", cursor?.getString(4)!!)
            dataList.add(item)
            itemID = cursor?.getInt(0)!!
        }

        adapter = MemoAdapter(this@BorrowFragment, dataList)
        binding.list.adapter = adapter

        val sPreference = PreferenceManager.getDefaultSharedPreferences(activity?.applicationContext!!)
        (adapter as MemoAdapter).backColor = sPreference.getString("backcolor", "1")!!.toInt()
        (adapter as MemoAdapter).divide = sPreference.getBoolean("divider", true)
        (adapter as MemoAdapter).line = sPreference.getBoolean("line", true)

        binding.fab.setOnClickListener{
            showManualEntry()
        }

        return binding.root
    }


    fun addMemo(title:String, library:String, borrow:String, due: String ) {
        val db = dbHelper?.writableDatabase

        val item = mutableMapOf<String,String>()
        itemID++
        item.put("id", itemID.toString())
        item.put("title", title)
        item.put("library", library)
        item.put("borrow", borrow)
        item.put("due", due)
        (binding.list.adapter as MemoAdapter).datas.add(item)

        val value = ContentValues()
        value.put("id", itemID)
        value.put("title", title)
        value.put("library", library)
        value.put("borrow", borrow)
        value.put("due", due)
        db?.insert(DBContract.TABLE_NAME, null, value)

        (binding.list.adapter as MemoAdapter).notifyDataSetChanged()

    }
    fun showManualEntry() {
        val dBinding = EditLayoutBinding.inflate(layoutInflater)
        val builder = AlertDialog.Builder(activity)
        dBinding.buttonCalendar.setOnClickListener {
            var calendar = Calendar.getInstance()
            var year = calendar.get(Calendar.YEAR)
            var month = calendar.get(Calendar.MONTH)
            var day = calendar.get(Calendar.DAY_OF_MONTH)
            context?.let { it1 ->
                DatePickerDialog(it1, { _, year, month, day ->
                    run {
                        dBinding.editText.setText(year.toString() + "/" + (month + 1).toString() + "/" + day.toString())
                    }
                }, year, month, day)
            }?.show()
        }
        dBinding.buttonCalendar2.setOnClickListener {
            var calendar = Calendar.getInstance()
            var year = calendar.get(Calendar.YEAR)
            var month = calendar.get(Calendar.MONTH)
            var day = calendar.get(Calendar.DAY_OF_MONTH)
            context?.let { it1 ->
                DatePickerDialog(it1, { _, year, month, day ->
                    run {
                        dBinding.editText2.setText(year.toString() + "/" + (month + 1).toString() + "/" + day.toString())
                    }
                }, year, month, day)
            }?.show()
        }
        builder.setTitle("메모 입력")
        builder.setView(dBinding.root)
        builder.setPositiveButton("Ok", object:DialogInterface.OnClickListener {
            override fun onClick(p0: DialogInterface?, p1: Int) {
                val title = dBinding.editTextTitle.text.toString()
                val library = dBinding.editTextLib.text.toString()
                val borrow = dBinding.editText.text.toString()
                val due = dBinding.editText2.text.toString()
                addMemo(title, library, borrow, due)
            }
        })
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    fun delItem(id:Int) {
        val db = dbHelper?.writableDatabase
        db?.delete(DBContract.TABLE_NAME, "id=$id", null)
    }

    fun editMemo(pos:Int, title:String, library:String, borrow:String, due:String) {
        val dBinding = EditLayoutBinding.inflate(layoutInflater)
        dBinding.editTextTitle.setText(title)
        dBinding.editTextLib.setText(library)
        dBinding.editText.setText(borrow)
        dBinding.editText2.setText(due)
        dBinding.buttonCalendar.setOnClickListener {
            var calendar = Calendar.getInstance()
            var year = calendar.get(Calendar.YEAR)
            var month = calendar.get(Calendar.MONTH)
            var day = calendar.get(Calendar.DAY_OF_MONTH)
            context?.let { it1 ->
                DatePickerDialog(it1, { _, year, month, day ->
                    run {
                        dBinding.editText.setText(year.toString() + "/" + (month + 1).toString() + "/" + day.toString())
                    }
                }, year, month, day)
            }?.show()
        }
        dBinding.buttonCalendar2.setOnClickListener {
            var calendar = Calendar.getInstance()
            var year = calendar.get(Calendar.YEAR)
            var month = calendar.get(Calendar.MONTH)
            var day = calendar.get(Calendar.DAY_OF_MONTH)
            context?.let { it1 ->
                DatePickerDialog(it1, { _, year, month, day ->
                    run {
                        dBinding.editText2.setText(year.toString() + "/" + (month + 1).toString() + "/" + day.toString())
                    }
                }, year, month, day)
            }?.show()
        }
        val builder = AlertDialog.Builder(activity)
        builder.setTitle("메모 입력")
        builder.setView(dBinding.root)
        builder.setPositiveButton("Ok", object:DialogInterface.OnClickListener {
            override fun onClick(p0: DialogInterface?, p1: Int) {
                val db = dbHelper?.writableDatabase
                val title = dBinding.editTextTitle.text.toString()
                val library = dBinding.editTextLib.text.toString()
                val borrow = dBinding.editText.text.toString()
                val due = dBinding.editText2.text.toString()

                (adapter as MemoAdapter).datas[pos].put("title", title)
                (adapter as MemoAdapter).datas[pos].put("library", library)
                (adapter as MemoAdapter).datas[pos].put("borrow", borrow)
                (adapter as MemoAdapter).datas[pos].put("due", due)


                val value = ContentValues()
                value.put("title", title)
                value.put("library", library)
                value.put("borrow", borrow)
                value.put("due", due)
                db?.update(DBContract.TABLE_NAME, value,
                    "id=${(adapter as MemoAdapter).datas[pos].get("id")}", null)

                (binding.list.adapter as MemoAdapter).notifyDataSetChanged()
            }
        })
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }
}

