package cukes.sync;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;

import java.io.IOException;

public class CustomPrettyPrinter extends DefaultPrettyPrinter {

    @Override
    public DefaultPrettyPrinter createInstance() {
        return this;
    }

    @Override
    public void writeObjectFieldValueSeparator(JsonGenerator g) throws IOException {
        g.writeRaw(": ");
    }

    @Override
    public void writeEndArray(JsonGenerator g, int nrOfValues) throws IOException {
        if (!this._arrayIndenter.isInline()) {
            --this._nesting;
        }

        if (nrOfValues > 0) {
            this._arrayIndenter.writeIndentation(g, this._nesting);
        }

        g.writeRaw(']');
    }
}
