package com.ibrasoft.commandcentre.security;

import com.ibrasoft.commandcentre.AbstractIntegrationTest;
import com.ibrasoft.commandcentre.model.Request;
import com.ibrasoft.commandcentre.model.RequestStatus;
import com.ibrasoft.commandcentre.model.RequestType;
import com.ibrasoft.commandcentre.model.Role;
import com.ibrasoft.commandcentre.repository.AuditEventRepository;
import com.ibrasoft.commandcentre.repository.RequestRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end checks of the role rules in {@code SecurityConfig}: real filter chain, tokens minted
 * by the real {@link JwtService}.
 */
class AuthorizationRulesTest extends AbstractIntegrationTest {

    /** Request that ROLE_USER write attempts target; seeded directly through the repository. */
    private static final long SEEDED_CHANNEL = 7_001L;
    /** Channel ROLE_USER tries (and must fail) to create. */
    private static final long USER_CREATE_CHANNEL = 7_002L;
    /** Channel used by the ROLE_BOT create/update/status/delete round trip. */
    private static final long BOT_ROUND_TRIP_CHANNEL = 7_100L;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private AuditEventRepository auditEventRepository;

    @Value("${security.jwt.secret}")
    private String jwtSecret;

    @Value("${security.jwt.issuer}")
    private String jwtIssuer;

    // ---------------------------------------------------------------- no token

    @Test
    void noTokenIsNotServedDataAndIsSentToTheDiscordLogin() throws Exception {
        for (String path : List.of("/api/workload/cycle-info", "/api/requests")) {
            mvc.perform(get(path))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", org.hamcrest.Matchers.endsWith("/oauth2/authorization/discord")));
        }
    }

    // ---------------------------------------------------------------- ROLE_USER

    @Test
    void userCanReadDataEndpoints() throws Exception {
        String token = userToken();
        for (String path : List.of("/api/requests", "/api/audit-events", "/api/workload/cycle-info")) {
            mvc.perform(get(path).header("Authorization", bearer(token)))
                .andExpect(status().isOk());
        }
    }

    static Stream<Arguments> writesForbiddenToUsers() {
        String channel = "/api/requests/channel/" + SEEDED_CHANNEL;
        return Stream.of(
            Arguments.of("POST /api/requests",
                post("/api/requests").contentType(MediaType.APPLICATION_JSON).content(requestJson(USER_CREATE_CHANNEL, "user create"))),
            Arguments.of("PUT " + channel,
                put(channel).contentType(MediaType.APPLICATION_JSON).content(requestJson(SEEDED_CHANNEL, "user edit"))),
            Arguments.of("DELETE " + channel, delete(channel)),
            Arguments.of("PATCH assign", patch(channel + "/assign/555")),
            Arguments.of("PATCH status", patch(channel + "/status/DONE")),
            Arguments.of("PATCH advance", patch(channel + "/advance")),
            Arguments.of("PATCH department", patch(channel + "/department/777")),
            Arguments.of("PATCH requester", patch(channel + "/requester/888")),
            Arguments.of("POST /api/audit-events",
                post("/api/audit-events").contentType(MediaType.APPLICATION_JSON).content(auditJson(SEEDED_CHANNEL)))
        );
    }

    @ParameterizedTest(name = "ROLE_USER {0} -> 403")
    @MethodSource("writesForbiddenToUsers")
    void userCannotWrite(String label, MockHttpServletRequestBuilder write) throws Exception {
        seedRequest();
        long auditEventsBefore = auditEventRepository.count();

        mvc.perform(write.header("Authorization", bearer(userToken())))
            .andExpect(status().isForbidden());

        Request stored = requestRepository.findById(SEEDED_CHANNEL).orElseThrow();
        assertThat(stored.getTitle()).isEqualTo("seeded");
        assertThat(stored.getStatus()).isEqualTo(RequestStatus.IN_QUEUE);
        assertThat(stored.getAssignedToID()).isNull();
        assertThat(stored.getRequesterID()).isEqualTo(42L);
        assertThat(stored.getRequesterDepartmentID()).isNull();
        assertThat(requestRepository.existsById(USER_CREATE_CHANNEL)).isFalse();
        assertThat(auditEventRepository.count()).isEqualTo(auditEventsBefore);
    }

    @Test
    void userCanUseTheBulkLookups() throws Exception {
        given(discordMappingService.getNicknames(anyList())).willReturn(Map.of("1", "Alice"));
        given(discordMappingService.getRoleNames(anyList())).willReturn(Map.of("2", "Designers"));
        String token = userToken();

        mvc.perform(post("/api/discord/users/bulk").header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON).content("[\"1\"]"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.1").value("Alice"));
        mvc.perform(post("/api/discord/roles/bulk").header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON).content("[\"2\"]"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.2").value("Designers"));
    }

    // ---------------------------------------------------------------- ROLE_BOT

    @Test
    void botCanCreateUpdateChangeStatusAndDeleteARequest() throws Exception {
        String token = botToken();
        String path = "/api/requests/channel/" + BOT_ROUND_TRIP_CHANNEL;

        mvc.perform(post("/api/requests").header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON).content(requestJson(BOT_ROUND_TRIP_CHANNEL, "bot create")))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.title").value("bot create"));

        mvc.perform(put(path).header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON).content(requestJson(BOT_ROUND_TRIP_CHANNEL, "bot update")))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("bot update"));

        mvc.perform(patch(path + "/status/IN_PROGRESS").header("Authorization", bearer(token)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        mvc.perform(delete(path).header("Authorization", bearer(token)))
            .andExpect(status().isNoContent());

        assertThat(requestRepository.existsById(BOT_ROUND_TRIP_CHANNEL)).isFalse();
    }

    @Test
    void botCanWriteAuditEvents() throws Exception {
        mvc.perform(post("/api/audit-events").header("Authorization", bearer(botToken()))
                .contentType(MediaType.APPLICATION_JSON).content(auditJson(SEEDED_CHANNEL)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.eventType").value("CHANNEL_RENAME"));
    }

    // ---------------------------------------------------------------- tokens that must not work

    @Test
    void tokenWithEmptyRolesGetsNoAccess() throws Exception {
        assertDenied(jwtService.generateUserToken("123", List.of()));
    }

    @Test
    void tokenWithMissingRolesClaimGetsNoAccess() throws Exception {
        assertDenied(signedToken(jwtSecret, null));
    }

    @Test
    void tokenWithOnlyAnUnknownRoleGetsNoAccess() throws Exception {
        assertDenied(signedToken(jwtSecret, List.of("ROLE_ADMIN")));
    }

    @Test
    void unknownRolesAreIgnoredAlongsideAKnownOne() throws Exception {
        String token = signedToken(jwtSecret, List.of("ROLE_ADMIN", "ROLE_USER"));
        mvc.perform(get("/api/requests").header("Authorization", bearer(token)))
            .andExpect(status().isOk());
        mvc.perform(post("/api/audit-events").header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON).content(auditJson(SEEDED_CHANNEL)))
            .andExpect(status().isForbidden());
    }

    @Test
    void tokenSignedWithAnotherSecretGetsNoAccess() throws Exception {
        String otherSecret = java.util.Base64.getEncoder()
            .encodeToString("a-different-test-only-secret-0123456789abcdef".getBytes());
        assertDenied(signedToken(otherSecret, List.of("ROLE_BOT")));
    }

    // ---------------------------------------------------------------- OAuth2 browser session

    @Test
    void oauthSessionPrincipalIsForbiddenFromApiData() throws Exception {
        mvc.perform(get("/api/requests")
                .with(oauth2Login().attributes(a -> a.put("id", "123456789")))
                // Pretend the guild check already passed, so a 403 can only come from the role rules.
                .sessionAttr("DISCORD_GUILD_VERIFIED", Boolean.TRUE))
            .andExpect(status().isForbidden())
            .andExpect(result -> assertThat(result.getResponse().getContentAsString()).doesNotContain("NOT_IN_GUILD"));
    }

    @Test
    void oauthSessionPrincipalStillReachesAuthEndpoints() throws Exception {
        mvc.perform(get("/api/auth/user")
                // MockMvc leaves the servlet path empty unless told; a real container sets it, and
                // DiscordGuildFilter relies on it to skip /api/auth/**.
                .servletPath("/api/auth/user")
                .with(oauth2Login().attributes(a -> {
                    a.put("id", "123456789");
                    a.put("username", "someone");
                })))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("123456789"));
    }

    // ---------------------------------------------------------------- helpers

    /** Denied = never served: no 2xx on a read endpoint, the bulk lookup, or a write. */
    private void assertDenied(String token) throws Exception {
        List<MockHttpServletRequestBuilder> attempts = List.of(
            get("/api/requests"),
            get("/api/workload/cycle-info"),
            post("/api/discord/users/bulk").contentType(MediaType.APPLICATION_JSON).content("[\"1\"]"),
            post("/api/audit-events").contentType(MediaType.APPLICATION_JSON).content(auditJson(SEEDED_CHANNEL))
        );
        for (MockHttpServletRequestBuilder attempt : attempts) {
            int status = mvc.perform(attempt.header("Authorization", bearer(token)))
                .andReturn().getResponse().getStatus();
            // 302 = treated as unauthenticated (sent to the Discord login), like no token at all.
            assertThat(status).as("status for a token that must grant nothing").isIn(302, 401, 403);
        }
    }

    private void seedRequest() {
        Request seeded = new Request();
        seeded.setChannelID(SEEDED_CHANNEL);
        seeded.setMainMessageID(1L);
        seeded.setRequesterID(42L);
        seeded.setTitle("seeded");
        seeded.setRequestType(RequestType.POST);
        seeded.setStatus(RequestStatus.IN_QUEUE);
        requestRepository.save(seeded);
    }

    private String userToken() {
        return jwtService.generateUserToken("123456789", List.of(Role.ROLE_USER));
    }

    private String botToken() {
        return jwtService.generateBotToken("discord-bot", List.of(Role.ROLE_BOT));
    }

    /** Hand-built token for shapes {@link JwtService} cannot produce (unknown roles, no claim, other key). */
    private String signedToken(String base64Secret, List<String> roles) {
        Instant now = Instant.now();
        var builder = Jwts.builder()
            .setIssuer(jwtIssuer)
            .setSubject("123456789")
            .setIssuedAt(Date.from(now))
            .setExpiration(Date.from(now.plus(1, ChronoUnit.HOURS)));
        if (roles != null) {
            builder.claim("roles", roles);
        }
        return builder
            .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(base64Secret)), SignatureAlgorithm.HS256)
            .compact();
    }

    private static String bearer(String token) {
        return "Bearer " + token;
    }

    private static String requestJson(long channelId, String title) {
        return """
            {"channelID": %d, "mainMessageID": 1, "requesterID": 42, "title": "%s",
             "requestType": "POST", "status": "IN_QUEUE"}
            """.formatted(channelId, title);
    }

    private static String auditJson(long entityId) {
        return """
            {"eventType": "CHANNEL_RENAME", "entityType": "Request", "entityId": %d, "eventDetails": "test"}
            """.formatted(entityId);
    }
}
