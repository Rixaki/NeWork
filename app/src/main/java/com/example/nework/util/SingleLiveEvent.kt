package ru.netology.nmedia.util

import androidx.annotation.MainThread
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import java.util.concurrent.atomic.AtomicBoolean

class SingleLiveEvent<T> : MutableLiveData<T>() {
    //private var pending = false//*simple version without atomics
    private var pending = AtomicBoolean(false)

    @MainThread//in atomic version
    override fun observe(owner: LifecycleOwner, observer: Observer<in T?>) {
        require(!hasActiveObservers()) {
            error("Multiple observers registered but only one will be notified of changes.")
        }

        super.observe(owner) {
            //if (pending) {//*
            if (pending.compareAndSet(true, false)) {
                //pending = false//*
                observer.onChanged(it)
            }
        }
    }

    @MainThread
    override fun setValue(t: T?) {
        //pending = true//*
        pending.set(true)
        super.setValue(t)
    }
}