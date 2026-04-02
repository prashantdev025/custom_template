package com.editor.template

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.asynclayoutinflater.view.AsyncLayoutInflater

class TemplateActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_template)

        val layoutResId = intent.getIntExtra(EXTRA_LAYOUT_RES_ID, -1)
        val container = findViewById<FrameLayout>(R.id.templateContainer1)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        if (layoutResId != -1) {
            progressBar.visibility = View.VISIBLE

            AsyncLayoutInflater(this).inflate(layoutResId, container) { view, _, parent ->
                progressBar.visibility = View.GONE
                parent?.addView(view)
            }
        }
    }

    companion object {
        const val EXTRA_LAYOUT_RES_ID = "extra_layout_res_id"
    }
}