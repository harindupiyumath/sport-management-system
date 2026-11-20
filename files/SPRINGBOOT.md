# SPRINGBOOT.md – Spring Boot Setup Guide

Everything needed to create, configure and run the backend of the Sport Management System.

---

## 1. Prerequisites

- JDK 25
- Maven 3.9+ (or use the Maven wrapper `mvnw`)
- MySQL 8 (+ MySQL Workbench or phpMyAdmin)
- IDE: IntelliJ IDEA / VS Code with Java extensions
- Postman (to test the API)

---

## 2. Create the Project

Go to **https://start.spring.io** and choose:

| Field | Value |
|---|---|
| Project | Maven |
| Language | Java |
| Spring Boot | latest stable 3.x |
| Group | `com.wayamba` |
| Artifact | `sportms` |
| Packaging | Jar |
| Java | 25 |

Dependencies: **Spring Web, Spring Data JPA, MySQL Driver, Spring Security, Validation, Lombok (optional)**.

Add JWT libraries to `pom.xml`:

```xml
<dependency>
  <groupId>io.jsonwebtoken</groupId>
  <artifactId>jjwt-api</artifactId>
  <version>0.12.5</version>
</dependency>
<dependency>
  <groupId>io.jsonwebtoken</groupId>
  <artifactId>jjwt-impl</artifactId>
  <version>0.12.5</version>
  <scope>runtime</scope>
</dependency>
<dependency>
  <groupId>io.jsonwebtoken</groupId>
  <artifactId>jjwt-jackson</artifactId>
  <version>0.12.5</version>
  <scope>runtime</scope>
</dependency>
```

---

## 3. Database

```sql
CREATE DATABASE sport_ms CHARACTER SET utf8mb4;
```

Tables are created automatically by JPA (`ddl-auto=update`).

---

## 4. `src/main/resources/application.properties`

```properties
spring.application.name=sportms
server.port=8080

# MySQL
spring.datasource.url=jdbc:mysql://localhost:3306/sport_ms?useSSL=false&serverTimezone=Asia/Colombo
spring.datasource.username=root
spring.datasource.password=YOUR_PASSWORD

# JPA / Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect

# JWT
app.jwt.secret=CHANGE_THIS_TO_A_LONG_RANDOM_STRING_AT_LEAST_32_CHARS
app.jwt.expiration-ms=86400000

# Frontend origin (Live Server)
app.cors.allowed-origin=http://127.0.0.1:5500
```

Do not commit your real DB password to a public repo.

---

## 5. Main Class

```java
@SpringBootApplication
public class SportManagementApplication {
    public static void main(String[] args) {
        SpringApplication.run(SportManagementApplication.class, args);
    }
}
```

---

## 6. Layers (Spring annotations)

| Layer | Annotation | Example |
|---|---|---|
| Entity | `@Entity`, `@Table` | `Booking` |
| Repository | `extends JpaRepository<T, ID>` | `BookingRepository` |
| Service | `@Service` | `BookingService` |
| Controller | `@RestController`, `@RequestMapping("/api/...")` | `BookingController` |
| DTO validation | `@Valid`, `@NotBlank`, `@Pattern` | `SignupRequest` |
| Errors | `@RestControllerAdvice` | `GlobalExceptionHandler` |

Example repository:

```java
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByUserIdOrderByBookingDateDesc(String userId);
    List<Booking> findByStatus(BookingStatus status);
    List<Booking> findBySportIdAndBookingDate(Long sportId, LocalDate date);
    boolean existsBySportIdAndBookingDateAndStartTimeAndStatus(
            Long sportId, LocalDate date, LocalTime start, BookingStatus status);
}
```

Example controller:

```java
@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingResponse> create(@Valid @RequestBody BookingRequest req,
                                                  Authentication auth) {
        return ResponseEntity.status(201)
                .body(bookingService.create(auth.getName(), req));   // user id from JWT
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(@PathVariable Long id, Authentication auth) {
        bookingService.cancel(auth.getName(), id);
        return ResponseEntity.noContent().build();
    }
}
```

---

## 7. Security Configuration (outline)

```java
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http, JwtFilter jwtFilter) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated())
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
}
```

`JwtFilter` reads the `Authorization: Bearer <token>` header, validates it with `JwtUtil`, and sets the authentication (userId + role) in the `SecurityContext`.

---

## 8. CORS (so the HTML pages can call the API)

```java
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Value("${app.cors.allowed-origin}") private String origin;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(origin, "http://localhost:5500")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}
```

---

## 9. Seed Data (`CommandLineRunner`)

```java
@Bean
CommandLineRunner seed(UserRepository users, SportRepository sports, PasswordEncoder enc) {
    return args -> {
        if (!users.existsById("admin")) {
            Admin a = new Admin();
            a.setUserId("admin");
            a.setFaculty("Technology");
            a.setPasswordHash(enc.encode("Admin@123"));
            users.save(a);
        }
        // insert indoor/outdoor facilities and sports if table is empty
    };
}
```

---

## 10. Run

```bash
cd backend
./mvnw spring-boot:run        # Windows: mvnw.cmd spring-boot:run
```

API is available at `http://localhost:8080/api`. Quick test:

```
POST http://localhost:8080/api/auth/login
{ "userId": "admin", "password": "Admin@123" }
```

---

## 11. Common Problems

| Problem | Fix |
|---|---|
| `Access denied for user 'root'` | Check username/password in `application.properties` |
| CORS error in browser | Check origin in `CorsConfig` matches how you open the frontend (`127.0.0.1` vs `localhost`) |
| 403 on every request | Token missing/expired, or the endpoint is not permitted in `SecurityConfig` |
| `Unknown database` | Create `sport_ms` first |
| Port 8080 in use | Change `server.port` |
