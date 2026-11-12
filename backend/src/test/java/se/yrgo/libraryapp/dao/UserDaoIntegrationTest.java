package se.yrgo.libraryapp.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import javax.sql.DataSource;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.radcortez.flyway.test.annotation.H2;

import se.yrgo.libraryapp.entities.LoginInfo;
import se.yrgo.libraryapp.entities.User;
import se.yrgo.libraryapp.entities.UserId;

@Tag("integration")
@H2
public class UserDaoIntegrationTest {
    private static DataSource ds;

    @BeforeAll
    static void initDataSource() {
        // this way we do not need to create a new datasource every time
        final JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:test");
        UserDaoIntegrationTest.ds = ds;
    }

    // get

    @Test
    void getUserById() {
        // this data comes from the test migration files
        final String username = "test";
        final UserId userId = UserId.of(1);

        UserDao userDao = new UserDao(ds);
        Optional<User> maybeUser = userDao.get(Integer.toString(userId.getId()));

        assertThat(maybeUser).isPresent();
        assertThat(maybeUser.get().getName()).isEqualTo(username);
        assertThat(maybeUser.get().getId()).isEqualTo(userId);
    }

    @Test
    void getUserByIdReturnsEmptyWhenUserDoesNotExist() {
        UserDao userDao = new UserDao(ds);

        assertThat(userDao.get("9999")).isEmpty();
    }

    // getLoginInfo

    @Test
    void getLoginInfoForExistingUser() {
        UserDao userDao = new UserDao(ds);

        Optional<LoginInfo> maybeInfo = userDao.getLoginInfo("test");

        assertThat(maybeInfo).isPresent();
        assertThat(maybeInfo.get().getUserId()).isEqualTo(UserId.of(1));
        assertThat(maybeInfo.get().getPasswordHash()).startsWith("$argon2");
    }

    @Test
    void getLoginInfoReturnsEmptyWhenUserDoesNotExist() {
        UserDao userDao = new UserDao(ds);

        assertThat(userDao.getLoginInfo("nobody")).isEmpty();
    }

    // isNameAvailable

    @Test
    void existingNameIsNotAvailable() {
        UserDao userDao = new UserDao(ds);

        assertThat(userDao.isNameAvailable("test")).isFalse();
    }

    @Test
    void unusedNameIsAvailable() {
        UserDao userDao = new UserDao(ds);

        assertThat(userDao.isNameAvailable("brandnewname")).isTrue();
    }

    // register

    @Test
    void registeredUserCanBeFetchedAgain() {
        UserDao userDao = new UserDao(ds);

        boolean registered = userDao.register("newuser", "New User", "somehash");
        Optional<LoginInfo> maybeInfo = userDao.getLoginInfo("newuser");

        assertThat(registered).isTrue();
        assertThat(maybeInfo).isPresent();
        assertThat(maybeInfo.get().getPasswordHash()).isEqualTo("somehash");
    }
}