# ===============================
# STEP 1: Base image (Java)
# ===============================
FROM eclipse-temurin:17-jdk-alpine

# ===============================
# STEP 2: App working directory
# ===============================
WORKDIR /app

# ===============================
# STEP 3: Copy Maven wrapper & config
# ===============================
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# ===============================
# STEP 4: Maven wrapper executable
# ===============================
RUN chmod +x mvnw

# ===============================
# STEP 5: Download dependencies (cache friendly)
# ===============================
RUN ./mvnw dependency:go-offline

# ===============================
# STEP 6: Copy source code
# ===============================
COPY src src

# ===============================
# STEP 7: Build Spring Boot jar
# ===============================
RUN ./mvnw clean package -DskipTests

# ===============================
# STEP 8: Expose application port
# ===============================
EXPOSE 8080

# ===============================
# STEP 9: Run the application
# ===============================
CMD ["sh", "-c", "java -jar target/*.jar"]
