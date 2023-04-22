package com.cs523.android.means_v2

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel


private const val TAG = "DataViewModel"

class DataViewModel: ViewModel() {

    init {
        Log.d(TAG, "ViewModel instance created")
    }

    // THE FUNCTION CALLED JUST BEFORE THE VIEW MODEL IS DESTROYED.
    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "ViewModel instance about to be destroyed")
    }

    val userID = MutableLiveData<String>().apply{
        value = null
    }

    val userName = MutableLiveData<String>().apply{
        value = null
    }
    val erContact = MutableLiveData<String>().apply{
        value = null
    }




}