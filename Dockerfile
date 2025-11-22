# Multi-stage build to keep it slim (compile in first stage, run in second)
FROM openjdk:17-jdk-slim AS builder

WORKDIR /app
COPY TicTacToeServer.java .
RUN javac TicTacToeServer.java

# Runtime stage (uses the same slim JDK — it's fine for running)
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=builder /app/TicTacToeServer.class .
EXPOSE 5000
CMD ["java", "TicTacToeServer"]
