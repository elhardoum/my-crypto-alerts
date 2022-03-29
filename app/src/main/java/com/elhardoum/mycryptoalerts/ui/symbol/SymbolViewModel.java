package com.elhardoum.mycryptoalerts.ui.symbol;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SymbolViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public SymbolViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is symbol fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}