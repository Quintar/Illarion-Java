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
package illarion.common.data;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class provides access to all skill groups that were load.
 *
 * @author Martin Karing %lt;nitram@illarion.org%gt;
 */
@Immutable
@ThreadSafe
public final class SkillGroups {
    /**
     * The singleton instance of this class.
     */
    @Nonnull
    private static final SkillGroups INSTANCE;

    static {
        INSTANCE = new SkillGroups();
        SkillLoader.load();
    }

    /**
     * Get the instance of this class.
     *
     * @return the singleton instance of this class
     */
    @Nonnull
    public static SkillGroups getInstance() {
        return INSTANCE;
    }

    /**
     * The list of skill groups that are known to this class.
     */
    @Nonnull
    private final List<SkillGroup> skillGroupList;

    /**
     * Private constructor that prepares the internal data structures and that is used to prevent that multiple
     * instances of this class are created.
     */
    private SkillGroups() {
        skillGroupList = new ArrayList<SkillGroup>();
    }

    /**
     * Add a skill group to this class.
     *
     * @param group the skill group to add
     */
    void addSkillGroup(@Nonnull final SkillGroup group) {
        skillGroupList.add(group);
    }

    /**
     * Get a list of all skill groups that are registered.
     *
     * @return a list of skill groups
     */
    @Nonnull
    public List<SkillGroup> getSkillGroups() {
        return Collections.unmodifiableList(skillGroupList);
    }
}
