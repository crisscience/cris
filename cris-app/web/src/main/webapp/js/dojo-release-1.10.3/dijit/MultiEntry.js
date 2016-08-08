// summary:
//          The MultiEnty widget allows multiple entry of items through a graphical front-end.
// description:
//          The widget acquires the input entered by the user for a selected set of items which are represented by
//          a set of items on the widget interface. This association allows users to enter values on the HTML side
//          for multiple items in a single round instead of typing in the same values for each and every item they
//          are interested in.
// returns:
//          Multientry widget object
define(["dojo/_base/lang", "dojo/_base/declare", "dojo/dom-construct", "dijit/_WidgetBase", "dojox/widget/_Invalidating", "dojox/widget/Selection", "dojox/gfx"],
    function(lang, declare, domConstruct, _WidgetBase, _Invalidating, Selection){

        return declare("MultiEntry", [_WidgetBase, _Invalidating, Selection], {

            // selectedItem:    Array
            //                  An array of the selected items on the widget
            selectedItem : new Array(),
            // shapeArray:      Array
            //                  An array of the shapes on the widget
            shapeArray : new Array(),
            // initialX:        int
            //                  The initial X position of the widget on the page
            initialX : 10,
            // initialY:        int
            //                  The initial Y position of the widget on the page
            initialY : 10,
            // width:           int
            //                  The widget width
            width : null,
            // height:          int
            //                  The widget height
            height : null,
            // maxPerRow:       int
            //                  The maximum number of shape items per row
            maxPerRow : null,
            // rootItem:        Object
            //                  The root HTML ID for the widget
            rootItem : null,
            // saveTemp:        Object
            //                  The HTML ID of the save temporary button
            saveTemp : null,
            // dataElements:    Array
            //                  The core backbone of the widget which hold the info of all saved items
            dataElements : new Array(),
            // dataElementsRequired:    Map
            //                  A map indicates whether a data element is required or optional
            dataElementsRequired : null,
            // uuid:            String
            //                  A string representing the UUID of the table the widget will post to.
            uuid : null,
            // shapeId:         String
            //                  A string representing the shape identifier from the list of items being passed
            shapeId : null,
            // baseClass:       String
            //                  The widget baseclass name
            baseClass : "multiEntry",

            // AlphaSelected:   Number, constant
            //                  The alpha value for selected cells
            AlphaSelected: 1.0,

            // AlphaNonSelected:   Number, constant
            //                    The alpha value for non-selected cells
            AlphaNonSelected: 0.4,

            constructor : function() {
                // summary:
                //          The constructor of the widget. It validates the "items", "width","height","maxPerRow","rootItem" parameters
                this.invalidatingProperties = ["dataElements", "dataElementsRequired", "width", "height", "maxPerRow", "saveTemp", "shapeId", "uuid", "rootItem"];
            },


            _objectsAreSame : function(x, y) {
                // summary:
                //          Function to compare two arrays. Returns true if they are the same, false otherwise
                // returns:
                //          true in case the objects are the same, false otherwise
                // tags:
                //          private
                var isSame = true;
                for (var propertyName in x) {
                    if (x[propertyName] !== y[propertyName]) {
                        isSame = false;
                        break;
                    }
                }
                return isSame;
            },

            getSelectedItem : function() {
                // summary:
                //          Gets the first item in the selected item list
                // returns:
                //          Object representing the first selected item
                return this.selectedItem[0];
            },

            getItem : function(i) {
                // summary:
                //          Get a specific item from the array of selected items
                // i: int
                //          The position of item in the selected items array
                // returns:
                //          An object in the position specified by i
                return this.selectedItem[i];
            },

            getDataElements : function() {
                // summary:
                //          Get the array of data elements
                // returns:
                //          An array of all data elements
                return this.dataElements;
            },

            getCompletedElements : function() {
                // summary:
                //          Get a JSON array of all completed data elements
                // returns:
                //          A JSON array of all completed data elements
                var mem = this.dataElements;
                var filtered = new Array();

                // Filter the NULL elements
                for (var j = 0 ; j < mem.length; j++) {
                    var nullFound = false;
                    var item = mem[j];

                    for (var prop in item) {
                        if ((item[prop] === null || item[prop] === "") && this.dataElementsRequired[prop]) {
                            nullFound = true;
                        }
                    }

                    if (!nullFound) {
                        filtered.push(mem[j]);
                    }
                }

                // Assign new identifiers in order to send them
                var finalSet = new Array();
                for (var l = 0; l < filtered.length; l++) {
                    var obj = new Object();
                    for (var prop in filtered[l]) {
                        var tmp = filtered[l];

                        if (tmp[prop] === null){
                            obj[prop] = "";
                        } else {
                            obj[prop] = tmp[prop];
                        }
                    }
                    finalSet.push(obj);
                }

                return finalSet;
            },

            _addSelectedItem : function(item) {
                // summary:
                //          Pushes item to the list of selected items array.
                // item: object
                //          An object representing a selected item
                // tags:
                //          private
                var index = dojo.indexOf(this.selectedItem, item);
                if (index === -1) {
                    this.selectedItem.push(item);
                }
            },

            _removeSelectedItem : function(item) {
                // summary:
                //          Removes a selected item from the list
                // item: object
                //          The object to be removed
                // tags:
                //          private
                var index = dojo.indexOf(this.selectedItem, item);
                if (index > -1) {
                    this.selectedItem.splice(index, 1);
                }
            },

            getNumberSelectedItems : function() {
                // summary:
                //          Retreives the number of selected items on the drawing surface
                // returns:
                //          The number of selected items
                return this.selectedItem.length;
            },

            _onShapeClick : function(e) {
                // summary:
                //          Called when the a shape is clicked.
                // description:
                //          This method is responsible for all the actions
                //          such as coloring of shapes based on the the current color already there.
                // e: event
                //      The event of the onClick callback
                // tags:
                //      private callback
                if (e.gfxTarget !== null) {
                    var shp = e.gfxTarget;
                    var rgba = shp.getFill().toRgba();
                    var alpha = rgba[3];
                    if (alpha === this.AlphaSelected) {
                        this._removeSelectedItem(shp.id);
                        rgba[3] = this.AlphaNonSelected;
                        this.onEventUnselected();
                    } else {
                        this.selectedItem.push(shp.id);
                        rgba[3] = this.AlphaSelected;
                        this.onEventSelected();
                    }
                    shp.setFill(rgba);
                }
            },

            _onSelectAllClick : function(shapeArray) {
                // summary:
                //          Called when the Select All Button is clicked. Selects all shapes
                // shapeArray: Array
                //          The array of all shapes
                // tags:
                //      private callback
                for (var i = 0; i < shapeArray.length; i ++) {
                    var shp = shapeArray[i];
                    this._addSelectedItem(shp.id);

                    var rgba = shp.getFill().toRgba();
                    rgba[3] = this.AlphaSelected;
                    shp.setFill(rgba);
                }

                this.onEventSelected();
            },

            _onSelectNoneClick : function(shapeArray) {
                // summary:
                //          Called when the Select All Button is clicked. Selects all shapes
                // shapeArray: Array
                //          The array of all shapes
                // tags:
                //      private callback
                for (var i = 0; i < shapeArray.length; i++) {
                    var shp = shapeArray[i];
                    this._removeSelectedItem(shp.id);

                    var rgba = shp.getFill().toRgba();
                    rgba[3] = this.AlphaNonSelected;
                    shp.setFill(rgba);
                }

                this.onEventUnselected();
            },

            _onSaveTemporary : function() {
                // summary:
                //          Used to save temporary the values entered by the user to the dataElements structure.
                for (var i = 0; i < this.getNumberSelectedItems(); i++) {
                    //var entry={};
                    var currentItem = this.getItem(i);
                    var entry;
                    var position;

                    // Get the entry
                    for (var k = 0; k < this.dataElements.length; k++) {
                        var elem = this.dataElements[k];
                        if (elem[this.shapeId] === currentItem) {
                            entry = this.dataElements[k];
                            position = k;
                            break;
                        }
                    }

                    for (var prop in entry) {
                        if (prop === this.shapeId) {
                            // Omit the ID from the property scan
                            entry[prop] = currentItem;
                        } else {
                            var v = dijit.byId(prop).getValue();
                            if (v !== "MULTIVALUE" && !!v) {
                                entry[prop] = v;
                            }
                        }
                    }

                    // Save in array
                    this.dataElements[position] = entry;

                    // figure out whether a data element is incomplete/empty
                    var notComplete = false;
                    var empty = true;
                    for (var prop in entry) {
                        var value = entry[prop];
                        if (prop !== this.shapeId) {
                            if ((value === null || value === "" || value == " ") && this.dataElementsRequired[prop]) {
                                notComplete = true;
                            } else if (value !== null && value !== "") {
                                empty = false;
                            }
                        }
                    }

                    if (!empty) {
                        // identify the cell
                        var cell = null;
                        for (var j = 0; j < this.shapeArray.length; j++) {
                            var shp = this.shapeArray[j];
                            if (shp.id === this.selectedItem[i]) {
                                cell = shp;
                                break;
                            }
                        }

                        if (cell) {
                            if (notComplete) {
                                var rgba = cell.getFill().toRgba();
                                // red for incomplete
                                rgba[0] = 255;
                                rgba[1] = 0;
                                rgba[2] = 0;
                                cell.setFill(rgba);
                            } else {
                                var rgba = cell.getFill().toRgba();
                                // green for complete
                                rgba[0] = 0;
                                rgba[1] = 255;
                                rgba[2] = 0;
                                cell.setFill(rgba);
                            }
                        }
                    }
                }
            },

            _setSelectedCompleted : function() {
                // summary:
                //          Indicates that the selected items as completed in terms of values entered
                for (var i = 0; i < this.shapeArray.length; i++) {
                    var shp = this.shapeArray[i];
                    for (var j = 0; j < this.selectedItem.length; j++) {
                        if (shp.id === this.selectedItem[j]) {
                            this.shapeArray[i].setFill([0, 255, 0, this.AlphaSelected]);
                        }
                    }
                }

                // Clear array
                //this.selectedItem = new Array();
            },

            _setSelectedIncomplete : function() {
                // summary:
                //          Indicates that the selected items as incompleted in terms of values entered
                for (var i = 0; i < this.shapeArray.length; i++) {
                    for (var j = 0; j < this.selectedItem.length; j++) {
                        var shp = this.shapeArray[i];
                        if (shp.id === this.selectedItem[j]) {
                            this.shapeArray[i].setFill([255, 0, 0, this.AlphaSelected]);
                        }
                    }
                }

                // Clear Array
                //this.selectedItem = new Array();
            },

            postCreate : function() {
                // summary:
                //		The main function used to initialize the widget
                //          config: Contains a set of configurations including (items) - The objects that will be used to create the widget.
                //          It must contain {id:<ID>,name:<name>} . Also (surfaceSize) - The surface size that we will draw the shape on.
                //          It must be specified in the following format - {width:<WIDTH>,height:<height>}. Last is (maxPerRow) - Indicates
                //          how many rectangles should be present per row.
                // nodeName:
                //          The node name to generate the widget on
                //
                this.inherited(arguments);

                //Initialize the data structures
                this.selectedItem = new Array();

                var container = null;
                var surface = null;
                this.shapeArray = new Array();

                //Define the surface that will contain the shapes
                container = this.rootItem;
                surface = dojox.gfx.createSurface(container, this.width, this.height);

                //Adjust the starting point of the whole widget on screen
                var xIncrease = this.initialX;
                var yIncrease = this.initialY;
                //Initialize the number of elements to be processed
                var numElements = 0;

                do {
                    for (var j = 0; j < this.maxPerRow; j ++) {
                        this.shapeArray[numElements] = surface.createRect({
                            x: xIncrease,
                            y: yIncrease,
                            height: 50,
                            width: 50
                        })
                        .setFill([191,191,191,this.AlphaNonSelected])
                        .setStroke({
                            color: "black",
                            width: 1
                        });

                        var elem = this.dataElements[numElements];

                        this.shapeArray[numElements].name = elem[this.shapeId];
                        this.shapeArray[numElements].id = elem[this.shapeId];

                        surface.createText({
                            x:xIncrease+5,
                            y:yIncrease+15,
                            text:elem[this.shapeId],
                            align:"start"
                        }).
                        setFont({
                            family:"Arial",
                            size:"8pt"
                        }).
                        setFill("black");

                        // Event Handler for rectangle shapes
                        this.shapeArray[numElements].connect("onclick", lang.hitch(this, this._onShapeClick));
                        xIncrease += 50;
                        numElements ++;

                        if (numElements === this.dataElements.length) {
                            break;
                        }
                    }
                    yIncrease += 50;
                    xIncrease = this.initialX;

                } while(numElements < this.dataElements.length);

                domConstruct.place("<button id=\"selectall\"></button><button id=\"selectnone\"></button>", this.rootItem, "before");

                var selectAllButton = dojo.byId('selectall');
                var selectNoneButton = dojo.byId('selectnone');

                var b1 = new dijit.form.Button({
                    label: "Select All",
                    onClick: lang.hitch(this, this._onSelectAllClick, this.shapeArray)
                }, selectAllButton);

                dojo.hitch(b1, b1.onClick);

                new dijit.form.Button({
                    label: "Select None",
                    onClick: lang.hitch(this, this._onSelectNoneClick, this.shapeArray)
                }, selectNoneButton);

                /*
                var saveTemporary = this.saveTemp;

                new dijit.form.Button({

                    label:"Save Temporary",

                    onClick: lang.hitch(this,this._onSaveTemporary)
                },saveTemporary);

                */

                var hitchedOnSaveTemporary = lang.hitch(this, this._onSaveTemporary);
                for (var prop in this.dataElements[0]) {
                    var w = dijit.byId(prop);
                    dojo.connect(w, "onChange", function() {
                        hitchedOnSaveTemporary();
                    });
                }

            },

            onEventUnselected : function() {
                // summary:
                //          Triggered once any item is selected. This method can be overwritten by the developer.
                for (var prop in this.dataElements[0]) {
                    dijit.byId(prop).setValue(null);
                }

                if (this.getNumberSelectedItems() >= 1) {
                    this._checkMultipleItems();
                }

            },

            onEventSelected : function() {
                // summary:
                //          Triggered once any item is un-selected. This method can be overwritten by the developer.
                if (this.getNumberSelectedItems() === 1) {
                    // var entry this.dataElements.get(this.getSelectedItem());
                    var entry = new Object();

                    // Get the entry from data elements
                    for (var i = 0 ; i < this.dataElements.length ; i++) {
                        var dE = this.dataElements[i];
                        if (dE[this.shapeId] === this.getSelectedItem()) {
                            entry = this.dataElements[i];
                            break;
                        }
                    }

                    // Set the values according to the data element object
                    for (var prop in this.dataElements[0]) {
                        dijit.byId(prop).setValue(entry[prop]);
                    }
                } else if (this.getNumberSelectedItems() > 1) {
                    this._checkMultipleItems();

                }
            },

            _checkMultipleItems : function() {
                // summary:
                //          Handler for selecting multiple items in terms of conflicting values.
                var list = new Array();

                // Get all selected items and insert them into a new list
                for (var j = 0; j < this.getNumberSelectedItems(); j++) {
                    var entry = new Object();
                    // Get the entry from data elements
                    for (var i = 0; i < this.dataElements.length; i++) {
                        var dE = this.dataElements[i];
                        if (dE[this.shapeId] === this.getItem(j)) {
                            entry = this.dataElements[i];
                            break;
                        }
                    }

                    list.push(entry);
                }

                // Set the values according to the list
                // if the first entry in the list is different in terms of the attribute
                // than the other entries then set the field to blank
                for (var prop in this.dataElements[0]) {
                    var temp = list[0];
                    var isBlank = false;
                    for (var k = 1; k < list.length; k++) {
                        var temp2 = list[k];

                        if (temp[prop] instanceof Date) {
                            if (dojo.date.compare(temp[prop], temp2[prop]) !== 0) {
                                isBlank = true;
                                break;
                            }
                        } else {
                            // Append other cases other than Date if any
                            if (temp[prop] !== temp2[prop]) {
                                // Set attribute to blank
                                isBlank = true;
                                break;
                            }
                        }
                    }
                    if (isBlank) {
                        if (dijit.byId(prop) instanceof dijit.form.FilteringSelect || dijit.byId(prop) instanceof dijit.form.DateTextBox) {
                            dijit.byId(prop).setValue(null);
                        } else {
                            dijit.byId(prop).setValue("MULTIVALUE");
                        }
                    } else {
                        dijit.byId(prop).setValue(temp[prop]);
                    }
                }
            },

            // Setter/Getter functions for attributes

            _setSelectedItem : function() {
                this._set("selectedItem", new Array());
            },

            _setDataElementsAttrib : function(value) {
                this._set("dataElements", value);
            },

            _setDataElementsRequiredAttrib : function(value) {
                this._set("dataElementsRequired", value);
            },

            _setWidthAttrib : function(value) {
                this._set("width", value);
            },

            _setHeightAttrib : function(value) {
                this._set("height", value);
            },

            _setMaxPerRowAttrib : function(value) {
                this._set("maxPerRow", value);
            },

            _setRootItemAttr : function(value) {
                this._set("rootItem", value);
            },

            _setSaveTempAttr : function(value) {
                this._set("saveTemp", value);
            },

            _setInitialXAttr : function(value) {
                this._set("initialX", value);
            },

            _setInitialYAttr : function(value) {
                this._set("initialY", value);
            },

            _setUuidAttr : function(value) {
                this._set("uuid", value);
            },

            _setShapeIdAttr : function(value ) {
                this._set("shapeId", value);
            }
        });
    }
);
