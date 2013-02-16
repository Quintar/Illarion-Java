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
package illarion.easynpc.parser;

import illarion.easynpc.EasyNpcScript;
import illarion.easynpc.Lang;
import illarion.easynpc.ParsedNpc;
import illarion.easynpc.docu.DocuEntry;
import illarion.easynpc.parsed.ParsedTradeText;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rsyntaxtextarea.TokenMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is used to parse the texts related to trading from the NPC.
 *
 * @author Martin Karing &lt;nitram@illarion.org&gt;
 */
public final class NpcTradeText implements NpcType {
    private static final Pattern WRONG_ITEM_PATTERN = Pattern.compile(
            "^\\s*(tradeWrongItemMsg)\\s*[\\(]*\\s*\"([^\"]*)\"\\s*,\\s*\"([^\"]*)\"\\s*[\\)]*\\s*$");
    private static final Pattern NOT_ENOUGH_MONEY_PATTERN = Pattern.compile(
            "^\\s*(tradeNotEnoughMoneyMsg)\\s*[\\(]*\\s*\"([^\"]*)\"\\s*,\\s*\"([^\"]*)\"\\s*[\\)]*\\s*$");
    private static final Pattern TRADE_ENDED_PATTERN = Pattern.compile(
            "^\\s*(tradeFinishedMsg)\\s*[\\(]*\\s*\"([^\"]*)\"\\s*,\\s*\"([^\"]*)\"\\s*[\\)]*\\s*$");
    private static final Pattern TRADE_ENDED_NO_ACTION_PATTERN = Pattern.compile(
            "^\\s*(tradeFinishedWithoutTradingMsg)\\s*[\\(]*\\s*\"([^\"]*)\"\\s*,\\s*\"([^\"]*)\"\\s*[\\)]*\\s*$");

    @Override
    public boolean canParseLine(@Nonnull final EasyNpcScript.Line line) {
        return WRONG_ITEM_PATTERN.matcher(line.getLine()).matches() ||
                NOT_ENOUGH_MONEY_PATTERN.matcher(line.getLine()).matches() ||
                TRADE_ENDED_PATTERN.matcher(line.getLine()).matches() ||
                TRADE_ENDED_NO_ACTION_PATTERN.matcher(line.getLine()).matches();
    }

    @Override
    public void parseLine(@Nonnull final EasyNpcScript.Line line, @Nonnull final ParsedNpc npc) {
        ParsedTradeText entry = parseHelper(NOT_ENOUGH_MONEY_PATTERN.matcher(line.getLine()),
                ParsedTradeText.TradeTextType.NoMoney);
        if (entry == null) {
            entry = parseHelper(TRADE_ENDED_PATTERN.matcher(line.getLine()),
                    ParsedTradeText.TradeTextType.TradingCanceled);
            if (entry == null) {
                entry = parseHelper(TRADE_ENDED_NO_ACTION_PATTERN.matcher(line.getLine()),
                        ParsedTradeText.TradeTextType.TradingCanceledWithoutTrade);
                if (entry == null) {
                    entry = parseHelper(WRONG_ITEM_PATTERN.matcher(line.getLine()),
                            ParsedTradeText.TradeTextType.WrongItem);
                    if (entry == null) {
                        return;
                    }
                }
            }
        }
        npc.addNpcData(entry);
    }

    /**
     * This support function is used to check if the matcher matches the assigned line and if it does to extract the
     * text and pack it into a parsed object.
     *
     * @param matcher the matcher
     * @param type    the type of the text tested
     * @return the parsed object in case the matcher matched, else {@code null}
     */
    @Nullable
    private static ParsedTradeText parseHelper(@Nonnull final Matcher matcher, final ParsedTradeText.TradeTextType type) {
        if (matcher.find()) {
            final String germanText = matcher.group(2);
            final String englishText = matcher.group(3);
            return new ParsedTradeText(type, germanText, englishText);
        }
        return null;
    }

    @Override
    public void enlistHighlightedWords(@Nonnull final TokenMap map) {
        map.put("tradeNotEnoughMoneyMsg", Token.RESERVED_WORD);
        map.put("tradeFinishedMsg", Token.RESERVED_WORD);
        map.put("tradeFinishedWithoutTradingMsg", Token.RESERVED_WORD);
        map.put("tradeWrongItemMsg", Token.RESERVED_WORD);
    }

    @Nonnull
    @Override
    public DocuEntry getChild(final int index) {
        throw new IllegalArgumentException("There are no children to request.");
    }

    @Override
    public int getChildCount() {
        return 0;
    }

    @Override
    public String getDescription() {
        return Lang.getMsg(NpcTradeText.class, "Docu.description");
    }

    @Override
    public String getExample() {
        return Lang.getMsg(NpcTradeText.class, "Docu.example");
    }

    @Override
    public String getSyntax() {
        return Lang.getMsg(NpcTradeText.class, "Docu.syntax");
    }

    @Override
    public String getTitle() {
        return Lang.getMsg(NpcTradeText.class, "Docu.title");
    }
}
