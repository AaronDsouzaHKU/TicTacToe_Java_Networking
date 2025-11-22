# Multi-stage build to keep it slim
FROM openjdk:17-jdk-slim AS builder

WORKDIR /app
COPY TicTacToeServer.java .
RUN javac TicTacToeServer.java

# Runtime stage
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=builder /app/TicTacToeServer.class .
EXPOSE 5000
CMD ["java", "TicTacToeServer"]
