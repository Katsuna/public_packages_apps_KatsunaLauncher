package com.katsuna.launcher.katsuna;

import android.content.Intent;
import android.view.View;

/**
 * Created by alkis on 24/10/2016.
 */

public class LaunchLogInfo {

    private View view;
    private Intent intent;
    private Object tag;

    public LaunchLogInfo(View view, Intent intent, Object tag) {
        setView(view);
        setIntent(intent);
        setTag(tag);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (view != null)
            sb.append("View=").append(view.toString()).append(" ");
        if (intent != null)
            sb.append("Intent=").append(intent.toString()).append(" ");
        if (tag != null)
            sb.append("Tag=").append(tag.toString());

        return sb.toString();
    }

    public View getView() {
        return view;
    }

    public void setView(View view) {
        this.view = view;
    }

    public Intent getIntent() {
        return intent;
    }

    public void setIntent(Intent intent) {
        this.intent = intent;
    }

    public Object getTag() {
        return tag;
    }

    public void setTag(Object tag) {
        this.tag = tag;
    }
}
