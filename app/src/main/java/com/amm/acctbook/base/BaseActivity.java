package com.amm.acctbook.base;

import android.app.Activity;
import android.os.Bundle;

import org.simple.eventbus.EventBus;

public abstract class BaseActivity extends Activity {
    private boolean isEventBusRegistered = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initBefore();
        setContentView(getLayoutResource());
        initAfter();
    }

    protected abstract void initAfter();

    protected abstract int getLayoutResource();

    protected void initBefore() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isEventBusRegistered) {
            EventBus.getDefault().unregister(this);
            isEventBusRegistered = false;
        }
    }

    protected void registerEventBus() {
        EventBus.getDefault().register(this);
        isEventBusRegistered = true;
    }

}
