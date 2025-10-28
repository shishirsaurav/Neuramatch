package com.neuramatch.matching.service;

import com.neuramatch.matching.dto.InterviewQuestionDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class InterviewQuestionService {

    // TODO: Integrate with EmbeddingService for AI-powered question generation

    /**
     * Generate interview questions for a specific match
     */
    public List<InterviewQuestionDTO> generateQuestionsForMatch(
            Long jobId,
            Long resumeId,
            List<String> requiredSkills,
            List<String> candidateSkills,
            List<String> experienceTitles) {

        log.info("Generating interview questions for job={}, resume={}", jobId, resumeId);

        List<InterviewQuestionDTO> questions = new ArrayList<>();

        // 1. Technical questions for required skills
        questions.addAll(generateTechnicalQuestions(requiredSkills, 3));

        // 2. Behavioral questions from experience
        questions.addAll(generateBehavioralQuestions(experienceTitles, 2));

        // 3. Skill gap questions
        List<String> skillGaps = findSkillGaps(requiredSkills, candidateSkills);
        if (!skillGaps.isEmpty()) {
            questions.addAll(generateSkillGapQuestions(skillGaps, 2));
        }

        // 4. Scenario-based questions
        questions.addAll(generateScenarioQuestions(requiredSkills, 2));

        return questions;
    }

    /**
     * Generate technical questions for specific skills
     */
    private List<InterviewQuestionDTO> generateTechnicalQuestions(
            List<String> skills, int questionsPerSkill) {

        List<InterviewQuestionDTO> questions = new ArrayList<>();

        // Get top priority skills
        List<String> topSkills = skills.stream().limit(3).collect(Collectors.toList());

        for (String skill : topSkills) {
            // Conceptual question
            questions.add(InterviewQuestionDTO.builder()
                    .skill(skill)
                    .type("TECHNICAL_CONCEPTUAL")
                    .difficulty("MEDIUM")
                    .question(generateConceptualQuestion(skill))
                    .suggestedAnswer(generateSuggestedAnswer(skill, "conceptual"))
                    .evaluationCriteria(generateEvaluationCriteria(skill, "conceptual"))
                    .timeLimit(10) // minutes
                    .build());

            // Practical question
            questions.add(InterviewQuestionDTO.builder()
                    .skill(skill)
                    .type("TECHNICAL_PRACTICAL")
                    .difficulty("MEDIUM")
                    .question(generatePracticalQuestion(skill))
                    .suggestedAnswer(generateSuggestedAnswer(skill, "practical"))
                    .evaluationCriteria(generateEvaluationCriteria(skill, "practical"))
                    .timeLimit(15)
                    .build());
        }

        return questions;
    }

    /**
     * Generate behavioral questions based on experience
     */
    private List<InterviewQuestionDTO> generateBehavioralQuestions(
            List<String> experienceTitles, int count) {

        List<InterviewQuestionDTO> questions = new ArrayList<>();

        // Leadership question
        questions.add(InterviewQuestionDTO.builder()
                .skill("Leadership")
                .type("BEHAVIORAL")
                .difficulty("MEDIUM")
                .question("Tell me about a time when you had to lead a team through a challenging project. " +
                         "What was your approach, and what was the outcome?")
                .suggestedAnswer("Look for: Clear situation description, specific actions taken, " +
                               "measurable results, lessons learned")
                .evaluationCriteria(Arrays.asList(
                        "Demonstrates leadership qualities",
                        "Shows problem-solving ability",
                        "Provides specific examples",
                        "Reflects on learnings"
                ))
                .timeLimit(10)
                .build());

        // Conflict resolution
        questions.add(InterviewQuestionDTO.builder()
                .skill("Communication")
                .type("BEHAVIORAL")
                .difficulty("MEDIUM")
                .question("Describe a situation where you disagreed with a colleague or manager about " +
                         "a technical decision. How did you handle it?")
                .suggestedAnswer("Look for: Respectful communication, data-driven arguments, " +
                               "willingness to compromise, focus on outcomes")
                .evaluationCriteria(Arrays.asList(
                        "Demonstrates emotional intelligence",
                        "Shows communication skills",
                        "Focuses on constructive resolution",
                        "Learns from conflicts"
                ))
                .timeLimit(10)
                .build());

        return questions;
    }

    /**
     * Generate questions to assess skill gaps
     */
    private List<InterviewQuestionDTO> generateSkillGapQuestions(
            List<String> skillGaps, int count) {

        List<InterviewQuestionDTO> questions = new ArrayList<>();

        for (String skill : skillGaps.stream().limit(count).collect(Collectors.toList())) {
            questions.add(InterviewQuestionDTO.builder()
                    .skill(skill)
                    .type("SKILL_GAP_ASSESSMENT")
                    .difficulty("EASY")
                    .question(String.format("I see you haven't worked with %s directly. " +
                            "Are you familiar with it, and how quickly do you think you could learn it?",
                            skill))
                    .suggestedAnswer("Look for: Willingness to learn, related experience, " +
                                   "realistic timeline, concrete learning plan")
                    .evaluationCriteria(Arrays.asList(
                            "Shows enthusiasm for learning",
                            "Has relevant transferable skills",
                            "Provides realistic timeline",
                            "Demonstrates learning agility"
                    ))
                    .timeLimit(5)
                    .build());
        }

        return questions;
    }

    /**
     * Generate scenario-based questions
     */
    private List<InterviewQuestionDTO> generateScenarioQuestions(
            List<String> skills, int count) {

        List<InterviewQuestionDTO> questions = new ArrayList<>();

        if (skills.contains("Java") || skills.contains("Spring Boot")) {
            questions.add(InterviewQuestionDTO.builder()
                    .skill("System Design")
                    .type("SCENARIO")
                    .difficulty("HARD")
                    .question("Design a microservices architecture for an e-commerce platform " +
                             "that needs to handle 10,000 requests per second. What services would you create, " +
                             "and how would you ensure scalability and reliability?")
                    .suggestedAnswer("Look for: Service decomposition, data consistency strategies, " +
                                   "caching, load balancing, monitoring, failure handling")
                    .evaluationCriteria(Arrays.asList(
                            "Identifies key services logically",
                            "Considers scalability patterns",
                            "Addresses failure scenarios",
                            "Discusses trade-offs"
                    ))
                    .timeLimit(20)
                    .build());
        }

        if (skills.contains("Python") || skills.contains("Machine Learning")) {
            questions.add(InterviewQuestionDTO.builder()
                    .skill("Machine Learning")
                    .type("SCENARIO")
                    .difficulty("HARD")
                    .question("You need to build a recommendation system for a streaming platform. " +
                             "Walk me through your approach, from data collection to model deployment.")
                    .suggestedAnswer("Look for: Data requirements, feature engineering, model selection, " +
                                   "evaluation metrics, production considerations")
                    .evaluationCriteria(Arrays.asList(
                            "Understands ML pipeline",
                            "Considers data quality",
                            "Discusses multiple approaches",
                            "Addresses production challenges"
                    ))
                    .timeLimit(20)
                    .build());
        }

        return questions.stream().limit(count).collect(Collectors.toList());
    }

    // Helper methods
    private String generateConceptualQuestion(String skill) {
        Map<String, String> conceptualQuestions = new HashMap<>();
        conceptualQuestions.put("Java", "Explain the difference between ArrayList and LinkedList. " +
                "When would you use one over the other?");
        conceptualQuestions.put("Spring Boot", "Explain how Spring Boot's auto-configuration works. " +
                "How does it know which beans to create?");
        conceptualQuestions.put("React", "Explain the React component lifecycle. " +
                "When would you use useEffect vs useLayoutEffect?");
        conceptualQuestions.put("Python", "Explain the difference between a list and a tuple in Python. " +
                "What are the performance implications?");
        conceptualQuestions.put("Kubernetes", "Explain the difference between a Deployment and a StatefulSet. " +
                "When would you use each?");

        return conceptualQuestions.getOrDefault(skill,
                String.format("Explain the core concepts of %s and when you would use it.", skill));
    }

    private String generatePracticalQuestion(String skill) {
        Map<String, String> practicalQuestions = new HashMap<>();
        practicalQuestions.put("Java", "Write a method to find the kth largest element in an unsorted array. " +
                "What's the time complexity?");
        practicalQuestions.put("Spring Boot", "How would you implement a REST API endpoint with pagination, " +
                "sorting, and filtering? Show the code structure.");
        practicalQuestions.put("React", "Create a custom hook for data fetching with loading and error states.");
        practicalQuestions.put("Python", "Write a function to merge two sorted lists. " +
                "Optimize for space and time complexity.");
        practicalQuestions.put("SQL", "Write a query to find the top 5 customers by total purchase amount " +
                "in the last 30 days.");

        return practicalQuestions.getOrDefault(skill,
                String.format("Demonstrate how you would solve a common problem using %s.", skill));
    }

    private String generateSuggestedAnswer(String skill, String type) {
        if ("conceptual".equals(type)) {
            return "Look for clear explanation of concepts, understanding of trade-offs, " +
                   "ability to provide real-world examples";
        } else {
            return "Look for correct implementation, consideration of edge cases, " +
                   "time/space complexity analysis, clean code practices";
        }
    }

    private List<String> generateEvaluationCriteria(String skill, String type) {
        if ("conceptual".equals(type)) {
            return Arrays.asList(
                    "Demonstrates deep understanding",
                    "Explains trade-offs clearly",
                    "Provides relevant examples",
                    "Communicates effectively"
            );
        } else {
            return Arrays.asList(
                    "Correct implementation",
                    "Handles edge cases",
                    "Analyzes complexity",
                    "Writes clean code"
            );
        }
    }

    private List<String> findSkillGaps(List<String> required, List<String> candidate) {
        Set<String> candidateSet = new HashSet<>(candidate);
        return required.stream()
                .filter(skill -> !candidateSet.contains(skill))
                .collect(Collectors.toList());
    }
}
