package backend.academy.bot.db;

import java.sql.Connection;
import liquibase.Contexts;
import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;

public class LiquibaseMigration {
    public static void migration(Connection connection, String path) throws LiquibaseException {
        Database database =
                DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));

        Liquibase liquibase = new Liquibase(path, new ClassLoaderResourceAccessor(), database);

        liquibase.update(new Contexts());
    }
}
