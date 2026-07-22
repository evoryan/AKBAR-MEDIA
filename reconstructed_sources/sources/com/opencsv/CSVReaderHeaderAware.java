package com.opencsv;

import com.opencsv.exceptions.CsvValidationException;
import com.opencsv.processor.RowProcessor;
import com.opencsv.validators.LineValidatorAggregator;
import com.opencsv.validators.RowValidatorAggregator;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/* loaded from: classes11.dex */
public class CSVReaderHeaderAware extends CSVReader {
    private final Map<String, Integer> headerIndex;

    public CSVReaderHeaderAware(Reader reader) throws IOException {
        super(reader);
        this.headerIndex = new HashMap();
        initializeHeader();
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public CSVReaderHeaderAware(Reader reader, int skipLines, ICSVParser parser, boolean keepCR, boolean verifyReader, int multilineLimit, Locale errorLocale, LineValidatorAggregator lineValidatorAggregator, RowValidatorAggregator rowValidatorAggregator, RowProcessor rowProcessor) throws IOException {
        super(reader, skipLines, parser, keepCR, verifyReader, multilineLimit, errorLocale, lineValidatorAggregator, rowValidatorAggregator, rowProcessor);
        this.headerIndex = new HashMap();
        initializeHeader();
    }

    public String[] readNext(String... headerNames) throws IOException, CsvValidationException {
        if (headerNames == null) {
            return super.readNextSilently();
        }
        String[] strings = readNext();
        if (strings == null) {
            return null;
        }
        if (strings.length != this.headerIndex.size()) {
            throw new IOException(String.format(ResourceBundle.getBundle(ICSVParser.DEFAULT_BUNDLE_NAME, this.errorLocale).getString("header.data.mismatch.with.line.number"), Long.valueOf(getRecordsRead()), Integer.valueOf(this.headerIndex.size()), Integer.valueOf(strings.length)));
        }
        String[] response = new String[headerNames.length];
        for (int i = 0; i < headerNames.length; i++) {
            String headerName = headerNames[i];
            Integer index = this.headerIndex.get(headerName);
            if (index == null) {
                throw new IllegalArgumentException(String.format(ResourceBundle.getBundle(ICSVParser.DEFAULT_BUNDLE_NAME, this.errorLocale).getString("header.nonexistant"), headerName));
            }
            response[i] = strings[index.intValue()];
        }
        return response;
    }

    public Map<String, String> readMap() throws IOException, CsvValidationException {
        String[] strings = readNext();
        if (strings == null) {
            return null;
        }
        if (strings.length != this.headerIndex.size()) {
            throw new IOException(String.format(ResourceBundle.getBundle(ICSVParser.DEFAULT_BUNDLE_NAME, this.errorLocale).getString("header.data.mismatch.with.line.number"), Long.valueOf(getRecordsRead()), Integer.valueOf(this.headerIndex.size()), Integer.valueOf(strings.length)));
        }
        Map<String, String> resultMap = new HashMap<>(this.headerIndex.size() * 2);
        for (Map.Entry<String, Integer> entry : this.headerIndex.entrySet()) {
            if (entry.getValue().intValue() < strings.length) {
                resultMap.put(entry.getKey(), strings[entry.getValue().intValue()]);
            }
        }
        return resultMap;
    }

    private void initializeHeader() throws IOException {
        String[] headers = super.readNextSilently();
        for (int i = 0; i < headers.length; i++) {
            this.headerIndex.put(headers[i], Integer.valueOf(i));
        }
    }
}
