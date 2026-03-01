public class EvaluationPipeline {
    private final Rubric rubric;
    private final Checker checker;
    private final Grader grader;
    private final Writer writer;

    public EvaluationPipeline(Rubric rubric, Checker checker, Grader grader, Writer writer) {
        this.rubric = rubric;
        this.checker = checker;
        this.grader = grader;
        this.writer = writer;
    }

    public void evaluate(Submission sub) {
        int plag = checker.check(sub);
        System.out.println("PlagiarismScore=" + plag);

        int code = grader.grade(sub, rubric);
        System.out.println("CodeScore=" + code);

        String reportName = writer.write(sub, plag, code);
        System.out.println("Report written: " + reportName);

        int total = plag + code;
        String result = (total >= 90) ? "PASS" : "FAIL";
        System.out.println("FINAL: " + result + " (total=" + total + ")");
    }
}
