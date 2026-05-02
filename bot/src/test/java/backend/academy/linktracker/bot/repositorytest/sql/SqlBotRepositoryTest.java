package backend.academy.linktracker.bot.repositorytest.sql;

import backend.academy.linktracker.bot.repositorytest.BotRepositoryTest;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "app.access-type=SQL")
public class SqlBotRepositoryTest extends BotRepositoryTest {}
