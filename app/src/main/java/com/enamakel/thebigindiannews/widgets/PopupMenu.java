/*
 * Copyright (c) 2015 Ha Duy Trung
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.enamakel.thebigindiannews.widgets;


import android.content.Context;
import android.support.annotation.MenuRes;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;


public interface PopupMenu {
    /**
     * Create a new popup menu with an anchor view and alignment
     * gravity. Must be called right after construction.
     *
     * @param context Context the popup menu is running in, through which it
     *                can access the current theme, resources, etc.
     * @param anchor  Anchor view for this popup. The popup will appear below
     *                the anchor if there is room, or above it if there is not.
     * @param gravity The {@link Gravity} value for aligning the popup with its
     *                anchor.
     */
    void create(Context context, View anchor, int gravity);


    /**
     * Inflate a menu resource into this PopupMenu. This is equivalent to calling
     * popupMenu.getMenuInflater().inflate(menuRes, popupMenu.getMenu()).
     *
     * @param menuRes Menu resource to inflate
     */
    void inflate(@MenuRes int menuRes);


    /**
     * @return the {@link Menu} associated with this popup. Populate the returned Menu with
     * items before calling {@link #show()}.
     * @see #show()
     */
    Menu getMenu();


    /**
     * Set a listener that will be notified when the user selects an item from the menu.
     *
     * @param listener Listener to notify
     */
    void setOnMenuItemClickListener(OnMenuItemClickListener listener);


    /**
     * Show the menu popup anchored to the view specified during construction.
     */
    void show();


    /**
     * Interface responsible for receiving menu item click events if the items themselves
     * do not have individual item click listeners.
     */
    interface OnMenuItemClickListener {
        /**
         * This method will be invoked when a menu item is clicked if the item itself did
         * not already handle the event.
         *
         * @param item {@link MenuItem} that was clicked
         * @return <code>true</code> if the event was handled, <code>false</code> otherwise.
         */
        boolean onMenuItemClick(MenuItem item);
    }


    class Impl implements PopupMenu {
        android.support.v7.widget.PopupMenu supportPopupMenu;


        @Override
        public void create(Context context, View anchor, int gravity) {
            supportPopupMenu = new android.support.v7.widget.PopupMenu(context, anchor, gravity);
        }


        @Override
        public void inflate(@MenuRes int menuRes) {
            supportPopupMenu.inflate(menuRes);
        }


        @Override
        public Menu getMenu() {
            return supportPopupMenu.getMenu();
        }


        @Override
        public void setOnMenuItemClickListener(final OnMenuItemClickListener listener) {
            supportPopupMenu.setOnMenuItemClickListener(new android.support.v7.widget.PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    return listener.onMenuItemClick(item);
                }
            });
        }


        @Override
        public void show() {
            supportPopupMenu.show();
        }
    }
}
