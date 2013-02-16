/*
 * This file is part of the Illarion Common Library.
 *
 * Copyright © 2013 - Illarion e.V.
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

import gnu.trove.map.hash.TIntObjectHashMap;
import javolution.context.ObjectFactory;

import javax.annotation.Nonnull;

/**
 * Recycle Factory. Base handler for storing and reusing recycle objects.
 *
 * @param <T> The target class that of the objects that are handled by this
 *            recycle factory
 * @author Martin Karing &lt;nitram@illarion.org&gt;
 * @author Nop
 */
public abstract class RecycleFactory<T extends RecycleObject> {
    /**
     * The class used as factory for each prototype stored in this factory. The
     * object factories create duplicates of the prototype instance.
     *
     * @param <K> The class of the objects created by this factory
     * @author Martin Karing &lt;nitram@illarion.org&gt;
     */
    private static final class PrototypeFactory<K extends RecycleObject> extends ObjectFactory<K> {
        /**
         * The prototype used to create the duplicates.
         */
        @Nonnull
        private final K prototype;

        /**
         * The public constructor. On one hand this one allows the parent class
         * to create instances of this class. On the other hand it allows to set
         * the prototype that is needed for this factory to work properly.
         *
         * @param proto the prototype used in this class
         */
        public PrototypeFactory(@Nonnull final K proto) {
            super();
            prototype = proto;
            proto.reset();
            recycle(proto);
        }

        /**
         * Get the prototype of this factory. This object is used to create
         * every new instance. Make sure this instance is not damaged by any
         * other class in any way.
         *
         * @return the prototype instance
         */
        @Nonnull
        public K getPrototype() {
            return prototype;
        }

        /**
         * Create a new instance of the object. That causes that a clone of the
         * prototype is created.
         */
        @Nonnull
        @SuppressWarnings("unchecked")
        @Override
        protected K create() {
            return (K) prototype.clone();
        }
    }

    /**
     * Default ID for no replacement.
     */
    private static final int NONE = -1;

    /**
     * The array index that is used as default mapping.
     */
    private volatile int defaultMap = NONE;

    /**
     * The modulo offset for the default map.
     */
    private int defaultMapModulo = 1;

    /**
     * The storage that stores the factories for every object stored in this
     * recycle factory.
     */
    @Nonnull
    private final TIntObjectHashMap<PrototypeFactory<T>> storage;

    /**
     * Set up the recycle factory and prepare all storage lists.
     */
    protected RecycleFactory() {
        storage = new TIntObjectHashMap<PrototypeFactory<T>>();
    }

    /**
     * Calling this function causes that the factory optimizes itself for the
     * current load. Its a good idea to invoke this after all prototyped have
     * been loaded into the factory to get the best performance.
     */
    public final void finish() {
        storage.compact();
    }

    /**
     * Get a recycle object from the factory. Use a object in the recycler or
     * create a copy of the prototype.
     *
     * @param requestId the id of the object that is wanted
     * @return the recycle object that can be used
     */
    public T getCommand(final int requestId) {
        final T obj = getFactory(requestId).object();
        obj.activate(requestId);

        return obj;
    }

    /**
     * Get the prototype of a recycle object, mappings are considered.
     *
     * @param id the id of the object
     * @return the prototype of the object
     */
    @Nonnull
    public final T getPrototype(final int id) {
        return getFactory(id).getPrototype();
    }

    /**
     * Check if the prototype of a object exists.
     *
     * @param id the id of the prototype that shall be checked
     * @return true in case the prototype is defined
     */
    public final boolean prototypeExists(final int id) {
        return storage.contains(id);
    }

    /**
     * Put a recycle object into the recycler for later usage.
     *
     * @param obj the object that shall be recycled
     */
    @SuppressWarnings("nls")
    public void recycle(@Nonnull final T obj) {
        final int id = obj.getId();

        final PrototypeFactory<T> factory = getFactory(id);

        obj.reset();
        factory.recycle(obj);
    }

    /**
     * Set up a mapping from one ID to another. The ID that is the target of the
     * mapping needs a prototype. The only difference to {@link #map(int, int)}
     * is that the ID the mapping comes from, can have a prototype that is
     * overwritten by this command.
     *
     * @param id            the source of the mapping. This id is mapped to the replacement
     *                      id
     * @param replacementID the target of the mapping. The source id is mapped
     *                      to this one
     */
    @SuppressWarnings("nls")
    protected final void forceMap(final int id, final int replacementID) {
        final PrototypeFactory<T> targetFactory = storage.get(replacementID);
        if (targetFactory == null) {
            throw new IllegalStateException("mapping target not found");
        }

        storage.put(id, targetFactory);
    }

    /**
     * Get the ID of the prototype.
     *
     * @param id the id of the requested prototype
     * @return the actual id of the prototype
     */
    protected final int getPrototypeID(final int id) {
        return getFactory(id).getPrototype().getId();
    }

    /**
     * Set up a mapping from one ID to another. The ID that is the target of the
     * mapping needs a prototype, the ID the mapping comes from does not need
     * one.
     *
     * @param id            the source of the mapping. This id is mapped to the replacement
     *                      id
     * @param replacementID the target of the mapping. The source id is mapped
     *                      to this one
     */
    @SuppressWarnings("nls")
    protected final void map(final int id, final int replacementID) {
        if (storage.contains(id)) {
            throw new IllegalStateException("prototype overwrite not allowed");
        }

        forceMap(id, replacementID);
    }

    /**
     * Maps all empty factory slots to the given value to avoid errors.
     *
     * @param replacementID id of replacement
     * @param modulo        offset modulo this value is added to the id
     */
    protected final void mapDefault(final int replacementID, final int modulo) {
        defaultMap = replacementID;
        defaultMapModulo = modulo;
    }

    /**
     * Register a prototype to the recycle factory. This causes that the
     * prototype and the recycler for this prototype are set up.
     *
     * @param proto the prototype that shall be registered
     */
    @SuppressWarnings("nls")
    protected final void register(@Nonnull final T proto) {
        final int id = proto.getId();

        if (storage.contains(id)) {
            throw new IllegalStateException("prototype exists for id "
                    + Integer.toString(id));
        }

        storage.put(id, new PrototypeFactory<T>(proto));
    }

    /**
     * Get the factory that stores and creates the objects of the requested
     * type.
     *
     * @param id the ID of the prototype
     * @return the factory of the prototype
     */
    @SuppressWarnings("nls")
    @Nonnull
    private PrototypeFactory<T> getFactory(final int id) {
        PrototypeFactory<T> factory = storage.get(id);

        if (factory != null) {
            return factory;
        }

        if (defaultMap != NONE) {
            factory = storage.get(getMappedId(id));

            if (factory != null) {
                return factory;
            }
        }

        throw new IndexOutOfBoundsException("id " + id);
    }

    /**
     * Map a ID by the parameters set for this factory.
     *
     * @param id the ID
     * @return the ID with applied mapping
     */
    private int getMappedId(final int id) {
        return defaultMap + (id % defaultMapModulo);
    }
}
