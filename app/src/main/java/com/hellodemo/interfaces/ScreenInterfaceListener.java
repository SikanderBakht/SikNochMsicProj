package com.hellodemo.interfaces;

import com.hellodemo.models.NavGroupItem;

/**
 * Created by new user on 2/17/2018.
 */

public interface ScreenInterfaceListener {
    void selectedScreen(int position, String title);
    void selectedGroup(NavGroupItem group);
    void setActivityNameNull();
}
