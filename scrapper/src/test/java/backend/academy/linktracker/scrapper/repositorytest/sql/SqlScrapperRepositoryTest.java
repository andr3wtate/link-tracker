package backend.academy.linktracker.scrapper.repositorytest.sql;

import backend.academy.linktracker.scrapper.repositorytest.ScrapperRepositoryTest;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "app.access-type=SQL")
public class SqlScrapperRepositoryTest extends ScrapperRepositoryTest {}
