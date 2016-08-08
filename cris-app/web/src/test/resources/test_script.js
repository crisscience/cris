/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


/* global Java */

function hello() {
    return "world";
}

/* Count items present in an object
 * 
 */
function countLength(obj) {
    //print("Entered count length function");
    var count = 0;
    for (var k in obj) {
        count++;
    }
    return count;
}


/* Display all data for deeply nested objects
 * 
 */
function display(obj1) {
    for (var k in obj1) {
        print(k);
        print("Type of k is" + typeof (k));
        print(obj1[k]);
        print("Type of object value of k is" + typeof (obj1[k]));
        print("*********************");
        if (typeof (obj1) === 'object') {
            display(obj1[k]);
        }
    }
}

/*
 Function to compare deep nested list of objects or array of deep nested objects: Format [{},{},{}]
 This code handles any position reordering within the array elements
 
 
 function compareList(expectedObject, resultObject)
 {
 var HashMap = Java.type("java.util.HashMap");
 var recursionRound = false;
 var parentKey;
 var destinationKey;
 var sourceLength = 0;
 var destinationLength = 0;
 var compareposition = 0;
 var status;
 var destinationNodeFree;
 var pairMatchStatus;
 status = new HashMap();
 destinationNodeFree = new HashMap();
 pairMatchStatus = new HashMap();
 
 function compareinner(expectedObject, resultObject) {
 if (!recursionRound) {
 for (var keyExpected in expectedObject) {
 status.put(keyExpected, "false");
 }
 for (var keyResult in resultObject) {
 destinationNodeFree.put(keyResult, "true");
 }
 sourceLength = expectedObject.length;
 destinationLength = resultObject.length;
 recursionRound = true;
 }
 if (typeof (expectedObject) !== typeof (resultObject)) {
 return false;
 }
 else if (expectedObject instanceof Array && !(resultObject instanceof Array)) {
 return false;
 }
 else if (resultObject instanceof Array && !(expectedObject instanceof Array)) {
 return false;
 }
 if (typeof (expectedObject) === 'object' && typeof (resultObject) === 'object') {
 
 if (expectedObject.length !== resultObject.length) {
 return false;
 }
 //iterate through each element in expected
 for (var keyExpected in expectedObject) {
 for (var keyResult in resultObject) {
 if ((keyExpected === keyResult) && typeof (keyExpected) !== 'number') {
 var output;
 output = compareObject(expectedObject[keyExpected], resultObject[keyResult]);
 if (!output) {
 //print("Mismatch found"); 
 }
 }
 else if (typeof (keyExpected) === 'number') {
 if (keyExpected === 0 && keyResult === 0) {
 parentKey = keyExpected;
 destinationKey = keyResult;
 }
 //Check if top level pair has changed
 if (parentKey !== keyExpected || destinationKey !== keyResult) {
 if (pairMatchStatus.size() > 0) {
 var mapSize = 0;
 for (var item in pairMatchStatus) {
 if (pairMatchStatus[item] === "true") {
 mapSize++;
 }
 }
 if (mapSize === pairMatchStatus.size()) {
 status.put(parentKey, "true");
 destinationNodeFree.put(destinationKey, "false");
 }
 pairMatchStatus = new HashMap();
 destinationKey = keyResult;
 }
 else {
 destinationKey = keyResult;
 }
 }
 if (parentKey !== keyExpected) {
 parentKey = keyExpected;
 }
 compareposition++;
 if (status.get(keyExpected) === "false" && destinationNodeFree.get(keyResult) === "true") {
 compareinner(expectedObject[keyExpected], resultObject[keyResult]);
 }
 }
 }
 }
 var count = 0;
 if (compareposition === (sourceLength * destinationLength)) {
 if (pairMatchStatus.size() > 0) {
 var mapSize = 0;
 for (var item in pairMatchStatus) {
 if (pairMatchStatus[item] === "true") {
 mapSize++;
 }
 }
 if (mapSize === pairMatchStatus.size()) {
 status.put(parentKey, "true");
 destinationNodeFree.put(destinationKey, "false");
 }
 pairMatchStatus = new HashMap();
 }
 for (var item in status) {
 if (status[item] === "false") {
 return false;
 }
 else {
 count++;
 }
 }
 if (count === status.size()) {
 count = 0;
 return true;
 }
 }
 }
 else {
 if (typeof (expectedObject) !== typeof (resultObject)) {
 pairMatchStatus.put(expectedObject.toString() + "," + resultObject.toString(), "false");
 }
 else {
 pairMatchStatus.put(expectedObject.toString() + "," + resultObject.toString(), "true");
 }
 return expectedObject.toString().trim() === resultObject.toString().trim();
 }
 }
 var finalcompare = compareinner(expectedObject, resultObject);
 if (finalcompare) {
 return true;
 } else {
 return false;
 }
 }
 */

/* Function to compare deep nested objects of type : {} */

function compareObject(expectedObject, resultObject) {
    //Consider type of both objects
    if (typeof (expectedObject) !== typeof (resultObject)) {
        return false;
    }
    else if (expectedObject instanceof Array && !(resultObject instanceof Array)) {
        return false;
    }
    else if (resultObject instanceof Array && !(expectedObject instanceof Array)) {
        return false;
    }
    if (typeof (expectedObject) === 'object' && typeof (resultObject) === 'object') {
        /* Check length of both objects */
        if (expectedObject.length !== resultObject.length) {
            //print("Length mismatch");
            return false;
        }
        for (var key in expectedObject) {
            if (expectedObject[key] === null && resultObject[key] === null) {
                continue;
            }
            else
            if (expectedObject[key] === null || resultObject[key] === null) {
                return false;
            }
            else {
                var output;
                if (typeof (expectedObject[key]) === 'object' && typeof (resultObject[key]) === 'object') {
                    output = compareObject(expectedObject[key], resultObject[key]);
                }
                else {
                    output = expectedObject[key].toString().trim() === resultObject[key].toString().trim();
                }
            }
            if (!output) {
                return false;
            }
        }
        return true;
    }
    else {
        return expectedObject.toString().trim() === resultObject.toString().trim();
    }
}

/* Function to compare any type of deep nested objects, Keyword exposed to user on spreadsheet  
 * 1. Handles deep nested array of objects or plain objects
 * 2. Handles primitive type comparisons
 * 
 */
function equals(expectedObject, resultObject) {
    return compareObject(expectedObject, resultObject);
}

/* Function to check if query expression contains the common meta fields
 * "hplc_id", "hplc_name", "hplc_owner", "hplc_make", "hplc_model", "hplc_serialnum", "hplc_description" 
 *
 */
function commonMetaFields(resultObject) {
    //Set variables here

    var initialize = false;
    var HashMap = Java.type("java.util.HashMap");
    var metaFieldsMapStatus;
    metaFieldsMapStatus = new HashMap();

    function findInnerfields(resultObject) {

        if (!initialize) {
            metaFieldsMapStatus.put("hplc_id", "false");
            metaFieldsMapStatus.put("hplc_name", "false");
            metaFieldsMapStatus.put("hplc_owner", "false");
            metaFieldsMapStatus.put("hplc_make", "false");
            metaFieldsMapStatus.put("hplc_model", "false");
            metaFieldsMapStatus.put("hplc_serialnum", "false");
            metaFieldsMapStatus.put("hplc_description", "false");
            initialize = true;
        }
        //Iterate through each element in the object

        for (var key in resultObject) {
            if (resultObject[key] === null && resultObject[key] === null) {
                continue;
            }
            else
            if (resultObject[key] === null || resultObject[key] === null) {
                return false;
            }
            else {
                //compare depending on type if list or object

                if (typeof (resultObject[key]) === 'object') {
                    findInnerfields(resultObject[key]);
                }
                else {
                    //Primitive key value comparison
                    if (key === 'field') {
                        //check if value is present in map
                        print("field key found");
                        if (metaFieldsMapStatus.containsKey(resultObject[key])) {
                            print("Match found for key" + resultObject[key]);
                            metaFieldsMapStatus.put(resultObject[key], "true");
                        }
                        else {
                            print("Not matching key is " + resultObject[key]);
                        }
                    }
                }
            }

        }
        print("check final status");
        var count;
        for (var item in metaFieldsMapStatus) {
            if (metaFieldsMapStatus[item] === "false") {
                print("Some meta field did not match");
                return false;
            }
            else {
                count++;
            }
        }
        if (count === metaFieldsMapStatus.size()) {
            print("All meta fields matched");
            return true;
        }
    }
    var finalcompare;
    finalcompare = findInnerfields(resultObject);
    if (finalcompare) {
        return true;
    } else {
        return false;
    }
}
/*
 * Function to check if query expression contains value for type "field"
 * Supporting function for containsFields when entire list of fields has to be compared
 */
function containsField(resultObject, field) {

    for (var key in resultObject) {
        if (resultObject[key] === null) {
            continue;
        }
        else {
            var output;
            if (typeof (resultObject[key]) === 'object') {
                output = containsField(resultObject[key], field);
            }
            else {
                if (key === 'field') {
                    if (resultObject[key].toString().trim() === field.toString().trim()) {
                        output = true;
                    }
                }
            }
        }
        if (output) {
            return true;
        }
    }
    return false;
}

/*
 * Function to check list of fields are present in object
 */
function containsFieldList(resultObject, fieldList) {
    var result;
    for (var keyField in fieldList) {
        result = containsField(resultObject, fieldList[keyField]);
        if (!result) {
            return false;
        }
    }
    return true;
}








