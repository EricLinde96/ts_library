package se.yrgo.libraryapp.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import javax.sql.DataSource;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.exception.CredentialsException;

import com.radcortez.flyway.test.annotation.H2;

import se.yrgo.libraryapp.entities.UserId;

@Tag("integration")
@H2
public class SessionDaoIntegrationTest {
    private static DataSource ds;

    @BeforeAll
    static void initDataSource() {
        final JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:test");
        SessionDaoIntegrationTest.ds = ds;
    }

    @Test
    void createdSessionCanBeValidated() {
        SessionDao sessionDao = new SessionDao(ds);

        UUID session = sessionDao.create(UserId.of(1));

        assertThat(sessionDao.validate(session)).isEqualTo(UserId.of(1));
    }

    @Test
    void unknownSessionIsRejected() {
        SessionDao sessionDao = new SessionDao(ds);

        assertThatThrownBy(() -> sessionDao.validate(UUID.randomUUID()))
                .isInstanceOf(CredentialsException.class);
    }

    @Test
    void deletedSessionIsRejected() {
        SessionDao sessionDao = new SessionDao(ds);
        UUID session = sessionDao.create(UserId.of(1));

        sessionDao.delete(session);

        assertThatThrownBy(() -> sessionDao.validate(session))
                .isInstanceOf(CredentialsException.class);
    }

    @Test
    void expiredSessionIsRejected() throws SQLException {
        SessionDao sessionDao = new SessionDao(ds);
        UUID session = UUID.randomUUID();

        // insert a session that was created 8 days ago (limit is 7 days)
        try (Connection conn = ds.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("INSERT INTO session VALUES ('" + session
                    + "', 1, TIMESTAMPADD(DAY, -8, CURRENT_TIMESTAMP))");
        }

        assertThatThrownBy(() -> sessionDao.validate(session))
                .isInstanceOf(CredentialsException.class);
    }
}