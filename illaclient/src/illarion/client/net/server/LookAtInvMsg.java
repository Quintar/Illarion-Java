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

import illarion.client.gui.Tooltip;
import illarion.client.net.CommandList;
import illarion.client.net.annotations.ReplyMessage;
import illarion.client.world.World;
import illarion.common.net.NetCommReader;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * Servermessage: Look at description of item in the inventory ({@link CommandList#MSG_LOOKAT_INV}).
 *
 * @author Martin Karing &lt;nitram@illarion.org&gt;
 * @author Nop
 */
@ReplyMessage(replyId = CommandList.MSG_LOOKAT_INV)
public final class LookAtInvMsg extends AbstractReply {
    /**
     * Inventory slot that message is related to.
     */
    private short slot;

    /**
     * The tooltip that is supposed to be displayed at the inventory slot.
     */
    private Tooltip tooltip;

    /**
     * Decode the inventory item look at text data the receiver got and prepare
     * it for the execution.
     *
     * @param reader the receiver that got the data from the server that needs
     *               to be decoded
     * @throws IOException thrown in case there was not enough data received to
     *                     decode the full message
     */
    @Override
    public void decode(@Nonnull final NetCommReader reader) throws IOException {
        slot = reader.readUByte();
        tooltip = new Tooltip(reader);
    }

    /**
     * Execute the inventory item look at text message and send the decoded data to the rest of the client.
     *
     * @return true if the execution is done, false if it shall be called again
     */
    @Override
    public boolean executeUpdate() {
        World.getGameGui().getInventoryGui().showTooltip(slot, tooltip);
        return true;
    }

    /**
     * Get the data of this inventory item look at text message as string.
     *
     * @return the string that contains the values that were decoded for this  message
     */
    @Nonnull
    @SuppressWarnings("nls")
    @Override
    public String toString() {
        return toString("Slot: " + slot + ' ' + tooltip);
    }
}
