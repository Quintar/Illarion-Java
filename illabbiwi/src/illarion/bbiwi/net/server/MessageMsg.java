/*
 * This file is part of the Illarion BBIWI.
 *
 * Copyright © 2013 - Illarion e.V.
 *
 * The Illarion BBIWI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The Illarion BBIWI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the Illarion BBIWI.  If not, see <http://www.gnu.org/licenses/>.
 */
package illarion.bbiwi.net.server;

import illarion.common.net.NetCommReader;
import illarion.common.net.ReplyMessage;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * This message is send by the server to report a message to the BBIWI tool.
 *
 * @author Martin Karing &lt;nitram@illarion.org&gt;
 */
@ReplyMessage(replyId = 0x01)
public final class MessageMsg extends AbstractReply {
    /**
     * The type constant for a normal message that is shown in the server message window.
     */
    private static final int MSG_TYPE_NORMAL = 0;

    /**
     * The type constant for a server message that is shown as popup window.
     */
    private static final int MSG_TYPE_POPUP = 1;

    /**
     * The type constant for a GM related server message.
     */
    private static final int MSG_TYPE_GM = 2;

    /**
     * The received message itself.
     */
    private String message;

    /**
     * The type of the message that was received.
     */
    private int type;

    @Override
    public void decode(@Nonnull final NetCommReader reader) throws IOException {
        message = reader.readString();
        type = reader.readUByte();
    }

    @Override
    public boolean executeUpdate() {
        // TODO: Show message in GUI
        return true;
    }

    @Override
    public String toString() {
        return toString("Message: \"" + message + "\" Type: " + type);
    }
}
