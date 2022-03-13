package de.tsearch.statuscollector.filter;

import de.tsearch.statuscollector.database.postgres.entity.TwitchMessageId;
import de.tsearch.statuscollector.database.postgres.repository.TwitchMessageIdRepository;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@Component
@Order(20)
public class TwitchMessageIdFilter extends OncePerRequestFilter {

    //TODO Resending messages has same id!

    private final TwitchMessageIdRepository twitchMessageIdRepository;

    public TwitchMessageIdFilter(TwitchMessageIdRepository twitchMessageIdRepository) {
        this.twitchMessageIdRepository = twitchMessageIdRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {
        final String messageId = httpServletRequest.getHeader("Twitch-Eventsub-Message-Id");

        final Optional<TwitchMessageId> twitchMessageIdOptional = twitchMessageIdRepository.findById(messageId);

        if (twitchMessageIdOptional.isPresent()) {
            logger.warn("Twitch Message Id is not unique! Reject request!");
            httpServletResponse.setStatus(400);
        } else {
            twitchMessageIdRepository.save(new TwitchMessageId(messageId));
            filterChain.doFilter(httpServletRequest, httpServletResponse);
        }
    }
}
