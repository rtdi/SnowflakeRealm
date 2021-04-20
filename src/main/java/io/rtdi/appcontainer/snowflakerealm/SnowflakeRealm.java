package io.rtdi.appcontainer.snowflakerealm;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.apache.catalina.realm.RealmBase;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

/**
 * The Snowflake Realm is an authentication method for tomcat to use the Snowflake database as authenticator.
 * In addition to that, the Principal returned by this Realm has all the Snowflake roles the user has assigned to as well.
 * 
 * There are two ways to set the jdbcurl
 * 1. In the server.xml as property &lt;Realm className="io.rtdi.appcontainer.snowflakerealm.SnowflakeRealm" 
 *    JDBCURL="jdbc:snowflake://&lt;account_name&gt;.snowflakecomputing.com/?&lt;connection_params&gt;"/&gt;
 * 2. As environment variable JDBCURL
 *
 */
public class SnowflakeRealm extends RealmBase {
    private static final Log log = LogFactory.getLog(SnowflakeRealm.class);
    private String jdbcurl;
    private Map<String, SnowflakePrincipal> userdirectory = new HashMap<>();

	public SnowflakeRealm() {
	}

	@Override
	public SnowflakePrincipal authenticate(String username, String credentials) {
		if (jdbcurl == null) {
			jdbcurl = System.getenv("JDBCURL");
			if (jdbcurl == null) {
				log.debug("No jdbc-url configured, neither as property in the server.xml nor as environment variable JDBCURL");
				return null;
			}
		}
		log.debug("Authenticating user \"" + username + "\" with database \"" + jdbcurl + "\"");
		try {
			SnowflakePrincipal principal = userdirectory.get(username);
			if (principal == null ) { 
				principal = new SnowflakePrincipal(username, credentials, jdbcurl); // this does throw a SQLException in case the login data is invalid
				userdirectory.put(username, principal);
			}
			return principal;
		} catch (SQLException e) {
			log.debug("failed to login with the provided credentials for \"" + username + "\" with database \"" + jdbcurl + "\" and exception " + e.getMessage());
			return null;
		}
	}

	/**
	 * Actually returns null for security reasons
	 */
	@Override
	protected String getPassword(String username) {
		return null; // Do not expose the password. What is the side effect of that with md5 digest???
	}

	/**
	 * Get the SnowflakePrincipal associated with the specified user
	 * @return SnowflakePrincipal
	 */
	@Override
	protected SnowflakePrincipal getPrincipal(String username) {
		return userdirectory.get(username);
	}
	
	/**
	 * @return JDBC URL of the used database
	 */
	public String getJDBCURL() {
		return jdbcurl;
	}

	/**
	 * @param jdbcurl the JDBC URL to be used
	 */
	public void setJDBCURL(String jdbcurl) {
		this.jdbcurl = jdbcurl;
	}
}
