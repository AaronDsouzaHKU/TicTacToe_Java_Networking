FROM openjdk:17-jre-slim
COPY TicTacToeServer.class /app/
WORKDIR /app
CMD ["java", "TicTacToeServer"]
