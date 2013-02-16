/*
 * This file is part of the Illarion Common Library.
 *
 * Copyright © 2012 - Illarion e.V.
 *
 * The Illarion Common Library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The Illarion Common Library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the Illarion Common Library.  If not, see <http://www.gnu.org/licenses/>.
 */
package illarion.common.util;

import illarion.common.types.Location;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.InputStream;

/**
 * This is a special implementation of the table loader that targets the characters table file.
 *
 * @author Martin Karing &gt;nitram@illarion.org&lt;
 */
public final class TableLoaderCharacters extends TableLoader {
    /**
     * The table index that stores the id of the animation this avatar shows.
     */
    private static final int TB_ANIMATION = 15;

    /**
     * The table index that stores the appearance of the avatar that is send by
     * the server to request the client to show this avatar.
     */
    private static final int TB_APPEARANCE = 6;

    /**
     * The table intex that stores the blue value of the avatar.
     */
    private static final int TB_BLUE = 18;

    /**
     * The table index that stores the direction the avatar is looking at.
     */
    private static final int TB_DIRECTION = 7;

    /**
     * The table index that stores the amount of frames of this avatar.
     */
    private static final int TB_FRAME = 2;

    /**
     * The table intex that stores the green value of the avatar.
     */
    private static final int TB_GREEN = 17;

    /**
     * The table index of the character ID.
     */
    private static final int TB_ID = 0;

    /**
     * The table index that stores if the graphic should be horizontal mirrored
     * or not.
     */
    private static final int TB_MIRROR = 14;

    /**
     * The table index of the name base of the files of the avatar.
     */
    private static final int TB_NAME = 1;

    /**
     * The table index that stores the x offset of the avatar graphic.
     */
    private static final int TB_OFFX = 4;

    /**
     * The table index that stores the y offset of the avatar graphic.
     */
    private static final int TB_OFFY = 5;

    /**
     * The table intex that stores the red value of the avatar.
     */
    private static final int TB_RED = 16;

    /**
     * The table index that stores the length of the shadow of this avatar
     * graphic.
     */
    private static final int TB_SHADOW = 9;

    /**
     * The table index that stores the first and the last frame of a animation.
     */
    private static final int TB_STILL = 3;

    /**
     * The table index that stores the visibility bonus of this avatar.
     */
    private static final int TB_VISIBLE = 10;


    public TableLoaderCharacters(@Nonnull final TableLoaderSink<TableLoaderCharacters> callback) {
        super("Chars", callback);
    }

    public TableLoaderCharacters(@Nonnull final File table, @Nonnull final TableLoaderSink<TableLoaderCharacters> callback) {
        super(table, callback);
    }

    public TableLoaderCharacters(final String table, @Nonnull final TableLoaderSink<TableLoaderCharacters> callback) {
        super(table, callback);
    }

    public TableLoaderCharacters(@Nonnull final File table, @Nonnull final TableLoaderSink<TableLoaderCharacters> callback, final String tableDelim) {
        super(table, callback, tableDelim);
    }

    public TableLoaderCharacters(final InputStream resource, final boolean ndsc, @Nonnull final TableLoaderSink<TableLoaderCharacters> callback, final String tableDelim) {
        super(resource, ndsc, callback, tableDelim);
    }

    public TableLoaderCharacters(final String table, final boolean ndsc, @Nonnull final TableLoaderSink<TableLoaderCharacters> callback, final String tableDelim) {
        super(table, ndsc, callback, tableDelim);
    }

    /**
     * Get the ID of this avatar.
     *
     * @return the ID of the avatar
     */
    public int getAvatarId() {
        return getInt(TB_ID);
    }

    /**
     * Get the amount of frames of the animation of this avatar.
     *
     * @return the frame count of the avatar
     */
    public int getFrameCount() {
        return getInt(TB_FRAME);
    }

    /**
     * Get the amount of frames of the animation of this effect.
     *
     * @return the frame count of the effect
     */
    public int getStillFrame() {
        return getInt(TB_STILL);
    }

    /**
     * The X-component of the offset of the avatar graphics.
     *
     * @return the offset component that needs to be applied to the avatar graphics
     */
    public int getOffsetX() {
        return getInt(TB_OFFX);
    }

    /**
     * The Y-component of the offset of the avatar graphics.
     *
     * @return the offset component that needs to be applied to the avatar graphics
     */
    public int getOffsetY() {
        return getInt(TB_OFFY);
    }

    /**
     * Get the shadow offset of this avatar
     *
     * @return this offset is the amount of pixels that reduces the interactive area of this avatar
     */
    public int getShadowOffset() {
        return getInt(TB_SHADOW);
    }

    /**
     * Check if the graphics of this avatar are supposed to be displayed mirrored.
     *
     * @return {@code true} in case the graphics are supposed to be rendered mirrored
     */
    public boolean isMirrored() {
        return getBoolean(TB_MIRROR);
    }

    /**
     * Get the direction value of the avatar. This is the direction the avatar is facing.
     *
     * @return the direction the avatar is looking at
     * @see Location
     */
    public int getDirection() {
        return getInt(TB_DIRECTION);
    }

    /**
     * Get the appearance of the avatar.
     *
     * @return the avatar appearance
     */
    public int getAppearance() {
        return getInt(TB_APPEARANCE);
    }

    /**
     * Get the visibility modifier of this avatar.
     *
     * @return the visibility modifier
     */
    public int getVisibilityMod() {
        return getInt(TB_VISIBLE);
    }

    /**
     * Get the ID of the animation of this avatar.
     *
     * @return the animation ID
     */
    public int getAnimationId() {
        return getInt(TB_ANIMATION);
    }

    /**
     * Get the red color component of the skin color of this avatar.
     *
     * @return the red skin color component
     */
    public int getSkinColorRed() {
        return getInt(TB_RED);
    }

    /**
     * Get the green color component of the skin color of this avatar.
     *
     * @return the green skin color component
     */
    public int getSkinColorGreen() {
        return getInt(TB_GREEN);
    }

    /**
     * Get the blue color component of the skin color of this avatar.
     *
     * @return the blue skin color component
     */
    public int getSkinColorBlue() {
        return getInt(TB_BLUE);
    }

    /**
     * Get the resource name of the effect. This name is supposed to be used to fetch the graphics of this avatar from
     * the resource loader.
     *
     * @return the resource name of this effect
     */
    public String getResourceName() {
        return getString(TB_NAME);
    }
}
