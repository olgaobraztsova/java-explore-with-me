FROM amazoncorretto:11
ENV JAVA_TOOL_OPTIONS -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:9091
COPY target/*.jar stats-service.jar
ENTRYPOINT ["java","-jar","/stats-service.jar"]