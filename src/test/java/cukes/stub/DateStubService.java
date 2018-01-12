package cukes.stub;

import com.emprovise.service.DateService;
import com.emprovise.service.DateServiceImpl;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

@Service
@Profile("stub")
public class DateStubService implements DateService {

    private Date currentDate;
    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    @Override
    public Date getCurrentDateAndTime(final TimeZone timeZone) {
        if(currentDate == null) {
            currentDate = DateServiceImpl.getCurrentDate(timeZone);
        }

        return currentDate;
    }

    @Override
    public String getCurrentDate(final TimeZone timeZone, String format) {
        if(currentDate == null) {
            currentDate = DateServiceImpl.getCurrentDate(timeZone);
        }

        return DateServiceImpl.convertDateToRequiredStringFormat(currentDate, format);
    }

    public void setCurrentDate(String date, String formatString) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat(formatString);
        currentDate = format.parse(date);
    }

    public void setCurrentDate(Date currentDate) {
        this.currentDate = currentDate;
    }
}
