# Flyway with MySQL Connector/J baked in. The official image bundles only the MariaDB driver
# (Connector/J is GPL), which would otherwise silently handle jdbc:mysql URLs.
FROM flyway/flyway:13.12.2

# When bumping the version, update the checksum too (sha256 of the jar on Maven Central).
# Keep in sync with mysql-connector-j in build.sbt.
ARG MYSQL_CONNECTOR_VERSION=26.10.0
ADD --checksum=sha256:cef70caf532833c9ab8f41f74773ce22c08bf503fad215c9c2ee1e9db87e759e \
    https://repo1.maven.org/maven2/com/mysql/mysql-connector-j/${MYSQL_CONNECTOR_VERSION}/mysql-connector-j-${MYSQL_CONNECTOR_VERSION}.jar \
    /flyway/mysql-driver/

# Not under drivers/, which holds the drivers bundled for Flyway's other database plugins.
ENV FLYWAY_JAR_DIRS=/flyway/mysql-driver
