package com.plplim.david.geopending;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferenceUtil{
    private final String PREF_NAME = "android.plplim.geopending.preference";

    public final static String PREF_INTRO_USER_AGREEMENT = "PREF_USER_AGREEMENT";
    public final static String PREF_MAIN_VALUE = "PREF_MAIN_VALUE";


    static Context mContext;

    public SharedPreferenceUtil(Context c) {
        mContext = c;
    }

    public void put(String key, String value) {
        SharedPreferences pref = mContext.getApplicationContext().getSharedPreferences(PREF_NAME,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        editor.putString(key, value);
        editor.commit();
    }

    public void put(String key, boolean value) {
        SharedPreferences pref = mContext.getApplicationContext().getSharedPreferences(PREF_NAME,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        editor.putBoolean(key, value);
        editor.commit();
    }

    public void put(String key, int value) {
        SharedPreferences pref = mContext.getApplicationContext().getSharedPreferences(PREF_NAME,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();

        editor.putInt(key, value);
        editor.commit();
    }

    public String getValue(String key, String dftValue) {
        SharedPreferences pref = mContext.getApplicationContext().getSharedPreferences(PREF_NAME,
                Context.MODE_PRIVATE);

        try {
            return pref.getString(key, dftValue);
        } catch (Exception e) {
            return dftValue;
        }

    }

    public int getValue(String key, int dftValue) {
        SharedPreferences pref = mContext.getApplicationContext().getSharedPreferences(PREF_NAME,
                Context.MODE_PRIVATE);

        try {
            return pref.getInt(key, dftValue);
        } catch (Exception e) {
            return dftValue;
        }

    }

    public boolean getValue(String key, boolean dftValue) {
        SharedPreferences pref = mContext.getApplicationContext().getSharedPreferences(PREF_NAME,
                Context.MODE_PRIVATE);

        try {
            return pref.getBoolean(key, dftValue);
        } catch (Exception e) {
            return dftValue;
        }
    }

    public void delValue(String key) {
        SharedPreferences pref = mContext.getApplicationContext().getSharedPreferences(PREF_NAME,
                Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = pref.edit();

        editor.remove(key);
        editor.commit();

    }

    public void removeAllValue() {
        SharedPreferences pref = mContext.getApplicationContext().getSharedPreferences(PREF_NAME,
                Context.MODE_PRIVATE);

        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.commit();

    }

}