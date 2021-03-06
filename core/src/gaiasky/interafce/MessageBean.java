/*
 * This file is part of Gaia Sky, which is released under the Mozilla Public License 2.0.
 * See the file LICENSE.md in the project root for full license details.
 */

package gaiasky.interafce;

import gaiasky.util.I18n;
import gaiasky.util.format.DateFormatFactory;
import gaiasky.util.format.DateFormatFactory.DateType;
import gaiasky.util.format.IDateFormat;

import java.time.Instant;

public class MessageBean {
    private static final String TAG_SEPARATOR = " - ";
    private static IDateFormat df = DateFormatFactory.getFormatter(I18n.locale, DateType.TIME);
    String msg;
    Instant date;


    public MessageBean(Instant date, String msg) {
        this.msg = msg;
        this.date = date;
    }

    public MessageBean(String msg) {
        this.msg = msg;
        this.date = Instant.now();
    }

    /** Has the message finished given the timeout? **/
    public boolean finished(long timeout) {
        return Instant.now().toEpochMilli() - date.toEpochMilli() > timeout;
    }

    @Override
    public String toString() {
        return formatMessage(true);
    }

    public String formatMessage(boolean writeDates) {
        return (writeDates ? df.format(this.date) + TAG_SEPARATOR : "") + this.msg;
    }

}
