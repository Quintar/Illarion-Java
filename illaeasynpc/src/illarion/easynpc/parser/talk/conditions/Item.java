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
package illarion.easynpc.parser.talk.conditions;

import illarion.easynpc.Lang;
import illarion.easynpc.data.CompareOperators;
import illarion.easynpc.data.ItemPositions;
import illarion.easynpc.data.Items;
import illarion.easynpc.parsed.shared.ParsedItemData;
import illarion.easynpc.parsed.talk.AdvancedNumber;
import illarion.easynpc.parsed.talk.TalkCondition;
import illarion.easynpc.parsed.talk.conditions.ConditionItem;
import illarion.easynpc.parser.shared.ItemData;
import illarion.easynpc.parser.talk.AdvNumber;
import illarion.easynpc.parser.talk.ConditionParser;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rsyntaxtextarea.TokenMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is a item condition. Its able to parse a item value out of the NPC condition line.
 *
 * @author Martin Karing &lt;nitram@illarion.org&gt;
 */
public final class Item extends ConditionParser {
    /**
     * This pattern is used to find the item operation in the condition properly.
     */
    @SuppressWarnings("nls")
    private static final Pattern ITEM_FIND = Pattern.compile("\\s*item\\s*\\(\\s*([\\d]{1,4})\\s*," +
            "\\s*([a-z]+)\\s*([,]\\s*" + ItemData.REGEXP + "\\s*)?(\\s*[,]\\s*0\\s)?\\)\\s*([=~!<>]{1,2})\\s*" + AdvNumber.ADV_NUMBER_REGEXP +
            "\\s*,\\s*", Pattern.CASE_INSENSITIVE);

    /**
     * Extract a condition from the working string.
     */
    @Nullable
    @Override
    @SuppressWarnings("nls")
    public TalkCondition extract() {
        if (getNewLine() == null) {
            throw new IllegalStateException("Can't extract if no string set.");
        }

        final Matcher stringMatcher = ITEM_FIND.matcher(getNewLine());
        if (stringMatcher.find()) {
            final int itemId = Integer.parseInt(stringMatcher.group(1));
            final String itemPos = stringMatcher.group(2).toLowerCase();

            final String dataString = stringMatcher.group(4);
            final ParsedItemData data;
            if (dataString != null) {
                data = ItemData.getData(stringMatcher.group(5));
            } else {
                data = ItemData.getData("");
            }

            final String comparator = stringMatcher.group(stringMatcher.groupCount() - 1);
            final AdvancedNumber targetValue =
                    AdvNumber.getNumber(stringMatcher.group(stringMatcher.groupCount()));

            setLine(stringMatcher.replaceFirst(""));

            if (targetValue == null) {
                reportError(String.format(Lang.getMsg(getClass(), "number"),
                        stringMatcher.group(stringMatcher.groupCount()), stringMatcher.group(0)));
                return extract();
            }

            Items item = null;
            for (final Items it : Items.values()) {
                if (it.getItemId() == itemId) {
                    item = it;
                    break;
                }
            }

            if (item == null) {
                reportError(String.format(Lang.getMsg(getClass(), "item"),
                        Integer.toString(itemId), stringMatcher.group(0)));
                return extract();
            }

            ItemPositions itemPosition = null;
            for (final ItemPositions itPos : ItemPositions.values()) {
                if (itPos.name().equalsIgnoreCase(itemPos)) {
                    itemPosition = itPos;
                    break;
                }
            }

            if (itemPosition == null) {
                reportError(String.format(Lang.getMsg(getClass(), "itemPos"),
                        itemPos, stringMatcher.group(0)));
                return extract();
            }

            CompareOperators operator = null;
            for (final CompareOperators op : CompareOperators.values()) {
                if (op.getRegexpPattern().matcher(comparator).matches()) {
                    operator = op;
                    break;
                }
            }

            if (operator == null) {
                reportError(String.format(Lang.getMsg(getClass(), "operator"),
                        comparator, stringMatcher.group(0)));
                return extract();
            }

            return new ConditionItem(item, itemPosition, operator, targetValue, data);
        }

        return null;
    }

    @Override
    public String getDescription() {
        return Lang.getMsg(getClass(), "Docu.description"); //$NON-NLS-1$
    }

    @Override
    public String getExample() {
        return Lang.getMsg(getClass(), "Docu.example"); //$NON-NLS-1$
    }

    @Override
    public String getSyntax() {
        return Lang.getMsg(getClass(), "Docu.syntax"); //$NON-NLS-1$
    }

    @Override
    public String getTitle() {
        return Lang.getMsg(getClass(), "Docu.title"); //$NON-NLS-1$
    }

    @Override
    public void enlistHighlightedWords(@Nonnull final TokenMap map) {
        map.put("item", Token.RESERVED_WORD);
    }
}
