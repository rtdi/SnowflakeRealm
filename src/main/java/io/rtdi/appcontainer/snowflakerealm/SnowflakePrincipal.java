package io.rtdi.appcontainer.snowflakerealm;

import java.security.Principal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.security.auth.login.LoginContext;

import org.apache.catalina.realm.GenericPrincipal;
import org.ietf.jgss.GSSCredential;

import io.rtdi.appcontainer.realm.IAppContainerPrincipal;

/**
 * The generic principal enriched with some additional information, e.g. the exact username (uppercase?) and the Snowflake version.
 *
 */
public class SnowflakePrincipal extends GenericPrincipal implements IAppContainerPrincipal {

	private static final long serialVersionUID = 465826394352656292L;
	private String jdbcurl;
	private String version;
	private String user;
	private String password;

	public SnowflakePrincipal(String name, String password, String hanajdbcurl) throws SQLException {
		super(name, password, queryRoles(name, password, hanajdbcurl));
		this.jdbcurl = hanajdbcurl;
		this.password = password;
		setSupportData(name, password, hanajdbcurl);
	}

	public SnowflakePrincipal(String name, String password, String hanajdbcurl, Principal userPrincipal) throws SQLException {
		super(name, password, queryRoles(name, password, hanajdbcurl), userPrincipal);
		this.jdbcurl = hanajdbcurl;
		this.password = password;
		setSupportData(name, password, hanajdbcurl);
	}

	public SnowflakePrincipal(String name, String password, String hanajdbcurl, Principal userPrincipal,
			LoginContext loginContext) throws SQLException {
		super(name, password, queryRoles(name, password, hanajdbcurl), userPrincipal, loginContext);
		this.jdbcurl = hanajdbcurl;
		this.password = password;
		setSupportData(name, password, hanajdbcurl);
	}

	public SnowflakePrincipal(String name, String password, String hanajdbcurl, Principal userPrincipal,
			LoginContext loginContext, GSSCredential gssCredential) throws SQLException {
		super(name, password, queryRoles(name, password, hanajdbcurl), userPrincipal, loginContext, gssCredential);
		this.jdbcurl = hanajdbcurl;
		this.password = password;
		setSupportData(name, password, hanajdbcurl);
	}

	private void setSupportData(String name, String password, String hanajdbcurl) throws SQLException {
		try (Connection c = getDatabaseConnection(name, password, hanajdbcurl)) {
			try (PreparedStatement stmt = c.prepareStatement("select current_version(), current_user()"); ) {
				ResultSet rs = stmt.executeQuery();
				if (rs.next()) {
					this.version = rs.getString(1);
					this.user = rs.getString(2);
				}
			}
		}		
	}
	/**
	 * @param name Snowflake user name
	 * @param password Snowflake password
	 * @param jdbcurl Snowflake JDBC connection URL
	 * @return the list of Snowflake role names the user has assigned, direct or indirect
	 * @throws SQLException in case the roles cannot be read
	 */
	public static List<String> queryRoles(String name, String password, String jdbcurl) throws SQLException {
		try (Connection c = getDatabaseConnection(name, password, jdbcurl)) {
			try (PreparedStatement stmt = c.prepareStatement("select role_name from UTIL_DB.INFORMATION_SCHEMA.APPLICABLE_ROLES"); ) {
				List<String> roles = new ArrayList<String>();
				ResultSet rs = stmt.executeQuery();
				while (rs.next()) {
					roles.add(rs.getString(1));
				}
				return roles;
			}
		}
	}
	
	/**
	 * @return the database connection JDBC URL used
	 */
	@Override
	public String getJDBCURL() {
		return jdbcurl;
	}
	
	@Override
	public Connection createNewConnection() throws SQLException {
		return getDatabaseConnection(super.getName(), password, jdbcurl);
	}
	
	static Connection getDatabaseConnection(String user, String passwd, String jdbcurl) throws SQLException {
        try {
            Class.forName("net.snowflake.client.jdbc.SnowflakeDriver");
            return DriverManager.getConnection(jdbcurl, user, passwd);
        } catch (ClassNotFoundException e) {
            throw new SQLException("No Snowflake JDBC driver library found");
        }
	}
	
	@Override
	public String getDriverURL() {
		return "net.snowflake.client.jdbc.SnowflakeDriver";
	}

	/**
	 * @return the version string of the connected Hana database as retrieved at login
	 */
	@Override
	public String getDBVersion() {
		return version;
	}

	/**
	 * @return the exact Hana user, e.g. the loginuser might by user1 but the actual database user name is "USER1"
	 */
	@Override
	public String getDBUser() {
		return user;
	}

	@Override
	public String getPassword() {
		return password;
	}

}
