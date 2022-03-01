package de.tsearch.statuscollector.filter;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@Component
@Order(10)
public class TwitchMessageHeaderFilter extends OncePerRequestFilter {

    private static final Set<String> neededHeaders = new HashSet<>();

    static {
        neededHeaders.add("Twitch-Eventsub-Message-Id");
        neededHeaders.add("Twitch-Eventsub-Message-Type");
        neededHeaders.add("Twitch-Eventsub-Message-Signature");
        neededHeaders.add("Twitch-Eventsub-Message-Timestamp");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {
        logger.trace("Check request for twitch header");
        boolean missingHeader = false;
        for (String neededHeader : neededHeaders) {
            if (httpServletRequest.getHeader(neededHeader) == null) {
                logger.trace("Missing " + neededHeader + " in request!");
                missingHeader = true;
                break;
            }
        }

        if (missingHeader) {
            httpServletResponse.setStatus(400);
        } else {
            filterChain.doFilter(httpServletRequest, httpServletResponse);
        }
    }
}
