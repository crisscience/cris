importPackage(java.io);
importPackage(java.lang);
var reader = new BufferedReader( new InputStreamReader(System['in']));
var data = JSON.parse(reader.readLine());
var json = data["ae8bd3c0-73cf-11e2-bcfd-0800200c9a66"];
var jsonOut = {};
if (json.length === 2) {
    jsonIn = json[1];
    if ((parseFloat(jsonIn.age) + parseFloat(jsonIn.temp)) > 40){
        jsonOut.isValid = "true";
    }
    else{
        jsonOut.isValid = "false";
    }
    jsonOut.message = "age + temp = " + ((parseFloat(jsonIn.age) + parseFloat(jsonIn.temp)));
} else {
    jsonOut.isValid = "false";
    jsonOut.message = "number of records: " + json.length;
}
print(JSON.stringify(jsonOut));
java.lang.System.exit(0);

