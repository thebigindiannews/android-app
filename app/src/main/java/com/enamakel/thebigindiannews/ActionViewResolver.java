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

package com.enamakel.thebigindiannews;

import android.support.v4.view.MenuItemCompat;
import android.view.MenuItem;
import android.view.View;

/**
 * Injectable utility to resolve action view for menu items
 */
public class ActionViewResolver {
    /**
     * Returns the currently set action view for this menu item.
     *
     * @param menuItem the item to query
     * @return This item's action view
     */
    public View getActionView(MenuItem menuItem) {
        return MenuItemCompat.getActionView(menuItem);
    }
}
