package backend.academy.linktracker.scrapper.repositorytest.orm;

import backend.academy.linktracker.scrapper.repositorytest.StringLongCacheRepositoryTest;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "app.access-type=ORM")
public class OrmStringLongCacheRepositoryTest extends StringLongCacheRepositoryTest {}
