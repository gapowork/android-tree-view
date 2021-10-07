package com.gapo.treeview

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btn_single).setOnClickListener {
            startActivity(Intent(this, SingleChoiceActivity::class.java))
        }

        findViewById<Button>(R.id.btn_multi).setOnClickListener {
            startActivity(Intent(this, MultiChoiceActivity::class.java))
        }
    }
}