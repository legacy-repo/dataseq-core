###################
# STAGE 1: builder
###################

# Build currently doesn't work on > Java 11 (i18n utils are busted) so build on 8 until we fix this
FROM adoptopenjdk/openjdk8:latest as builder

WORKDIR /app/source

ENV FC_LANG en-US
ENV LC_CTYPE en_US.UTF-8

COPY sources.list /etc/apt/sources.list

# bash:    various shell scripts
# wget:    installing lein
# git:     ./bin/version
# make:    backend building
# gettext: translations
RUN apt-get update && apt-get install -y coreutils bash git wget make gettext

# lein:    backend dependencies and building
ADD ./bin/lein /usr/local/bin/lein
RUN chmod 744 /usr/local/bin/lein
RUN lein upgrade

# install dependencies before adding the rest of the source to maximize caching

# backend dependencies
ADD project.clj .
RUN lein deps

# add the rest of the source
ADD . .

# build the app
RUN bin/build

# ###################
# # STAGE 2: runner
# ###################

FROM adoptopenjdk/openjdk11:jre as runner

WORKDIR /app

ENV FC_LANG en-US
ENV LC_CTYPE en_US.UTF-8

# dependencies
COPY sources.list /etc/apt/sources.list
RUN echo "**** Install dev packages ****" && \
    apt-get update && \
    apt-get install -y bash wget git curl libxml2-dev libcurl4-openssl-dev libssl-dev && \
    echo "**** Cleanup ****" && \
    apt-get clean

# add dataseq-core script and uberjar
RUN mkdir -p bin target/uberjar
COPY --from=builder /app/source/target/uberjar/dataseq-core.jar /app/target/uberjar/
COPY --from=builder /app/source/bin /app/bin/

# expose our default runtime port
EXPOSE 3000

# run it
ENTRYPOINT ["/app/bin/start"]
