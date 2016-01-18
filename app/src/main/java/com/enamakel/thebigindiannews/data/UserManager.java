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

package com.enamakel.thebigindiannews.data;

import android.content.Context;
import android.os.Parcelable;
import android.support.annotation.NonNull;

public interface UserManager {
    void getUser(String username, final ResponseListener<User> listener);

    interface User extends Parcelable {
        String getId();
        String getAbout();
        long getKarma();
        String getCreated(Context context);
        @NonNull ItemManager.Item[] getItems();
    }
}