importPackage(java.io);
importPackage(java.lang);

var reader = new BufferedReader( new InputStreamReader(System['in']));

var jsonOut = {};
jsonOut.isEmpty = "false";
jsonOut.isSingle = "false";
jsonOut.isMulti = "false";
jsonOut.jsonString = new String(reader.readLine());

try {
    if (jsonOut.jsonString == null) {
        jsonOut.isEmpty = "true";
    } else {
        if (jsonOut.jsonString == "{}") {
            jsonOut.isEmpty = "true";
        } else {
            var jsonIn = {};
            jsonIn = JSON.parse(jsonOut.jsonString);
            if (jsonIn.a) {
                if (parseFloat(jsonIn.a) === 1) {
                    jsonOut.isSingle = "true";
                }
            }
            if (jsonIn.a && jsonIn.b) {
                if (parseFloat(jsonIn.a) === 2 && jsonIn.b === "123") {
                    jsonOut.isMulti = "true";
                }
            }
        }
    }
} catch (exception) {
    if (exception.toString().indexOf("SyntaxError: ") !== -1) {
        jsonOut.errorMessage = "Invalid JSON input";
    }
}
print(JSON.stringify(jsonOut));
java.lang.System.exit(0);

