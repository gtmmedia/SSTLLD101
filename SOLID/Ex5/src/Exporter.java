/**
 * Exporter contract:
 * - Accepts non-null ExportRequest with non-null title and body (empty allowed).
 * - Never throws for valid requests.
 * - Handles large content gracefully (no arbitrary limits).
 * - No lossy or surprising conversions; preserves data meaning.
 * - Returns ExportResult with correct contentType and bytes.
 */
public abstract class Exporter {
    public final ExportResult export(ExportRequest req) {
        if (req == null) throw new IllegalArgumentException("ExportRequest must not be null");
        if (req.title == null || req.body == null)
            throw new IllegalArgumentException("title/body must not be null");
        return doExport(req);
    }

    protected abstract ExportResult doExport(ExportRequest req);
}
