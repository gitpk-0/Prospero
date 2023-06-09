package pk.wgu.capstone.data.converter;

import com.vaadin.flow.data.binder.Result;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.converter.Converter;

import java.math.BigDecimal;

public class BigDecimalToDoubleConverter implements Converter<Double, BigDecimal> {

    @Override
    public Result<BigDecimal> convertToModel(Double value, ValueContext context) {
        if (value == null) {
            return Result.ok(null);
        }
        return Result.ok(BigDecimal.valueOf(value));
    }

    @Override
    public Double convertToPresentation(BigDecimal value, ValueContext context) {
        if (value == null) {
            return null;
        }
        return value.doubleValue();
    }
}
