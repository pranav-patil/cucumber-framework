package com.library.service;

import java.util.Date;
import java.util.TimeZone;

public interface DateService {

    Date getCurrentDateAndTime(final TimeZone timeZone);

    String getCurrentDate(final TimeZone timeZone, String format);
}
