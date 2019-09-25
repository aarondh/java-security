package org.daisleyharrison.security.samples.jerseyService.models;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

@Provider
@Produces("text/html")
public class ErrorResponseWriter implements MessageBodyWriter<ErrorResponse> {

    public static String escapeHTML(String s) {
        StringBuilder out = new StringBuilder(Math.max(16, s.length()));
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c > 127 || c == '"' || c == '<' || c == '>' || c == '&') {
                out.append("&#");
                out.append((int) c);
                out.append(';');
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }
    @Override
    public boolean isWriteable(Class<?> type, Type genericType,
                               Annotation[] annotations, MediaType mediaType) {
        return type == ErrorResponse.class;
    }

    @Override
    public long getSize(ErrorResponse errorResponse, Class<?> type, Type genericType,
                        Annotation[] annotations, MediaType mediaType) {
        // deprecated by JAX-RS 2.0 and ignored by Jersey runtime
        return 0;
    }

    @Override
    public void writeTo(ErrorResponse errorResponse, Class<?> type, Type genericType, Annotation[] annotations,
                        MediaType mediaType, MultivaluedMap<String, Object> httpHeaders,
                        OutputStream out) throws IOException, WebApplicationException {

        Writer writer = new PrintWriter(out);
        writer.write("<html>");
        writer.write("<body>");
        writer.write("<h2>Error</h2>");
        writer.write("<p>Error: " + escapeHTML(errorResponse.getError()) + "</p>");
        writer.write("<p>Description: " + escapeHTML(errorResponse.getError_description()) + "</p>");
        writer.write("</body>");
        writer.write("</html>");

        writer.flush();
        writer.close();
    }
}