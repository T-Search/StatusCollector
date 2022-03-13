package de.tsearch.statuscollector.filter;

import de.tsearch.statuscollector.database.postgres.entity.Broadcaster;
import de.tsearch.statuscollector.database.postgres.repository.BroadcasterRepository;
import de.tsearch.statuscollector.filter.entity.RequestMock;
import org.apache.tomcat.util.buf.HexUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Order(40)
public class TwitchMessageSignatureFilter extends OncePerRequestFilter {

    private static final Pattern broadcasterIdPattern = Pattern.compile("/webhook/(\\d+)/?");

    private final BroadcasterRepository broadcasterRepository;

    public TwitchMessageSignatureFilter(BroadcasterRepository broadcasterRepository) throws NoSuchAlgorithmException {
        this.broadcasterRepository = broadcasterRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {
        final Matcher matcher = broadcasterIdPattern.matcher(httpServletRequest.getServletPath());

        /*                                     @RequestHeader("Twitch-Eventsub-Message-Type") String messageType,
                                     @RequestHeader("Twitch-Eventsub-Message-Signature") String signature,
                                     @RequestHeader("Twitch-Eventsub-Message-Id") String messageId,
                                     @RequestHeader("Twitch-Eventsub-Message-Timestamp") String timestamp,

         */
        RequestMock requestMock = new RequestMock(httpServletRequest);
        final String messageId = httpServletRequest.getHeader("Twitch-Eventsub-Message-Id");
        final String timestamp = httpServletRequest.getHeader("Twitch-Eventsub-Message-Timestamp");
        final String signature = httpServletRequest.getHeader("Twitch-Eventsub-Message-Signature");
        final String content = requestMock.getByteArrayOutputStream().toString(StandardCharsets.UTF_8);


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
                final Broadcaster broadcaster = broadcasterOptional.get();

                //Signature check!
                try {
                    Mac mac = Mac.getInstance("HmacSHA256");
                    SecretKeySpec secretKeySpec = new SecretKeySpec(broadcaster.getTwitchWebhookSecret().toString().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
                    mac.init(secretKeySpec);
                    String hexString = "sha256=" + HexUtils.toHexString(mac.doFinal((messageId + timestamp + content).getBytes(StandardCharsets.UTF_8)));
                    if (!signature.equals(hexString)) {
                        logger.warn("Invalid webhook notification received!");
                        httpServletResponse.setStatus(400);
                        return;
                    }
                } catch (NoSuchAlgorithmException | InvalidKeyException e) {
                    e.printStackTrace();
                    httpServletResponse.setStatus(500);
                    return;
                }


                filterChain.doFilter(requestMock, httpServletResponse);
            } else {
                httpServletResponse.setStatus(400);
            }
        } else {
            logger.warn("Cannot find broadcaster id in path: " + httpServletRequest.getServletPath());
            httpServletResponse.setStatus(400);
        }
    }
}
