/*
 * This file is part of Gaia Sky, which is released under the Mozilla Public License 2.0.
 * See the file LICENSE.md in the project root for full license details.
 */

package gaiasky.event;

public interface IObserver {

    void notify(final Events event, final Object... data);

}
