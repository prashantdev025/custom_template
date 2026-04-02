package com.editor.template

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = GridLayoutManager(this,2)

        // Build list from frame 3 to frame 25 (23 items)
        val templateItems = (3..26).map { frameNumber ->
            val resId = resources.getIdentifier(
                "custom_temp_lv_$frameNumber", "layout", packageName
            )
            TemplateAdapter.TemplateItem(frameNumber, resId)
        }

        val adapter = TemplateAdapter(templateItems) { item ->
            if (item.layoutResId != 0) {
                val intent = Intent(this, TemplateActivity::class.java)
                intent.putExtra(TemplateActivity.EXTRA_LAYOUT_RES_ID, item.layoutResId)
                startActivity(intent)
            }
        }

        recyclerView.adapter = adapter
    }
}