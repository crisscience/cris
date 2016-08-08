////////////////////////////////////////////////////////////
// Program starts
////////////////////////////////////////////////////////////
try {
    importPackage(java.io);

    var jsonOut = {};
    jsonOut.is_valid = true;
    jsonOut.error_message = "";

    if (arguments.length != 2) {
        jsonOut.is_valid = false;
        jsonOut.error_message = "Syntax: java -jar js.jar svToJson.js svFile conversionFile";
        print(JSON.stringify(jsonOut));

        //java.lang.System.err.println("Syntax: java -jar js.jar svToJson.js svFile conversionFile");

        java.lang.System.exit(-1);
    }

    var mappingFilename = arguments[1];
    var mappingFileReader = new BufferedReader(new InputStreamReader(new FileInputStream(mappingFilename)));
    var jsonMapping = "";
    while ((line = mappingFileReader.readLine()) != null) {
        jsonMapping += line;
    }
    var mapping = JSON.parse(jsonMapping);

    var template = mapping["$template"];
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

    //java.lang.System.err.println("------------ Mapping File: " + mappingFilename + " -------------");
    //java.lang.System.err.println(JSON.stringify(jsonMapping));

    var svFilename = arguments[0];
    var svFileReader = new BufferedReader(new InputStreamReader(new FileInputStream(svFilename)));
    var count = 0;
    var $header = [];
    var $value = [];
    var numberOfFields = 0;
    var json = [];
    while ((line = svFileReader.readLine()) != null) {
        var ta = line.split(delimiter);
        if (count == 0) {
            // the first line establishes the number of fields
            numberOfFields = ta.length;
        }

        if (count == 0 && hasHeader) {
            // header
            for (i = 0; i < ta.length; i++) {
                $header.push(new String(ta[i]));
            }
            for (i = 0; i < $header.length; i++) {
                $header[$header[i]] = i;
            }

            //java.lang.System.err.println("------------ header " + " -------------");
            //java.lang.System.err.println(JSON.stringify($header));
            //java.lang.System.err.println("------------ Data   " + " -------------");
        } else if (line.trim() != "") {
            // data
            $value = []
            for (i = 0; i < ta.length; i++) {
                $value.push(new String(ta[i]));
            }
            for (i = 0; i < $value.length; i++) {
                $value[$header[i]] = $value[i];
            }

            //java.lang.System.err.println(JSON.stringify($value));

            if (numberOfFields != $value.length) {
                jsonOut.is_valid = false;
                jsonOut.error_message += "Line " + count + ": \"" + line + "\": wrong number of fields: " + $value.length + ". Should be: " + numberOfFields;
            } else {
                var result = evaluate(mapping, $header, $value);
                json.push(result);
            }
        }

        count++;
    }

    // pring to stdout
    jsonOut[template] = json;
    print(JSON.stringify(jsonOut));

    //java.lang.System.err.println("----------------- jsonOut -------------------");
    //java.lang.System.err.println(JSON.stringify(jsonOut));
} catch (ex) {
    jsonOut.is_valid = false;
    jsonOut.error_message = "Runtime exception: " + ex ? ex : "no further information";
    print(JSON.stringify(jsonOut));

    //java.lang.System.err.println("----------------- jsonOut -------------------");
    //java.lang.System.err.println(JSON.stringify(jsonOut));
}

////////////////////////////////////////////////////////////
// library functions
////////////////////////////////////////////////////////////
function matchHeaderAndEvaluate(pattern, range, expression, $header, $value) {
    var items = [];
    var i;
    var aRange = eval(range);

    for (i = 0; i < $header.length; i++) {
        if (aRange.indexOf(i) > -1) {
            var $match = pattern.exec($header[i]);
            var $cindex = i;
            var $cheader = $header[i];
            var $cvalue = $value[i];
            if ($match) {
                var result = evaluate(expression, $header, $value, $match, $cindex, $cheader, $cvalue);
                items.push(result);
            }
        }
    }
    return items;
}

function evaluate(expression, $header, $value, $match, $cindex, $cheader, $cvalue) {
    var result = null;
    var i, e, r;

    if (typeof expression == "string" || expression instanceof String) {
        // a string
        if (expression.trim() != "") {
            eval("var result = " + expression + ";");
        }
    } else if (expression instanceof Array) {
        // an array
        result = [];
        for (i = 0; i < expression.length; i++) {
            r = evaluate(expression[i], $header, $value, $match, $cindex, $cheader, $cvalue);
            result.push(r);
        }
    } else if (expression instanceof Object) {
        if (expression["$condition"]) {
            // single condition
            result = matchHeaderAndEvaluate(new RegExp(expression["$condition"]), expression["$range"], expression["$output"], $header, $value);
        } else if (expression["$conditions"]) {
            // multiple conditions
            var conditions = expression["$conditions"];
            for (i = 0; i < conditions.length; i++) {
                var condition = evaluate(conditions[i]["$condition"], $header, $value, $match, $cindex, $cheader, $cvalue);
                if (condition) {
                    result = evaluate(conditions[i]["$output"], $header, $value, $match, $cindex, $cheader, $cvalue);
                    break;
                }
            }
        } else {
            // a regular object
            result = {};
            for (var key in expression) {
                if (expression.hasOwnProperty(key)) {
                    if (["$template", "$delimiter", "$hasHeader"].indexOf(key) == -1) {
                        e = expression[key];
                        r = evaluate(e, $header, $value, $match, $cindex, $cheader, $cvalue);
                        result[key] = r;
                    }
                }
            }
        }
    }

    return result;
}
