# Snowfalke Application Server Realm
_The security foundation for the Snowflake Application Server_

A tomcat realm allows to configure various authentication methods and groups. 
This SnowflakeRealm is using the Snowflake database to authenticate, meaning the user is requested in the browser to enter his Snowflake login and password
and if valid, the list of assigned Snowflake groups is read. Thus tomcat can utilize these group names in the standard way to implement role level
security.

## Installation

Copy the [jar](https://github.com/rtdi/SnowflakeRealm/releases) file into tomcat's lib folder so that it can be used in the server configuration.
Also copy the Snowflake JDBC driver and the [base jar file for all AppContainerRealms](https://github.com/rtdi/AppContainerRealm/releases).

In the server.xml of the tomcat, the realm is configured, with the parameter JDBCURL as the only property to be specified.

		<Server>
		  <Service>
		    <Engine>
		      ...
		      <Realm className="org.apache.catalina.realm.LockOutRealm">
		        <Realm className="io.rtdi.appcontainer.snowflakerealm.SnowflakeRealm" JDBCURL="jdbc:..."/>
		      </Realm>
		      ...
		    </Engine>
		  </Service>
		</Server>

Alternatively, the environment variable JDBCURL can be set as well. This is especially useful when building docker images. 