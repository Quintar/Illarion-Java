/*
 * This file is part of the Illarion Client.
 *
 * Copyright © 2012 - Illarion e.V.
 *
 * The Illarion Client is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The Illarion Client is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the Illarion Client.  If not, see <http://www.gnu.org/licenses/>.
 */
package illarion.client.net.server;

import illarion.client.net.CommandList;
import illarion.client.net.annotations.ReplyMessage;
import illarion.client.world.MapTile;
import illarion.client.world.World;
import illarion.common.net.NetCommReader;
import illarion.common.types.ItemCount;
import illarion.common.types.ItemId;
import illarion.common.types.Location;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Servermessage: Update Items on map ( {@link illarion.client.net.CommandList#MSG_UPDATE_ITEMS}).
 *
 * @author Martin Karing &lt;nitram@illarion.org&gt;
 * @author Nop
 */
@ReplyMessage(replyId = CommandList.MSG_UPDATE_ITEMS)
public final class ItemUpdateMsg
        extends AbstractReply {
    /**
     * Default size of the array that stores the items on this field. The size is increase automatically if needed.
     */
    private static final int DEFAULT_SIZE = 5;

    /**
     * Count values for each item on this map tile.
     */
    private final List<ItemCount> itemCount = new ArrayList<ItemCount>(DEFAULT_SIZE);

    /**
     * List of the item IDs on this map tile.
     */
    private final List<ItemId> itemId = new ArrayList<ItemId>(DEFAULT_SIZE);

    /**
     * Amount of item stacks on the map tile.
     */
    private short itemNumber;

    /**
     * Position of the server map that is updated.
     */
    private transient Location loc;

    /**
     * Decode the items on tile data the receiver got and prepare it for the execution.
     *
     * @param reader the receiver that got the data from the server that needs to be decoded
     * @throws IOException thrown in case there was not enough data received to decode the full message
     */
    @Override
    public void decode(@Nonnull final NetCommReader reader)
            throws IOException {
        loc = decodeLocation(reader);

        itemNumber = reader.readUByte();

        for (int i = 0; i < itemNumber; ++i) {
            itemId.add(new ItemId(reader));
            itemCount.add(ItemCount.getInstance(reader));
        }
    }

    /**
     * Execute the items on tile message and send the decoded data to the rest of the client.
     *
     * @return true if the execution is done, false if it shall be called again
     */
    @Override
    public boolean executeUpdate() {
        final MapTile tile = World.getMap().getMapAt(loc);
        if (tile != null) {
            tile.updateItems(itemNumber, itemId, itemCount);
        }

        return true;
    }

    /**
     * Get the data of this items on tile message as string.
     *
     * @return the string that contains the values that were decoded for this message
     */
    @Nonnull
    @SuppressWarnings("nls")
    @Override
    public String toString() {
        return toString("Loc: " + loc.toString() + " - Items: " + itemId.toString());
    }
}
