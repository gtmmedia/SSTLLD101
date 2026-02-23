import java.nio.charset.StandardCharsets;

public class CsvExporter extends Exporter {
    @Override
    protected ExportResult doExport(ExportRequest req) {
        // Escape commas and newlines for CSV, preserve data
        String safeTitle = escapeCsv(req.title);
        String safeBody = escapeCsv(req.body);
        String csv = "title,body\n" + safeTitle + "," + safeBody + "\n";
        return new ExportResult("text/csv", csv.getBytes(StandardCharsets.UTF_8));
    }

    private String escapeCsv(String s) {
        if (s.contains(",") || s.contains("\n") || s.contains("\"") ) {
            s = s.replace("\"", "\"\"");
            return '"' + s.replace("\n", " ") + '"';
        }
        return s.replace("\n", " ");
    }
}
