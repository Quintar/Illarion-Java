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

import illarion.client.gui.ChatGui;
import illarion.client.gui.GameGui;
import illarion.client.net.CommandList;
import illarion.client.util.Lang;
import illarion.client.world.World;
import illarion.common.net.NetCommReader;
import illarion.common.net.ReplyMessage;
import javolution.text.TextBuilder;
import org.apache.log4j.Logger;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;

/**
 * The inform message that is used to receive inform messages from the server.
 *
 * @author Martin Karing &gt;nitram@illarion.org&lt;
 */
@ReplyMessage(replyId = CommandList.MSG_INFORM)
public final class InformMsg extends AbstractReply {
    /**
     * The logger that is used for the log output of this class.
     */
    @Nonnull
    private static final Logger LOGGER = Logger.getLogger(InformMsg.class);

    /**
     * The type of the inform.
     */
    private int informType;

    /**
     * The text of the inform.
     */
    @Nullable
    private String informText;

    @Nonnull
    @Override
    public String toString() {
        final TextBuilder builder = TextBuilder.newInstance();
        try {
            builder.append("Type: ").append(informType);
            builder.append(" Text: ").append(informText);
            return toString(builder.toString());
        } finally {
            TextBuilder.recycle(builder);
        }
    }

    @Override
    public void decode(@Nonnull final NetCommReader reader) throws IOException {
        informType = reader.readUByte();
        informText = reader.readString();
    }

    @Override
    public boolean executeUpdate() {
        if (informText == null) {
            throw new IllegalStateException("Executing a inform message while the inform text is not set.");
        }

        final GameGui gui = World.getGameGui();
        switch (informType) {
            case 0:
                gui.getInformGui().showServerInform(informText);
                gui.getChatGui().addChatMessage(Lang.getMsg("chat.broadcast") + ": " + informText,
                        ChatGui.COLOR_DEFAULT);
                break;
            case 1:
                gui.getInformGui().showBroadcastInform(informText);
                break;
            case 2:
                gui.getInformGui().showTextToInform(informText);
                gui.getChatGui().addChatMessage(Lang.getMsg("chat.textto") + ": " + informText,
                        ChatGui.COLOR_DEFAULT);
                break;
            case 100:
                gui.getInformGui().showScriptInform(0, informText);
                break;
            case 101:
                gui.getInformGui().showScriptInform(1, informText);
                gui.getChatGui().addChatMessage(Lang.getMsg("chat.scriptInform") + ": " + informText,
                        ChatGui.COLOR_DEFAULT);
                break;
            case 102:
                gui.getInformGui().showScriptInform(2, informText);
                gui.getChatGui().addChatMessage(Lang.getMsg("chat.scriptInform") + ": " + informText,
                        ChatGui.COLOR_SHOUT);
                break;

            default:
                TextBuilder builder = null;
                try {
                    builder = TextBuilder.newInstance();
                    builder.append("Received inform with unknown type: ").append(informType);
                    builder.append("(").append(informText).append(")");
                    LOGGER.warn(builder.toString());
                } finally {
                    if (builder != null) {
                        TextBuilder.recycle(builder);
                    }
                }
        }
        return true;
    }
}
