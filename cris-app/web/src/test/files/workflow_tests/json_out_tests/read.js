importPackage(java.io);
importPackage(java.lang);
var reader = new BufferedReader( new InputStreamReader(System['in']));
jsonIn = new String(reader.readLine());
if (jsonIn == "empty_json") {
    print("{\"a\":\"1\", \"isValid\":\"true\"}");
}
if (jsonIn == "single_json") {
    var jsonOut = {};
    jsonOut.a = 1;
    jsonOut.isValid = "true";
    print(JSON.stringify(jsonOut));
}
if (jsonIn == "multi_json") {
    var jsonOut = {};
    jsonOut.a = 1;
    jsonOut.b = "123";
    jsonOut.isValid = "true";
    print(JSON.stringify(jsonOut));
}
if (jsonIn == "extra_colon") {
    print("{\"a\"::1, \"isValid\":\"true\"}");
}
if (jsonIn == "missing_colon") {
    print("{\"a\"1, \"isValid\":\"true\"}");
}
java.lang.System.exit(0);

