/*
 * This file is part of the Illarion easyNPC Editor.
 *
 * Copyright © 2012 - Illarion e.V.
 *
 * The Illarion easyNPC Editor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The Illarion easyNPC Editor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the Illarion easyNPC Editor.  If not, see <http://www.gnu.org/licenses/>.
 */
package illarion.easynpc.data;

import illarion.common.util.TableLoader;
import illarion.common.util.TableLoaderSink;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * This class loads and stores all items defined in the client database along
 * with all the values required for the script parser to work properly.
 *
 * @author Martin Karing &lt;nitram@illarion.org&gt;
 */
@SuppressWarnings("nls")
public final class Items implements Comparable<Items> {
    /**
     * The table index of the item id in the definition table.
     */
    protected static final int TB_ID = 0;

    /**
     * The list of items that were load.
     */
    @Nonnull
    private static final Items[] itemsList;

    static {
        final ArrayList<Items> itemList = new ArrayList<Items>();

        new TableLoader("Items", new TableLoaderSink() {
            @Override
            public boolean processRecord(final int line,
                                         @Nonnull final TableLoader loader) {
                final int itemId = loader.getInt(TB_ID);
                itemList.add(new Items(itemId));
                return true;
            }
        });

        itemsList = itemList.toArray(new Items[itemList.size()]);
        Arrays.sort(itemsList);
    }

    /**
     * The ID of the item.
     */
    private final int itemId;

    /**
     * Protected constructor to store the item ID.
     *
     * @param id the ID of the item
     */
    protected Items(final int id) {
        itemId = id;
    }

    /**
     * Get the values stored in this list. This creates a new list filled with
     * the references to the item object. The internal list remains untouched.
     *
     * @return the array of references to the item objects
     */
    @Nonnull
    public static Items[] values() {
        final Items[] tempList = new Items[itemsList.length];
        System.arraycopy(itemsList, 0, tempList, 0, itemsList.length);
        return tempList;
    }

    /**
     * Check if a ID is part oft this list.
     *
     * @param id the ID of the item to look for
     * @return {@code true} in case the item is found in the list of valid items
     */
    public static boolean contains(final int id) {
        return Arrays.binarySearch(itemsList, new Items(id)) > 0;
    }

    /**
     * Get the ID of the item.
     *
     * @return the ID of the item.
     */
    public int getItemId() {
        return itemId;
    }

    @Override
    public int compareTo(@Nonnull final Items o) {
        return Integer.valueOf(itemId).compareTo(o.itemId);
    }
}
