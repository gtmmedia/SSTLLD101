public class Main {
    public static void main(String[] args) {
        System.out.println("=== Evaluation Pipeline ===");
        Submission sub = new Submission("23BCS1007", "public class A{}", "A.java");
        
        // Inject dependencies
        Rubric rubric = new Rubric();
        Checker checker = new PlagiarismChecker();
        Grader grader = new CodeGrader();
        Writer writer = new ReportWriter();
        
        EvaluationPipeline pipeline = new EvaluationPipeline(rubric, checker, grader, writer);
        pipeline.evaluate(sub);
    }
}
