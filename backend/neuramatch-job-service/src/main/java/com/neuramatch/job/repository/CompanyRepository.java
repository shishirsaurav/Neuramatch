package com.neuramatch.job.repository;

import com.neuramatch.job.entity.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    // Find by name
    Optional<Company> findByCompanyNameIgnoreCase(String companyName);

    boolean existsByCompanyNameIgnoreCase(String companyName);

    // Find by status
    List<Company> findByStatus(Company.CompanyStatus status);

    Page<Company> findByStatus(Company.CompanyStatus status, Pageable pageable);

    // Find verified companies
    List<Company> findByIsVerifiedTrue();

    Page<Company> findByIsVerifiedTrue(Pageable pageable);

    // Find by industry
    List<Company> findByIndustryIgnoreCase(String industry);

    Page<Company> findByIndustryIgnoreCase(String industry, Pageable pageable);

    // Find by company size
    List<Company> findByCompanySize(Company.CompanySize companySize);

    // Find by country
    List<Company> findByCountryIgnoreCase(String country);

    // Search companies
    @Query("SELECT c FROM Company c WHERE " +
           "LOWER(c.companyName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Company> searchCompanies(@Param("searchTerm") String searchTerm, Pageable pageable);

    // Find companies with active jobs
    @Query("SELECT DISTINCT c FROM Company c JOIN c.jobs j WHERE j.status = 'ACTIVE'")
    Page<Company> findCompaniesWithActiveJobs(Pageable pageable);

    // Count companies by status
    long countByStatus(Company.CompanyStatus status);

    // Find by name pattern
    List<Company> findByCompanyNameContainingIgnoreCase(String name);

    // Find with jobs
    @Query("SELECT DISTINCT c FROM Company c LEFT JOIN FETCH c.jobs WHERE c.id = :id")
    Optional<Company> findByIdWithJobs(@Param("id") Long id);

    // Find top hiring companies
    @Query("SELECT c, COUNT(j) as jobCount FROM Company c JOIN c.jobs j " +
           "WHERE j.status = 'ACTIVE' GROUP BY c ORDER BY jobCount DESC")
    Page<Object[]> findTopHiringCompanies(Pageable pageable);
}
