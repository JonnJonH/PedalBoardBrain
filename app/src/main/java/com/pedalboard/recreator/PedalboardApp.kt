package com.pedalboard.recreator

import android.app.Application
import com.pedalboard.recreator.data.AppDatabase

class PedalboardApp : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
}
