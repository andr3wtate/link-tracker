package backend.academy.linktracker.bot.repositorytest.orm;

import backend.academy.linktracker.bot.repositorytest.BotRepositoryTest;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "app.access-type=ORM")
public class OrmBotRepositoryTest extends BotRepositoryTest {}
