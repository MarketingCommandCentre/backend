package com.ibrasoft.commandcentre;

import com.ibrasoft.commandcentre.service.DiscordMappingService;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Shared setup for full-context tests, so they run on a clean checkout with no secrets and no
 * network access to Discord:
 * <ul>
 *   <li>profile {@code test} loads the fake configuration in {@code application-test.properties};</li>
 *   <li>{@link DiscordMappingService} is mocked, because the real bean logs in to Discord (JDA)
 *       in {@code @PostConstruct} and fails without a valid bot token.</li>
 * </ul>
 * All subclasses share the same configuration and therefore one cached application context.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    @MockitoBean
    protected DiscordMappingService discordMappingService;
}
