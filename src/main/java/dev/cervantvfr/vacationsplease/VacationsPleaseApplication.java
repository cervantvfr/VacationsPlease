package dev.cervantvfr.vacationsplease;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import java.time.LocalDate;
import dev.cervantvfr.vacationsplease.domain.repository.LeaveRequestRepository;


@SpringBootApplication
public class VacationsPleaseApplication {

    public static void main(String[] args) {
        SpringApplication.run(VacationsPleaseApplication.class, args);
    }

    // @Bean
    // CommandLineRunner overlapProbe(LeaveRequestRepository repo) {
    //     return args -> {
    //         boolean overlap = repo.existsOverlappingPendingOrApproved(
    //                 1L,
    //                 LocalDate.of(2026, 6, 11),
    //                 LocalDate.of(2026, 6, 13)
    //         );
    //         System.out.println("OVERLAP_EXPECT_TRUE = " + overlap);

    //         boolean noOverlap = repo.existsOverlappingPendingOrApproved(
    //                 1L,
    //                 LocalDate.of(2026, 6, 20),
    //                 LocalDate.of(2026, 6, 22)
    //         );
    //         System.out.println("OVERLAP_EXPECT_FALSE = " + noOverlap);
    //     };
    // }

}
