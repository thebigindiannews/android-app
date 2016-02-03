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

package com.enamakel.thebigindiannews.util;


import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;


public class FontCache {
    static final ArrayMap<String, Typeface> typefaceMap;


    static {
        typefaceMap = new ArrayMap<>();
    }


    /**
     * Returns the typeface instance of the given font. The function caches each font as it is being
     * called, to avoid loading it multiple times.
     *
     * @param context      The context of the application
     * @param typefaceName The font name to load.
     * @return A {@link Typeface} instance of the font. null if the font isin't in the assets
     * folder.
     */
    public static Typeface get(Context context, String typefaceName) {
        if (TextUtils.isEmpty(typefaceName)) return null;

        // sometimes the typeface name contains the '.ttf' part twice. This fixes that.
        String fontName = typefaceName.replaceAll(".ttf", "") + ".ttf";

        // Check if the typeface is in our cache.
        if (!typefaceMap.containsKey(fontName)) {
            Typeface typeface = Typeface.createFromAsset(context.getAssets(), fontName);
            typefaceMap.put(fontName, typeface);
        }

        return typefaceMap.get(fontName);
    }


    /**
     * Returns the 'bold' version of the given font.
     *
     * @param context      The context of the application
     * @param typefaceName The font name to load.
     * @return A {@link Typeface} instance of the font. null if the font isin't in the assets
     * folder.
     */
    public static Typeface getBold(Context context, String typefaceName) {
        if (TextUtils.isEmpty(typefaceName)) return null;

        // Remove the -bold component if it exists.
        typefaceName = typefaceName.replace("-bold", "");

        // Append '-bold' to the font name.
        typefaceName = typefaceName.replace(".ttf", "") + "-bold.ttf";

        return get(context, typefaceName);
    }
}
