package com.plplim.david.geopending;

import android.support.annotation.NonNull;

/**
 * Created by OHRok on 2018-04-19.
 */

public class UserId {

    public String userId;

    public <T extends UserId> T withId(@NonNull final String id) {
        this.userId = id;
        return (T) this;
    }
}
