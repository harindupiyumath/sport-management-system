package com.wayamba.sportms;

import com.wayamba.sportms.model.*;
import com.wayamba.sportms.repository.FacilityRepository;
import com.wayamba.sportms.repository.SportRepository;
import com.wayamba.sportms.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class SportManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(SportManagementApplication.class, args);
    }

    @Bean
    CommandLineRunner seed(UserRepository users, FacilityRepository facilities, SportRepository sports, PasswordEncoder enc) {
        return args -> {
            // Seed Super Admin if not exists
            if (!users.existsById("admin")) {
                Admin admin = new Admin();
                admin.setUserId("admin");
                admin.setFaculty("TECHNOLOGY");
                admin.setPasswordHash(enc.encode("Admin@123"));
                users.save(admin);
                System.out.println("Default Super Admin seeded: admin / Admin@123");
            }

            // Seed Facilities
            IndoorCourt indoorCourt = (IndoorCourt) facilities.findByName("Indoor Court")
                    .orElseGet(() -> facilities.save(new IndoorCourt("Indoor Court")));

            OutdoorGround outdoorGround = (OutdoorGround) facilities.findByName("Outdoor Ground")
                    .orElseGet(() -> facilities.save(new OutdoorGround("Outdoor Ground")));

            // Seed Indoor Sports
            String[] indoorSports = {"Badminton", "Volleyball", "Table Tennis", "Carrom"};
            for (String sName : indoorSports) {
                if (sports.findByName(sName).isEmpty()) {
                    sports.save(new Sport(sName, indoorCourt));
                }
            }

            // Seed Outdoor Sports
            String[] outdoorSports = {"Cricket", "Football", "Netball", "Athletics"};
            for (String sName : outdoorSports) {
                if (sports.findByName(sName).isEmpty()) {
                    sports.save(new Sport(sName, outdoorGround));
                }
            }
        };
    }
}

