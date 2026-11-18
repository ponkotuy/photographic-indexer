# Development image for running `sbt ~warStart` inside docker-compose.dev.yml.
# The source tree is bind-mounted at /app, so only the toolchain lives here.
FROM eclipse-temurin:17-jdk-jammy

ARG SBT_VERSION=2.0.0
ARG UID=1000
ARG GID=1000

RUN apt-get update && \
    apt-get install -y --no-install-recommends exiftool curl && \
    apt-get clean && rm -rf /var/lib/apt/lists/*

# Link to /usr/bin/sbt (same path as a typical host install) so that .bsp/sbt.json,
# which sbt rewrites on startup into the shared source tree, stays valid for the host IDE.
RUN curl -fsSL https://github.com/sbt/sbt/releases/download/v${SBT_VERSION}/sbt-${SBT_VERSION}.tgz | tar xz -C /opt && \
    ln -s /opt/sbt/bin/sbt /usr/bin/sbt

# Run as the host user so files written into the bind-mounted tree are not root-owned.
# The cache/target dirs are pre-created so that named volumes mounted there inherit this ownership.
RUN groupadd -g ${GID} dev && \
    useradd -m -u ${UID} -g ${GID} dev && \
    mkdir -p /home/dev/.cache /home/dev/.sbt /home/dev/.ivy2 /app/target /app/project/target && \
    chown -R dev:dev /home/dev /app

USER dev
WORKDIR /app
