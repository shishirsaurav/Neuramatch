package com.neuramatch.matching.repository;

import com.neuramatch.matching.entity.SkillNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Repository for querying the skill knowledge graph
 */
@Repository
public interface SkillGraphRepository extends Neo4jRepository<SkillNode, String> {

    /**
     * Find a skill by name (case-insensitive)
     */
    Optional<SkillNode> findByNameIgnoreCase(String name);

    /**
     * Find skills by category
     */
    List<SkillNode> findByCategory(SkillNode.SkillCategory category);

    /**
     * Find popular skills (popularity >= threshold)
     */
    @Query("MATCH (s:Skill) WHERE s.popularity >= $popularity RETURN s ORDER BY s.popularity DESC")
    List<SkillNode> findPopularSkills(@Param("popularity") Double popularity);

    /**
     * Find trending skills (trendScore >= threshold)
     */
    @Query("MATCH (s:Skill) WHERE s.trendScore >= $trendScore RETURN s ORDER BY s.trendScore DESC")
    List<SkillNode> findTrendingSkills(@Param("trendScore") Double trendScore);

    /**
     * Find all prerequisites for a skill (including transitive dependencies)
     */
    @Query("MATCH (s:Skill {name: $skillName})-[:REQUIRES*]->(req:Skill) RETURN DISTINCT req")
    List<SkillNode> findAllPrerequisites(@Param("skillName") String skillName);

    /**
     * Find direct prerequisites for a skill
     */
    @Query("MATCH (s:Skill {name: $skillName})-[r:REQUIRES]->(req:Skill) " +
           "RETURN req, r ORDER BY r.strength DESC")
    List<SkillNode> findDirectPrerequisites(@Param("skillName") String skillName);

    /**
     * Find required prerequisites (required=true)
     */
    @Query("MATCH (s:Skill {name: $skillName})-[r:REQUIRES {required: true}]->(req:Skill) " +
           "RETURN req ORDER BY r.strength DESC")
    List<SkillNode> findRequiredPrerequisites(@Param("skillName") String skillName);

    /**
     * Find complementary skills that work well together
     */
    @Query("MATCH (s:Skill {name: $skillName})-[c:COMPLEMENTS]->(comp:Skill) " +
           "RETURN comp, c ORDER BY c.strength DESC LIMIT $limit")
    List<SkillNode> findComplementarySkills(@Param("skillName") String skillName, @Param("limit") int limit);

    /**
     * Find complementary skills with high commonality
     */
    @Query("MATCH (s:Skill {name: $skillName})-[c:COMPLEMENTS]->(comp:Skill) " +
           "WHERE c.commonality >= $minCommonality " +
           "RETURN comp ORDER BY c.commonality DESC, c.strength DESC")
    List<SkillNode> findHighlyComplementarySkills(
        @Param("skillName") String skillName,
        @Param("minCommonality") Double minCommonality
    );

    /**
     * Find alternative skills (interchangeable)
     */
    @Query("MATCH (s:Skill {name: $skillName})-[a:ALTERNATIVE_TO]->(alt:Skill) " +
           "RETURN alt, a ORDER BY a.similarity DESC")
    List<SkillNode> findAlternativeSkills(@Param("skillName") String skillName);

    /**
     * Find alternatives with high transferability
     */
    @Query("MATCH (s:Skill {name: $skillName})-[a:ALTERNATIVE_TO]->(alt:Skill) " +
           "WHERE a.transferability >= $minTransferability " +
           "RETURN alt ORDER BY a.transferability DESC, a.similarity DESC")
    List<SkillNode> findEasilyTransferableAlternatives(
        @Param("skillName") String skillName,
        @Param("minTransferability") Double minTransferability
    );

    /**
     * Resolve skill synonyms to canonical name
     */
    @Query("MATCH (s:Skill {name: $skillName})-[:SYNONYM_OF*]->(canonical:Skill) " +
           "WHERE NOT (canonical)-[:SYNONYM_OF]->() " +
           "RETURN canonical")
    Optional<SkillNode> findCanonicalSkill(@Param("skillName") String skillName);

    /**
     * Find all synonyms for a skill
     */
    @Query("MATCH (s:Skill {name: $skillName})<-[:SYNONYM_OF]-(syn:Skill) RETURN syn")
    List<SkillNode> findSynonyms(@Param("skillName") String skillName);

    /**
     * Find skills that are part of a larger ecosystem/framework
     */
    @Query("MATCH (s:Skill {name: $skillName})-[:PART_OF]->(parent:Skill) RETURN parent")
    List<SkillNode> findParentSkills(@Param("skillName") String skillName);

    /**
     * Find sub-skills that are part of this skill
     */
    @Query("MATCH (s:Skill {name: $skillName})<-[:PART_OF]-(child:Skill) RETURN child")
    List<SkillNode> findChildSkills(@Param("skillName") String skillName);

    /**
     * Find missing prerequisites given a set of known skills
     */
    @Query("MATCH (target:Skill {name: $targetSkill})-[:REQUIRES]->(req:Skill) " +
           "WHERE NOT req.name IN $knownSkills " +
           "RETURN DISTINCT req")
    List<SkillNode> findMissingPrerequisites(
        @Param("targetSkill") String targetSkill,
        @Param("knownSkills") Set<String> knownSkills
    );

    /**
     * Find skill gaps - what skills would complement existing skillset
     */
    @Query("MATCH (known:Skill) WHERE known.name IN $knownSkills " +
           "MATCH (known)-[:COMPLEMENTS]->(suggested:Skill) " +
           "WHERE NOT suggested.name IN $knownSkills " +
           "WITH suggested, COUNT(known) as matchCount " +
           "RETURN suggested ORDER BY matchCount DESC, suggested.popularity DESC LIMIT $limit")
    List<SkillNode> findSkillGaps(
        @Param("knownSkills") Set<String> knownSkills,
        @Param("limit") int limit
    );

    /**
     * Find learning path - ordered list of skills to learn
     */
    @Query("MATCH path = (start:Skill {name: $currentSkill})-[:REQUIRES*]->(target:Skill {name: $targetSkill}) " +
           "RETURN nodes(path) as learningPath " +
           "ORDER BY length(path) ASC LIMIT 1")
    List<SkillNode> findLearningPath(
        @Param("currentSkill") String currentSkill,
        @Param("targetSkill") String targetSkill
    );

    /**
     * Calculate skill similarity score between two skillsets
     */
    @Query("MATCH (s1:Skill) WHERE s1.name IN $skillSet1 " +
           "MATCH (s2:Skill) WHERE s2.name IN $skillSet2 " +
           "WITH s1, s2 " +
           "OPTIONAL MATCH (s1)-[r:COMPLEMENTS|ALTERNATIVE_TO]-(s2) " +
           "RETURN COUNT(DISTINCT r) as relationshipCount, " +
           "       COUNT(DISTINCT s1) as set1Size, " +
           "       COUNT(DISTINCT s2) as set2Size")
    SkillSimilarityScore calculateSkillSetSimilarity(
        @Param("skillSet1") Set<String> skillSet1,
        @Param("skillSet2") Set<String> skillSet2
    );

    /**
     * Find skills by text search (name or description)
     */
    @Query("MATCH (s:Skill) " +
           "WHERE toLower(s.name) CONTAINS toLower($searchTerm) " +
           "   OR toLower(s.displayName) CONTAINS toLower($searchTerm) " +
           "   OR toLower(s.description) CONTAINS toLower($searchTerm) " +
           "RETURN s ORDER BY s.popularity DESC")
    List<SkillNode> searchSkills(@Param("searchTerm") String searchTerm);

    /**
     * Projection interface for skill similarity calculation
     */
    interface SkillSimilarityScore {
        Integer getRelationshipCount();
        Integer getSet1Size();
        Integer getSet2Size();
    }
}
