package de.tsearch.statuscollector.filter;

import de.tsearch.statuscollector.database.postgres.entity.Broadcaster;
import de.tsearch.statuscollector.database.postgres.repository.BroadcasterRepository;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Order(30)
public class BroadcastFilter extends OncePerRequestFilter {

    private static final Pattern broadcasterIdPattern = Pattern.compile("/webhook/(\\d+)/?");

    private final BroadcasterRepository broadcasterRepository;

    public BroadcastFilter(BroadcasterRepository broadcasterRepository) {
        this.broadcasterRepository = broadcasterRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {
        final Matcher matcher = broadcasterIdPattern.matcher(httpServletRequest.getServletPath());
        if (matcher.find()) {
            long broadcasterId;
            try {
                broadcasterId = Long.parseLong(matcher.group(1));
            } catch (NumberFormatException e) {
                logger.error("Cannot parse path id " + matcher.group(1) + " as Long");
                httpServletResponse.setStatus(400);
                return;
            }

            final Optional<Broadcaster> broadcasterOptional = broadcasterRepository.findById(broadcasterId);
            if (broadcasterOptional.isPresent()) {
                filterChain.doFilter(httpServletRequest, httpServletResponse);
            } else {
                httpServletResponse.setStatus(400);
            }
        } else {
            logger.warn("Cannot find broadcaster id in path: " + httpServletRequest.getServletPath());
            httpServletResponse.setStatus(400);
        }

    }
}
