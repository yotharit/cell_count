package com.waevploy.cellcount

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.waevploy.cellcount.cell.CellFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportFragmentManager.beginTransaction()
            .replace(R.id.content_frame, CellFragment.createInstance())
            .commit()
    }
}
