var globalEditor1 = null;
var globalMergeEditor = null;
var widgets = [];
var timeout;
var app = angular.module('myApp', []);
var ExcludedIntelliSenseTriggerKeys = {
    "8": "backspace",
    "9": "tab",
    "13": "enter",
    "16": "shift",
    "17": "ctrl",
    "18": "alt",
    "19": "pause",
    "20": "capslock",
    "27": "escape",
    "33": "pageup",
    "34": "pagedown",
    "35": "end",
    "36": "home",
    "37": "left",
    "38": "up",
    "39": "right",
    "40": "down",
    "45": "insert",
    "46": "delete",
    "90": "ctrl-z",
    "91": "left window key",
    "92": "right window key",
    "93": "select",
    "107": "add",
    "109": "subtract",
    "110": "decimal point",
    "111": "divide",
    "112": "f1",
    "113": "f2",
    "114": "f3",
    "115": "f4",
    "116": "f5",
    "117": "f6",
    "118": "f7",
    "119": "f8",
    "120": "f9",
    "121": "f10",
    "122": "f11",
    "123": "f12",
    "144": "numlock",
    "145": "scrolllock",
    "186": "semicolon",
    "187": "equalsign",
    "188": "comma",
    "189": "dash",
    "190": "period",
    "191": "slash",
    "192": "graveaccent",
    "220": "backslash",
    "222": "quote"
}
app.controller('OrderFormController', function($scope, $http) {
    document.getElementById("saveBtn").disabled = true;
    $http.post("/getAllApexClasses").success(function(data) {
        var foundClass = [];
        for (var index = 0; index < data.length; ++index) {
            foundClass.push(data[index]);
        }
        $scope.names = foundClass;
    }).error(function(data) {
        var x = document.getElementById("snackbar");
        x.innerHTML = data;
        x.className = "show";
        // After 3 seconds, remove the show class from DIV
        setTimeout(function() {
            x.className = x.className.replace("show", "");
        }, 10000);
    });
    $scope.retrieveSelectedClass = function() {
        if ($scope.selectedName === undefined) {
            return;
        }
        if ($scope.selectedName.groupName === 'Create New') {
            bootbox.prompt({
                title: 'Enter Class Name',
                placeholder: 'Enter Class Name',
                buttons: {
                    confirm: {
                        label: 'Create'
                    }
                },
                callback: function(value) {
                    if (value == null) {
                        return;
                    }
                    var allNames = $scope.names;
                    for(var eachName in allNames){
                        $.inArray(value, eachName.name)
                    };
                    $http.post("/createFile", value).success(function(data) {
                        if (data) {
                            $scope.apexClassWrapper = data;
                            if (globalEditor1) {
                                globalEditor1.toTextArea();
                            }
                            setTimeout(function(test) {
                                var editor = CodeMirror.fromTextArea(document.getElementById('apexBody'), {
                                    lineNumbers: true,
                                    matchBrackets: true,
                                    styleActiveLine: true,
                                    extraKeys: {
                                        ".": function(editor) {
                                            setTimeout(function() {
                                                editor.execCommand("autocomplete");
                                            }, 100);
                                            throw CodeMirror.Pass; // tell CodeMirror we didn't handle the key
                                        }
                                    },
                                    gutters: ["CodeMirror-lint-markers"],
                                    lint: true,
                                    mode: "text/x-apex"
                                });
                                editor.on("keyup", function(cm, event) {
                                    var keyCode = event.keyCode || event.which;
                                    if (!ExcludedIntelliSenseTriggerKeys[(event.keyCode || event.which).toString()]) {
                                        if (timeout) clearTimeout(timeout);
                                        timeout = setTimeout(function() {
                                            editor.showHint({
                                                hint: CodeMirror.hint.auto,
                                                completeSingle: false
                                            });
                                        }, 150);
                                    }
                                });
                                globalEditor1 = $('.CodeMirror')[0].CodeMirror;
                            }), 2000
                        }
                    }).error(function(data) {
                        var x = document.getElementById("snackbar");
                        x.innerHTML = data;
                        x.className = "show";
                        // After 3 seconds, remove the show class from DIV
                        setTimeout(function() {
                            x.className = x.className.replace("show", "");
                        }, 10000);
                    });
                }
            });
        } else {
            var data = {
                apexClassName: $scope.selectedName.name
            };
            var config = {
                params: data
            };
            $http.get("/getApexBody", config).then(function(response) {
                if (response.data) {
                    $scope.apexClassWrapper = response.data;
                    if (globalEditor1) {
                        globalEditor1.toTextArea();
                    }
                    setTimeout(function(test) {
                        var editor = CodeMirror.fromTextArea(document.getElementById('apexBody'), {
                            lineNumbers: true,
                            matchBrackets: true,
                            styleActiveLine: true,
                            extraKeys: {
                                ".": function(editor) {
                                    setTimeout(function() {
                                        editor.execCommand("autocomplete");
                                    }, 100);
                                    throw CodeMirror.Pass; // tell CodeMirror we didn't handle the key
                                }
                            },
                            gutters: ["CodeMirror-lint-markers"],
                            lint: true,
                            mode: "text/x-apex"
                        });
                        editor.on("keyup", function(cm, event) {
                            var keyCode = event.keyCode || event.which;
                            if (!ExcludedIntelliSenseTriggerKeys[(event.keyCode || event.which).toString()]) {
                                if (timeout) clearTimeout(timeout);
                                timeout = setTimeout(function() {
                                    editor.showHint({
                                        hint: CodeMirror.hint.auto,
                                        completeSingle: false
                                    });
                                }, 150);
                            }
                        });
                        globalEditor1 = $('.CodeMirror')[0].CodeMirror;
                    }), 2000
                }
            });
        }
    }
    $scope.postdata = function(apexClassWrapper) {
        console.log(apexClassWrapper);
        apexClassWrapper.body = globalEditor1.getValue();
        var dataObj = {
            name: apexClassWrapper.name,
            body: apexClassWrapper.body,
            id: apexClassWrapper.id,
            originalBodyFromOrg: apexClassWrapper.originalBodyFromOrg
        };
        $http.post("/modifyApexBody", dataObj).success(function(data) {
            $scope.apexClassWrapper = data;
            var errors = data.pmdStructures;
            if (Object.keys(errors).length > 0) {
                if (data.isCompilationError) {
                    for (var i = 0; i < widgets.length; ++i) {
                        globalEditor1.removeLineWidget(widgets[i]);
                    }
                    widgets.length = 0;
                    for (var i = 0; i < errors.length; ++i) {
                        var err = errors[i];
                        if (!err) continue;
                        var msg = document.createElement("div");
                        var icon = msg.appendChild(document.createElement("span"));
                        icon.innerHTML = "!!";
                        icon.className = "lint-error-icon";
                        msg.appendChild(document.createTextNode(err.reviewFeedback));
                        msg.className = "lint-error";
                        widgets.push(globalEditor1.addLineWidget(err.lineNumber - 1, msg, {
                            coverGutter: true,
                            noHScroll: true
                        }));
                    }
                } else {
                    for (var i = 0; i < widgets.length; ++i) {
                        globalEditor1.removeLineWidget(widgets[i]);
                    }
                    $scope.errorDetails = errors;
                    $('#myModal').modal('show');
                }
            } else {
                for (var i = 0; i < widgets.length; ++i) {
                    globalEditor1.removeLineWidget(widgets[i]);
                }
                $scope.errorDetails = 'No errors';
                $('#myModalWithoutError').modal('show');
            }
        }).error(function(data) {
            var x = document.getElementById("snackbar");
            x.innerHTML = data;
            x.className = "show";
            // After 3 seconds, remove the show class from DIV
            setTimeout(function() {
                x.className = x.className.replace("show", "");
            }, 10000);
        });
    };
    $scope.deployWithErrors = function(apexClassWrapper) {
        $('#myModal').modal('hide');
        $('#myModalWithoutError').modal('hide');
        var cleaneddata = globalEditor1.getValue().replace(new RegExp(' +', 'g'), ' ');
        globalEditor1.getDoc().setValue(cleaneddata);
        globalEditor1.setSelection({
            'line': globalEditor1.firstLine(),
            'ch': 0,
            'sticky': null
        }, {
            'line': globalEditor1.lastLine(),
            'ch': 0,
            'sticky': null
        }, {
            scroll: false
        });
        //auto indent the selection
        globalEditor1.indentSelection("smart");
        globalEditor1.setCursor({
            line: globalEditor1.firstLine(),
            ch: 0
        })
        apexClassWrapper.body = globalEditor1.getValue();
        var dataObj = {
            name: apexClassWrapper.name,
            body: apexClassWrapper.body,
            id: apexClassWrapper.id,
            //originalBodyFromOrg: apexClassWrapper.originalBodyFromOrg
            originalBodyFromOrg: globalMergeEditor != undefined ? globalMergeEditor.rightOriginal().getValue() : apexClassWrapper.originalBodyFromOrg
        };
        $http.post("/saveModifiedApexBody", dataObj).success(function(data) {
            if (data.dataNotMatching) {
                $scope.apexClassWrapper = data;
                $('#diffView').modal('show');
                var value, orig1, orig2, dv, hilight = true;
                orig1 = data.body;
                orig2 = data.modifiedApexClassWrapper.body;
                var target = document.getElementById("mergemodal");
                target.innerHTML = "";
                setTimeout(function() {
                    dv = CodeMirror.MergeView(target, {
                        value: orig1,
                        origLeft: null,
                        orig: orig2,
                        lineNumbers: true,
                        mode: "text/x-apex",
                        highlightDifferences: hilight
                    });
                    globalMergeEditor = dv;
                }, 1000);
            }
            console.log('Success : ' + data);
            var x = document.getElementById("snackbar");
            x.innerHTML = "Saved Successfully !";
            x.className = "show";
            // After 3 seconds, remove the show class from DIV
            setTimeout(function() {
                x.className = x.className.replace("show", "");
            }, 3000);
        }).error(function(data) {
            var x = document.getElementById("snackbar");
            x.innerHTML = data;
            x.className = "show";
            // After 3 seconds, remove the show class from DIV
            setTimeout(function() {
                x.className = x.className.replace("show", "");
            }, 10000);
        });
    }
    $scope.replaceMerged = function() {
        globalEditor1.getDoc().setValue(globalMergeEditor.editor().getValue());
    };
    $scope.replaceSpaceWithTabs = function() {
        var cleaneddata = globalEditor1.getValue().replace(new RegExp(' +', 'g'), ' ');
        globalEditor1.getDoc().setValue(cleaneddata);
    };

});

function testAnim(x) {
    $('.modal .modal-dialog').attr('class', 'modal-dialog  ' + x + '  animated');
};
$(document).ready(function() {
    $('.code-helper').select2();
});