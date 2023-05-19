package pk.wgu.capstone.data.converter;

import com.vaadin.flow.data.binder.Result;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.converter.Converter;

import java.sql.Date;
import java.time.LocalDate;

public class SqlDateToLocalDateConverter implements Converter<LocalDate, Date> {

    @Override
    public Result<Date> convertToModel(LocalDate value,ValueContext context) {
        if (value == null) {
            return Result.ok(null);
        }
        return Result.ok(java.sql.Date.valueOf(value));
    }

    @Override
    public LocalDate convertToPresentation(java.sql.Date value, ValueContext context) {
        return value.toLocalDate();
    }
}
