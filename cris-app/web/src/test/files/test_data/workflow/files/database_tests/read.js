importPackage(java.io);
importPackage(java.lang);
var reader = new BufferedReader( new InputStreamReader(System['in']));
var jsonIn = JSON.parse(reader.readLine());
var jsonOut = {};
if ((parseFloat(jsonIn.age) == 40 && parseFloat(jsonIn.temp)) == 40){
    jsonOut.isValid = "true";
} else {
    jsonOut.isValid = "false";
}
print(JSON.stringify(jsonOut));
java.lang.System.exit(0);

