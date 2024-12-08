package servlets;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.http.*;
import util.Util;

public class CharResponseWrapper extends HttpServletResponseWrapper {
    private CharArrayWriter charWriter = new CharArrayWriter();
    private PrintWriter writer = new PrintWriter(charWriter);

    public CharResponseWrapper(HttpServletResponse response) {
        super(response);
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return writer;
    }

    @Override
    public String toString() {
        return charWriter.toString();
    }

    public String processJspContent(String originalContent, HttpServletRequest request) {
        String returnToValue = null;
        if (request.getParameter("origin") == null) {
            returnToValue = Util.getUrlPath(request.getRequestURL().toString(),
                    new FrontServlet().getBaseUrl(request));
        } else {
            returnToValue = request.getParameter("origin");
        }
        return originalContent.replaceAll(
                "(<form[^>]*>)",
                "$1\n<input type=\"hidden\" name=\"origin\" value=\""
                        + returnToValue + "\">");
    }
}
