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
package illarion.client.world;

import javax.annotation.concurrent.ThreadSafe;

/**
 * This class is the implementation of the time in Illarion.
 *
 * @author Martin Karing &lt;nitram@illarion.org&gt;
 */
@SuppressWarnings("ClassNamingConvention")
@ThreadSafe
public final class Clock {
    /**
     * The day that was set during the last synchronization.
     */
    private int day;

    /**
     * The month that was set during the last synchronization.
     */
    private int month;

    /**
     * The year that was set during the last synchronization.
     */
    private int year;

    /**
     * The hour that was set during the last synchronization.
     */
    private int hour;

    /**
     * The minute that was set during the last synchronization.
     */
    private int minute;

    /**
     * The time in milliseconds when the synchronization was applied last.
     */
    private long lastSync;

    /**
     * Set the current date and time.
     *
     * @param year   the year of the new date
     * @param month  the month of the new date
     * @param day    the day of the new date
     * @param hour   the hour of the new date
     * @param minute the minute of the new date
     */
    public void setDateTime(final int year, final int month, final int day, final int hour, final int minute) {
        lastSync = System.currentTimeMillis();
        this.year = year;
        this.month = month;
        this.day = day;
        this.hour = hour;
        this.minute = minute;
    }

    /**
     * Get the current day.
     *
     * @return the current day
     */
    public int getDay() {
        return day;
    }

    /**
     * Get the current month.
     *
     * @return the current month
     */
    public int getMonth() {
        return month;
    }

    /**
     * Get the current year.
     *
     * @return the current year
     */
    public int getYear() {
        return year;
    }

    /**
     * Get the current hours.
     *
     * @return the current hour
     */
    public int getHour() {
        final long illaHoursPass = getIllaSecondPass() / 60 / 60;
        return (int) ((hour + illaHoursPass) % 24);
    }

    /**
     * Get the current minute.
     *
     * @return the current minute
     */
    public int getMinute() {
        final long illaMinutesPass = getIllaSecondPass() / 60;
        return (int) ((minute + illaMinutesPass) % 60);
    }

    /**
     * Get the current second.
     *
     * @return the current second
     */
    public int getSecond() {
        return (int) (getIllaSecondPass() % 60);
    }

    /**
     * Get the amount of seconds in Illarion Time that did pass since the last time the synchronization was applied.
     *
     * @return the elapsed time in Illarion seconds
     */
    private long getIllaSecondPass() {
        final long secondsPass = (System.currentTimeMillis() - lastSync) / 1000;
        return secondsPass * 3L;
    }
}
