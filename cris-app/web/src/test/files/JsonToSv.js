////////////////////////////////////////////////////////////
// Program starts
////////////////////////////////////////////////////////////
try {
    importPackage(java.io);
    importPackage(java.lang);

    var jsonOut = {};
    jsonOut.is_valid = true;
    jsonOut.error_message = "";

    if (arguments.length != 2) {
        jsonOut.is_valid = false;
        jsonOut.error_message = "Syntax: java -jar js.jar JsonToSv.js svFile mappingFile";
        print(JSON.stringify(jsonOut));

        java.lang.System.exit(-1);
    }

    var mappingFilename = arguments[1];
    var mappingFileReader = new BufferedReader(new InputStreamReader(new FileInputStream(mappingFilename)));
    var jsonMapping = "";
    while ((line = mappingFileReader.readLine()) != null) {
        jsonMapping += line;
    }
    var mapping = JSON.parse(jsonMapping);

    var delimiter = mapping["$delimiter"];
    var hasHeader = mapping["$hasHeader"];
    if (delimiter) {
        delimiter = eval(delimiter);
    } else {
        // defailt
        delimiter = ",";
    }
    if (hasHeader) {
        hasHeader = eval(hasHeader);
    } else {
        // defailt
        hasHeader = true;
    }

    var reader = new BufferedReader(new InputStreamReader(System["in"]));
    var json = "";
    var line;
    while ((line = reader.readLine())) {
        json += line;
    }
    json = JSON.parse(json);

    // make sure it is an array
    if (!(json instanceof Array)) {
        jsonOut.is_valid = false;
        jsonOut.error_message = "The input should be an array";
        print(JSON.stringify(jsonOut));

        java.lang.System.exit(-1);
    }

    // generate header if requested
    var svFilename = arguments[0];
    var svFileWriter = new BufferedWriter(new FileWriter(svFilename));
    var record = "";
    if (hasHeader) {
        for (var key in mapping) {
            if (mapping.hasOwnProperty(key)) {
                if (["$delimiter", "$hasHeader"].indexOf(key) == -1) {
                    record += key + delimiter;
                }
            }
        }
        svFileWriter.write(record + "\n");
    }

    // generate records
    for (i = 0; i < json.length; i++) {
        record = evaluate(mapping, json[i]);
        svFileWriter.write(record + "\n");
    }
    svFileWriter.close();
} catch (ex) {
    jsonOut.is_valid = false;
    jsonOut.error_message = "Runtime exception: " + ex ? ex : "no further information";
}
print(JSON.stringify(jsonOut));


////////////////////////////////////////////////////////////
// library functions
////////////////////////////////////////////////////////////
function evaluate(mapping, $value) {
    var delimiter = eval(mapping["$delimiter"]);
    var record = "";
    for (var key in mapping) {
        if (mapping.hasOwnProperty(key)) {
            if (["$delimiter", "$hasHeader"].indexOf(key) == -1) {
                result = eval(mapping[key]);
                record += result + delimiter;
            }
        }
    }
    return record;
}
