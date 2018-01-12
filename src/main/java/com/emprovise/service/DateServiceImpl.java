package com.emprovise.service;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

@Service
@Profile("!stub")
public class DateServiceImpl implements DateService {

    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";

    public Date getCurrentDateAndTime(final TimeZone timeZone) {
        if (timeZone == null) {
            throw new RuntimeException(String.format("Timezone for the current user %s is null"));
        }
        return getCurrentDate(timeZone);
    }

    public String getCurrentDate(final TimeZone timeZone, String format) {
        String currentDate = null;
        Date date = getCurrentDateAndTime(timeZone);
        currentDate = convertDateToRequiredStringFormat(date, format);
        return currentDate;
    }

    public static Date getCurrentDate(TimeZone timeZone) {
        Calendar cal = Calendar.getInstance();
        Date today = cal.getTime();
        Date date = convertTimeZone(today, cal.getTimeZone(), timeZone);
        return date;
    }

    public static Date getCurrentDateTime() {
        Calendar cal = Calendar.getInstance();
        return cal.getTime();
    }

    public static Date convertTimeZone(java.util.Date date, TimeZone fromTZ , TimeZone toTZ) {
        long fromTZDst = 0;

        if(fromTZ.inDaylightTime(date)) {
            fromTZDst = fromTZ.getDSTSavings();
        }

        long fromTZOffset = fromTZ.getRawOffset() + fromTZDst;
        long toTZDst = 0;

        if(toTZ.inDaylightTime(date)) {
            toTZDst = toTZ.getDSTSavings();
        }

        long toTZOffset = toTZ.getRawOffset() + toTZDst;
        return new java.util.Date(date.getTime() + (toTZOffset - fromTZOffset));
    }

    public static String convertDateToRequiredStringFormat(Date date, String formatTo){
        String strDate = "";
        if (null != date) {
            strDate = new SimpleDateFormat(formatTo).format(date);
        }
        return strDate;
    }

    public static Date convertStringToDate(String strdate, String format) throws ParseException {
        Date frmDate = null;
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        if (strdate != null && !strdate.isEmpty()) {
            frmDate = sdf.parse(strdate);
        }
        return frmDate;
    }
}
