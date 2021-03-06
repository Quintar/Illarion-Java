/*
 * This file is part of the Illarion Game Engine.
 *
 * Copyright © 2013 - Illarion e.V.
 *
 * The Illarion Game Engine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The Illarion Game Engine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with the Illarion Game Engine.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.illarion.engine.backend.gdx;

import org.illarion.engine.sound.Music;
import org.illarion.engine.sound.Sound;
import org.illarion.engine.sound.Sounds;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * This is the sound engine that uses the libGDX backend.
 *
 * @author Martin Karing &lt;nitram@illarion.org&gt;
 */
class GdxSounds implements Sounds {

    /**
     * The current global music volume.
     */
    private float musicVolume;

    /**
     * The music that is currently played in the background.
     */
    @Nullable
    private com.badlogic.gdx.audio.Music currentBackgroundMusic;

    /**
     * The current sound volume;
     */
    private float soundVolume;

    /**
     * Create a new instance of the libGDX sound system.
     */
    GdxSounds() {
        musicVolume = 1.f;
        soundVolume = 1.f;
    }

    @Override
    public float getMusicVolume() {
        return musicVolume;
    }

    @Override
    public void setMusicVolume(final float volume) {
        musicVolume = volume;
        if (currentBackgroundMusic != null) {
            currentBackgroundMusic.setVolume(volume);
        }
    }

    @Override
    public float getSoundVolume() {
        return soundVolume;
    }

    @Override
    public void setSoundVolume(final float volume) {
        soundVolume = volume;
    }

    @Override
    public float getSoundVolume(@Nonnull final Sound sound, final int handle) {
        return soundVolume;
    }

    @Override
    public boolean isMusicPlaying(@Nonnull final Music music) {
        if (music instanceof GdxMusic) {
            return ((GdxMusic) music).getWrappedMusic().isPlaying();
        }
        return false;
    }

    @Override
    public boolean isSoundPlaying(@Nonnull final Sound sound, final int handle) {
        return false;
    }

    @Override
    public void playMusic(@Nonnull final Music music, final int fadeOutTime, final int fadeInTime) {
        if (currentBackgroundMusic != null) {
            currentBackgroundMusic.stop();
        }
        if (music instanceof GdxMusic) {
            currentBackgroundMusic = ((GdxMusic) music).getWrappedMusic();
            currentBackgroundMusic.setLooping(true);
            currentBackgroundMusic.setVolume(getMusicVolume());
            currentBackgroundMusic.play();
        }
    }

    @Override
    public int playSound(@Nonnull final Sound sound, final float volume) {
        if (sound instanceof GdxSound) {
            return (int) ((GdxSound) sound).getWrappedSound().play(soundVolume * volume);
        }
        return -1;
    }

    @Override
    public int playSound(@Nonnull final Sound sound, final float volume, final int offsetX, final int offsetY, final int offsetZ) {
        if (sound instanceof GdxSound) {
            return (int) ((GdxSound) sound).getWrappedSound().play(soundVolume * volume);
        }
        return -1;
    }

    @Override
    public void poll(final int delta) {
        // nothing
    }

    @Override
    public void setSoundVolume(@Nonnull final Sound sound, final int handle, final float volume) {
        if (sound instanceof GdxSound) {
            ((GdxSound) sound).getWrappedSound().setVolume(handle, volume);
        }
    }

    @Override
    public void stopMusic(final int fadeOutTime) {
        if (currentBackgroundMusic != null) {
            currentBackgroundMusic.stop();
        }
    }

    @Override
    public void stopSound(@Nonnull final Sound sound, final int handle) {
        if (sound instanceof GdxSound) {
            ((GdxSound) sound).getWrappedSound().stop(handle);
        }
    }

    @Override
    public void stopSound(@Nonnull final Sound sound) {
        if (sound instanceof GdxSound) {
            ((GdxSound) sound).getWrappedSound().stop();
        }
    }
}
