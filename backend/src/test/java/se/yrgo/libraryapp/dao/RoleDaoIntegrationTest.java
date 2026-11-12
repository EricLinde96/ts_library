package se.yrgo.libraryapp.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import javax.sql.DataSource;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.radcortez.flyway.test.annotation.H2;

import se.yrgo.libraryapp.entities.Role;
import se.yrgo.libraryapp.entities.UserId;

@Tag("integration")
@H2
public class RoleDaoIntegrationTest {
    private static DataSource ds;

    @BeforeAll
    static void initDataSource() {
        final JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:test");
        RoleDaoIntegrationTest.ds = ds;
    }

    @Test
    void adminUserHasAdminRole() {
        RoleDao roleDao = new RoleDao(ds);

        List<Role> roles = roleDao.get(UserId.of(1));

        assertThat(roles).containsExactly(Role.ADMIN);
    }

    @Test
    void userWithTwoRolesGetsBoth() {
        RoleDao roleDao = new RoleDao(ds);

        List<Role> roles = roleDao.get(UserId.of(2));

        assertThat(roles).containsExactlyInAnyOrder(Role.ADMIN, Role.USER);
    }

    @Test
    void unknownUserHasNoRoles() {
        RoleDao roleDao = new RoleDao(ds);

        assertThat(roleDao.get(UserId.of(9999))).isEmpty();
    }
}