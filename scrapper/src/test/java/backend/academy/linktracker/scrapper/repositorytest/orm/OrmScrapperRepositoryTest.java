package backend.academy.linktracker.scrapper.repositorytest.orm;

import backend.academy.linktracker.scrapper.repositorytest.ScrapperRepositoryTest;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "app.access-type=ORM")
public class OrmScrapperRepositoryTest extends ScrapperRepositoryTest {}
