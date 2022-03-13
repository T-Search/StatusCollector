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

    private final TwitchMessageIdRepository twitchMessageIdRepository;

    public TwitchMessageIdFilter(TwitchMessageIdRepository twitchMessageIdRepository) {
        this.twitchMessageIdRepository = twitchMessageIdRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {
        final String messageId = httpServletRequest.getHeader("Twitch-Eventsub-Message-Id");
        final String trieString = httpServletRequest.getHeader("Twitch-Eventsub-Message-Retry");

        byte trie;
        try {
            trie = Byte.parseByte(trieString);
        } catch (NumberFormatException e) {
            logger.warn("Cannot parse header Twitch-Eventsub-Message-Retry number");
            httpServletResponse.setStatus(400);
            return;
        }

        final Optional<TwitchMessageId> twitchMessageIdOptional = twitchMessageIdRepository.findById(messageId);

        if (twitchMessageIdOptional.isPresent()) {
            TwitchMessageId twitchMessageId = twitchMessageIdOptional.get();
            if (trie > twitchMessageId.getTries()) {
                //Ist okay
                twitchMessageId.setTries(trie);
                twitchMessageIdRepository.save(twitchMessageId);
                filterChain.doFilter(httpServletRequest, httpServletResponse);
            } else {
                logger.warn("Twitch Message Id is not unique and tries not match! Reject request!");
                httpServletResponse.setStatus(400);
            }
        } else {
            twitchMessageIdRepository.save(new TwitchMessageId(messageId, trie));
            filterChain.doFilter(httpServletRequest, httpServletResponse);
        }
    }
}
