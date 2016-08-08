package edu.purdue.cybercenter.dm.service;

import edu.purdue.cybercenter.dm.domain.MetaField;
import edu.purdue.cybercenter.dm.domain.Report;
import edu.purdue.cybercenter.dm.util.DomainObjectHelper;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import javax.persistence.TypedQuery;
import org.springframework.stereotype.Service;
import javax.swing.table.TableModel;
import javax.xml.bind.DatatypeConverter;
import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;
import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.engine.classic.core.StaticDataRow;
import org.pentaho.reporting.engine.classic.core.TableDataFactory;
import org.pentaho.reporting.engine.classic.core.modules.output.pageable.pdf.PdfReportUtil;
import org.pentaho.reporting.engine.classic.core.modules.output.table.rtf.RTFReportUtil;
import org.pentaho.reporting.engine.classic.core.modules.output.table.xls.ExcelReportUtil;
import org.pentaho.reporting.libraries.resourceloader.ResourceException;
import org.pentaho.reporting.libraries.resourceloader.ResourceManager;
import org.pentaho.reporting.libraries.resourceloader.Resource;
import org.pentaho.reporting.engine.classic.core.DataFactory;
import org.pentaho.reporting.engine.classic.core.DataRow;
import org.pentaho.reporting.engine.classic.core.ReportProcessingException;
import org.pentaho.reporting.engine.classic.core.modules.output.table.html.HtmlReportUtil;
import org.pentaho.reporting.libraries.resourceloader.ResourceCreationException;
import org.pentaho.reporting.libraries.resourceloader.ResourceKeyCreationException;
import org.pentaho.reporting.libraries.resourceloader.ResourceLoadingException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author xu222
 */
@Service
public class ReportService {

    @Autowired
    private DomainObjectService domainObjectService;

    @Autowired
    private DatasetService datasetService;

    static private final Set<String> predefinedKeys = new HashSet<>(Arrays.asList("filename", "type", "root_path", "uuid", "out_filename"));

    static {
        ClassicEngineBoot.getInstance().start();
    }

    private MasterReport instantiateReport(String reportTemplate) throws MalformedURLException, ResourceLoadingException, ResourceCreationException, ResourceKeyCreationException, ResourceException {
        ResourceManager manager = new ResourceManager();
        manager.registerDefaults();

        URL urlToReport = new URL("file:" + reportTemplate);
        Resource resource = manager.createDirectly(urlToReport, MasterReport.class);
        MasterReport report = (MasterReport) resource.getResource();

        return report;
    }

    public Map<String, String> getParameters(String reportTemplate) throws MalformedURLException, ResourceCreationException, ResourceKeyCreationException, ResourceException {
        Map<String, String> result = new HashMap<>();
        MasterReport report = instantiateReport(reportTemplate);

        for (int i = 0; i < report.getParameterDefinition().getParameterCount(); ++i) {
            result.put(report.getParameterDefinition().getParameterDefinition(i).getName(), report.getParameterDefinition().getParameterDefinition(i).getValueType().toString());
        }

        return result;
    }

    public Map<String, String> generateReport(String reportTemplate, Map<String, Object> parameters) throws MalformedURLException, ResourceCreationException, ResourceKeyCreationException, ResourceException, IOException, ReportProcessingException {

        Integer jobId = (Integer) parameters.get(MetaField.JobId);
        MasterReport report = instantiateReport(reportTemplate);

        parameters.keySet().stream().filter((key) -> !(predefinedKeys.contains(key))).forEach((key) -> {
            report.getParameterValues().put(key, parameters.get(key));
        });

        DataRow dr = new StaticDataRow();
        DataFactory df = report.getDataFactory();

        if (df.isQueryExecutable("data", dr)) {
            TableModel tm = df.queryData("data", dr);
            String uuid = (String) parameters.get("uuid");
            if (uuid == null || uuid.isEmpty()) {
                uuid = (String) tm.getValueAt(0, 0);
            }

            String version = (String) tm.getValueAt(0, 1);
            TableDataFactory tdf = getData(uuid, version, jobId);
            report.setDataFactory(tdf);
        }

        String type = (String) parameters.get("type");
        String outFilename = (parameters.containsKey("out_filename"))? (String) parameters.get("out_filename") : getOutFilename(type);
        File outFolder = new File((String) parameters.get("rootPath"));

        if (!outFolder.exists()) {
            outFolder.mkdir();
        }

        Map<String, String> result = new HashMap<>();
        result.put("filename", outFilename);
        String outFilenamePath = outFolder.getAbsolutePath() + "/" + outFilename;

        switch (type.toLowerCase()) {
            case "pdf":
                result.put("content_type", "application/pdf");
                PdfReportUtil.createPDF(report, outFilenamePath);
                break;
            case "xls":
                result.put("content_type", "application/vnd.ms-excel");
                ExcelReportUtil.createXLS(report, outFilenamePath);
                break;
            case "xlsx":
                result.put("content_type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                ExcelReportUtil.createXLSX(report, outFilenamePath);
                break;
            case "rtf":
                result.put("content_type", "application/rtf");
                RTFReportUtil.createRTF(report, outFilenamePath);
                break;
            case "zip":
                result.put("content_type", "application/zip");
                HtmlReportUtil.createZIPHTML(report, outFilenamePath);
                break;
            default:
                throw new RuntimeException("Report type given is not supported: " + type + ".");
        }

        return result;
    }

    public String getReportTemplateFromId(UUID uuid) throws IOException {
        TypedQuery<Report> query = DomainObjectHelper.createNamedQuery("Report.findByUuid", Report.class);
        query.setParameter("uuid", uuid);
        Report report = domainObjectService.executeTypedQueryWithSingleResult(query);

        Random r = new Random();
        String filename = System.getProperty("java.io.tmpdir")
                + System.getProperty("file.separator")
                + "report_designe_template_"
                + System.currentTimeMillis() + "_"
                + r.nextInt((int) 1e+7);
        byte[] content = DatatypeConverter.parseBase64Binary(report.getContent());
        Files.write(Paths.get(filename), content);
        return filename;
    }

    private String getOutFilename(String type) {
        return "_out" + System.currentTimeMillis() + "." + type;
    }

    private TableDataFactory getData(String uuid, String version, Integer jobId) {
        Map<String, Object> query = new HashMap<>();
        if (version != null && !version.trim().isEmpty()) {
            query.put(MetaField.TemplateVersion, UUID.fromString(version));
        }
        if (jobId != null) {
            query.put(MetaField.Current + MetaField.JobId, jobId);
        }
        Map<String, Object> aggregators = new HashMap<>();
        aggregators.put(DocumentService.AGGREGATOR_MATCH, query);
        List values = datasetService.find(UUID.fromString(uuid), aggregators);
        Map first = (Map) values.get(0);
        Set<String> keys = (Set<String>) first.keySet();
        String[] header = new String[keys.size()];
        int index = 0;
        for (String key : keys) {
            header[index++] = key;
        }
        ReportTableModel model = new ReportTableModel(header);

        for (Object o : values) {
            Map l = (Map) o;
            for (String key : keys) {
                try {
                    Object obj = l.get(key);
                    if (obj != null) {
                        if (obj.getClass() == String.class) {
                            model.add((String) obj);
                        } else if (obj.getClass() == Integer.class) {
                            model.add((Integer) obj);
                        } else if (obj.getClass() == Double.class || obj.getClass() == Float.class) {
                            model.add((Double) obj);
                        } else if (obj.getClass() == java.util.Date.class) {
                            model.add((java.util.Date) obj);
                        } else if (obj.getClass() == Boolean.class) {
                            model.add((Boolean) obj);
                        } else {
                            model.add(obj);
                        }
                    } else {
                        model.add("null");
                    }
                } catch (java.lang.ClassCastException e) {
                    model.add("not available");
                }
            }
        }
        return new TableDataFactory("data", model);
    }
}

final class ReportTableModel extends javax.swing.table.AbstractTableModel {
    private static final long serialVersionUID = 1L;

    private final java.util.List<Object> data;

    private String[] columnNames;

    private boolean editable = true;

    public ReportTableModel(String[] columnNames) {
        data = new java.util.ArrayList<>();
        setColumnNames(columnNames);
    }

    public void setColumnNames(String[] columnNames) {
        removeAll();
        this.columnNames = columnNames;
    }

    @Override
    public int getRowCount() {
        return data.size() / columnNames.length;
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        int pos = rowIndex * columnNames.length + columnIndex;
        if (pos < data.size()) {
            return data.get(pos);
        } else {
            return null;
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return editable;
    }

    public void setCellEditable(boolean editable) {
        this.editable = editable;
    }

    public boolean getCellEditable() {
        return editable;
    }

    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }

    public void add(String element) {
        if (element == null) {
            data.add("null");
        } else {
            data.add(" " + element.trim());
        }
    }

    public void add(int element) {
        data.add((double) element);
    }

    public void add(double element, int decimal) {
        data.add(mathRound(element, decimal));
    }

    public void add(double element) {
        data.add(mathRound(element, 3));
    }

    public void add(boolean element) {
        data.add(element);
    }

    public void add(java.net.InetAddress addr) {
        data.add(addr.toString().substring(1));
    }

    public void add(java.awt.Color color) {
        data.add(color);
    }

    public void add(java.util.Date date) {
        data.add(date);
    }

    public void add(Object obj) {
        data.add(obj);
    }

    public void removeAll() {
        data.clear();
    }

    @Override
    public Class getColumnClass(int col) {
        Class returnValue;
        if ((col >= 0) && (col < getColumnCount())) {
            if (getValueAt(0, col) == null) {
                returnValue = null;
            } else {
                returnValue = getValueAt(0, col).getClass();
            }
        } else {
            returnValue = Object.class;
        }
        return returnValue;
    }

    private static double mathRound(double val, int d) {
        if (Double.isNaN(val)) {
            return val;
        }
        java.text.DecimalFormat fmt = new java.text.DecimalFormat();
        fmt.setMaximumFractionDigits(d);
        return Double.valueOf(fmt.format(val));
    }
}
