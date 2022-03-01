package de.tsearch.statuscollector.filter.entity;

import lombok.Getter;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class RequestMock extends HttpServletRequestWrapper {

    @Getter
    final ByteArrayOutputStream byteArrayOutputStream;

    final ServletInputStream contentInputStream;


    /**
     * Constructs a request object wrapping the given request.
     *
     * @param request The request to wrap
     * @throws IllegalArgumentException if the request is null
     */
    public RequestMock(HttpServletRequest request) {
        super(request);

        this.byteArrayOutputStream = new ByteArrayOutputStream();
        try {
            request.getInputStream().transferTo(byteArrayOutputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        InputStream firstClone = new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        contentInputStream = new ServletInputStream() {
            @Override
            public boolean isFinished() {
                try {
                    return firstClone.available() == 0;
                } catch (IOException e) {
                    return true;
                }
            }

            @Override
            public boolean isReady() {
                try {
                    return firstClone.available() == 0;
                } catch (IOException e) {
                    return false;
                }
            }

            @Override
            public void setReadListener(ReadListener listener) {
            }

            @Override
            public int read() throws IOException {
                return firstClone.read();
            }
        };
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return this.contentInputStream;
    }
}
