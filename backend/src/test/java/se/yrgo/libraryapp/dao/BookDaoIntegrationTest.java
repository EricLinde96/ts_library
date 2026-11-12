package se.yrgo.libraryapp.dao;

import static org.assertj.core.api.Assertions.assertThat;

import javax.sql.DataSource;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.radcortez.flyway.test.annotation.H2;

import se.yrgo.libraryapp.entities.BookId;
import se.yrgo.libraryapp.entities.UserId;

@Tag("integration")
@H2
public class BookDaoIntegrationTest {
    private static DataSource ds;

    @BeforeAll
    static void initDataSource() {
        final JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:test");
        BookDaoIntegrationTest.ds = ds;
    }

    @Test
    void existingBookIsFound() {
        BookDao dao = new BookDao(ds);

        assertThat(dao.get(BookId.of(1))).isPresent();
    }

    @Test
    void unknownBookIsNotFound() {
        BookDao dao = new BookDao(ds);

        assertThat(dao.get(BookId.of(9999))).isEmpty();
    }

    @Test
    void findWithBlankInputReturnsEmpty() {
        BookDao dao = new BookDao(ds);

        assertThat(dao.find(null, " ", "")).isEmpty();
    }

    @Test
    @Disabled("lend() uses MySQL's DATE_ADD, which H2  does not seem have.  It needs a real MySQL database to test.")
    void lendCreatesLoan() {
        BookDao dao = new BookDao(ds);

        assertThat(dao.lend(BookId.of(1), UserId.of(1))).isTrue();
    }
}