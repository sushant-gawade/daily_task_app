package com.example.dailytask

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private var taskLayout: LinearLayout? = null
    private var sharedPreferences: SharedPreferences? = null
    private val TASKS_KEY = "tasks"
    private val DATE_KEY = "date"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        taskLayout = findViewById(R.id.taskLayout)
        sharedPreferences = getSharedPreferences("DailyTasks", MODE_PRIVATE)

        val addTaskButton = findViewById<Button>(R.id.addTaskButton)
        addTaskButton.setOnClickListener { addTask() }

        // ✅ About Developer button click
        val aboutButton = findViewById<Button>(R.id.aboutButton)
        aboutButton.setOnClickListener {
            val intent = Intent(this, AboutActivity::class.java)
            startActivity(intent)
        }

        refreshTasks()
    }

    private fun addTask() {
        val taskInput = findViewById<EditText>(R.id.taskInput)
        val taskText = taskInput.text.toString()
        if (!taskText.isEmpty()) {
            addTaskToLayout(taskText, false)
            taskInput.setText("")
            saveTask(taskText, false)
        }
    }

    private fun addTaskToLayout(taskText: String, isChecked: Boolean) {
        val taskItem = layoutInflater.inflate(R.layout.task_item, null) as LinearLayout
        val taskCheckBox = taskItem.findViewById<CheckBox>(R.id.taskCheckBox)
        val editButton = taskItem.findViewById<Button>(R.id.editTaskButton)
        val deleteButton = taskItem.findViewById<Button>(R.id.deleteTaskButton)
        taskCheckBox.text = taskText
        taskCheckBox.isChecked = isChecked

        taskCheckBox.setOnCheckedChangeListener { _, isChecked1 ->
            updateTaskCheckedStatus(taskText, isChecked1)
        }

        editButton.setOnClickListener { renameTask(taskText) }

        deleteButton.setOnClickListener {
            taskLayout!!.removeView(taskItem)
            removeTask(taskText)
        }

        taskLayout!!.addView(taskItem)
    }

    private fun saveTask(taskText: String, isChecked: Boolean) {
        try {
            val taskArray = JSONArray(sharedPreferences!!.getString(TASKS_KEY, "[]"))
            val taskObject = JSONObject()
            taskObject.put("task", taskText)
            taskObject.put("checked", isChecked)
            taskArray.put(taskObject)

            val editor = sharedPreferences!!.edit()
            editor.putString(TASKS_KEY, taskArray.toString())
            editor.apply()
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun updateTaskCheckedStatus(taskText: String, isChecked: Boolean) {
        try {
            val taskArray = JSONArray(sharedPreferences!!.getString(TASKS_KEY, "[]"))
            for (i in 0 until taskArray.length()) {
                val taskObject = taskArray.getJSONObject(i)
                if (taskObject.getString("task") == taskText) {
                    taskObject.put("checked", isChecked)
                    break
                }
            }

            val editor = sharedPreferences!!.edit()
            editor.putString(TASKS_KEY, taskArray.toString())
            editor.apply()
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun removeTask(taskText: String) {
        try {
            val taskArray = JSONArray(sharedPreferences!!.getString(TASKS_KEY, "[]"))
            val newTaskArray = JSONArray()

            for (i in 0 until taskArray.length()) {
                val taskObject = taskArray.getJSONObject(i)
                if (taskObject.getString("task") != taskText) {
                    newTaskArray.put(taskObject)
                }
            }

            val editor = sharedPreferences!!.edit()
            editor.putString(TASKS_KEY, newTaskArray.toString())
            editor.apply()
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun renameTask(oldTaskText: String) {
        val editTaskInput = EditText(this)
        editTaskInput.setText(oldTaskText)

        AlertDialog.Builder(this)
            .setTitle("Rename Task")
            .setView(editTaskInput)
            .setPositiveButton("OK") { _, _ ->
                val newTaskText = editTaskInput.text.toString()
                if (!newTaskText.isEmpty()) {
                    updateTask(oldTaskText, newTaskText)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateTask(oldTaskText: String, newTaskText: String) {
        try {
            val taskArray = JSONArray(sharedPreferences!!.getString(TASKS_KEY, "[]"))
            for (i in 0 until taskArray.length()) {
                val taskObject = taskArray.getJSONObject(i)
                if (taskObject.getString("task") == oldTaskText) {
                    taskObject.put("task", newTaskText)
                    break
                }
            }

            val editor = sharedPreferences!!.edit()
            editor.putString(TASKS_KEY, taskArray.toString())
            editor.apply()

            taskLayout!!.removeAllViews()
            loadTasks()
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    private fun refreshTasks() {
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val lastSavedDate = sharedPreferences!!.getString(DATE_KEY, "")!!

        if (currentDate != lastSavedDate) {
            try {
                val taskArray = JSONArray(sharedPreferences!!.getString(TASKS_KEY, "[]"))
                val updatedTaskArray = JSONArray()

                for (i in 0 until taskArray.length()) {
                    val taskObject = taskArray.getJSONObject(i)
                    taskObject.put("checked", false)
                    updatedTaskArray.put(taskObject)
                }

                val editor = sharedPreferences!!.edit()
                editor.putString(TASKS_KEY, updatedTaskArray.toString())
                editor.putString(DATE_KEY, currentDate)
                editor.apply()
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }

        loadTasks()
    }

    private fun loadTasks() {
        try {
            val taskArray = JSONArray(sharedPreferences!!.getString(TASKS_KEY, "[]"))
            for (i in 0 until taskArray.length()) {
                val taskObject = taskArray.getJSONObject(i)
                val taskText = taskObject.getString("task")
                val isChecked = taskObject.getBoolean("checked")
                addTaskToLayout(taskText, isChecked)
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }
}
