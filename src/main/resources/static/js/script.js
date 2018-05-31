var globalEditor1 = null;
var globalMergeEditor = null;
var widgets = [];
var timeout;
var app = angular.module('myApp', []);
var previousValue;
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
app.config(['$locationProvider', function($locationProvider) {
    $locationProvider.html5Mode({
        enabled: true,
        requireBase: false
    })
}])
app.controller('OrderFormController', function($scope, $http, $filter, $window, $location) {
    console.log('$scope.selectedName -> ' + $scope.selectedName);
    document.getElementById('saveBtn').style.visibility = 'hidden';
    var namesFromOption = [];
    $scope.isPaneShown = true;
    $http.get("/getCurrentUser").then(userCallback, userErrorCallback);
    function userCallback(response) {
        if (response.data.error && (response.data.error.indexOf('Bad_OAuth_Token') || response.data.error.indexOf('No cookies found'))) {
            alert(response.data.error + ', Please relogin!');
            $window.location.href = '/index.html';
        } else {
            $scope.currentUser = response.data;
            var x = document.getElementById("snackbar");
            x.innerHTML = 'Welcome ' + response.data.display_name;
            x.className = "show";
            // After 3 seconds, remove the show class from DIV
            setTimeout(function() {
                x.className = x.className.replace("show", "");
            }, 5000);
        }
    }

    function userErrorCallback(error) {
        var x = document.getElementById("snackbar");
        x.innerHTML = error;
        x.className = "show";
        // After 3 seconds, remove the show class from DIV
        setTimeout(function() {
            x.className = x.className.replace("show", "");
        }, 10000);
    }
    /*$http.get("/getCurrentUser").success(function(data) {

    }).error(function(data) {

    });*/
    $http.post("/getAllApexClasses").then(classesCallback, classesErrorCallback);

    function classesCallback(response) {
        var foundClass = [];
        if (response.data) {
            for (var index = 0; index < response.data.length; ++index) {
                foundClass.push(response.data[index]);
                namesFromOption.push(response.data[index].name)
            }
            $scope.names = foundClass;
            $scope.isPaneShown = false;
        }
        var paramValue = $location.search();
        if (paramValue.name) {
            var newSelectedValue = {};
            if (angular.isUndefined(newSelectedValue.id)) {
                newSelectedValue = paramValue;
            }
            var possibleNewValues = $filter('filter')($scope.names, {
                name: newSelectedValue.name
            }, true);
            $scope.selectedName = possibleNewValues[0];
        }
    }

    function classesErrorCallback(error) {
        var x = document.getElementById("snackbar");
        x.innerHTML = error;
        x.className = "show";
        // After 3 seconds, remove the show class from DIV
        setTimeout(function() {
            x.className = x.className.replace("show", "");
        }, 10000);
        $scope.isPaneShown = false;
    }
    /*$scope.openInNewTab = function(event) {
        if (event.ctrlKey) {
            alert("ctrl clicked")
        }
    }*/
    $scope.retrieveSelectedClass = function(newValue, oldValue) {
        var windowsEvent = $window;
        if(windowsEvent.event.ctrlKey){
            $window.open('/html/apexEditor.html/?name='+newValue.name,'_blank');

        }
        $scope.isPaneShown = true;
        if ($scope.selectedName === undefined) {
            $scope.isPaneShown = false;
            return;
        }
        if ($scope.selectedName.groupName === 'Create New') {
            if (globalEditor1) {
                if (!globalEditor1.isClean()) {
                    var r = confirm("You have unsaved changes, are you sure you want to proceed ?");
                    if (r != true) {
                        var oldValueSelected = {};
                        if (angular.isUndefined(oldValueSelected.id) && oldValue.indexOf('"id"') !== -1) {
                            oldValueSelected = JSON.parse(oldValue);
                        }
                        var possibleOldValues = $filter('filter')($scope.names, {
                            id: oldValueSelected.id
                        }, true);
                        $scope.selectedName = possibleOldValues[0];
                        $scope.isPaneShown = false;
                        return;
                    }
                }
            }
            $scope.isPaneShown = false;
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
                    if ($.inArray(value, namesFromOption) > -1) {
                        var x = document.getElementById("snackbar");
                        x.innerHTML = "Class with same name already exists";
                        x.className = "show";
                        // After 4 seconds, remove the show class from DIV
                        setTimeout(function() {
                            x.className = x.className.replace("show", "");
                        }, 4000);
                        return;
                    }
                    $scope.isPaneShown = true;
                    $http.post("/createFile", value).then(createFileCallback, createFileErrorCallback);

                    function createFileCallback(response) {
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
                                editor.markClean();
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
                            document.getElementById('saveBtn').style.visibility = 'visible';
                            $scope.isPaneShown = false;
                        }
                    }

                    function createFileErrorCallback(error) {
                        $scope.isPaneShown = false;
                        var x = document.getElementById("snackbar");
                        x.innerHTML = error;
                        x.className = "show";
                        // After 3 seconds, remove the show class from DIV
                        setTimeout(function() {
                            x.className = x.className.replace("show", "");
                        }, 10000);
                    }
                }
            })
        } else {
            if (globalEditor1) {
                if (!globalEditor1.isClean()) {
                    var r = confirm("You have unsaved changes, are you sure you want to proceed ?");
                    if (r != true) {
                        var oldValueSelected = {};
                        if (angular.isUndefined(oldValueSelected.id) && oldValue.indexOf('"id"') !== -1) {
                            oldValueSelected = JSON.parse(oldValue);
                        }
                        var possibleOldValues = $filter('filter')($scope.names, {
                            id: oldValueSelected.id
                        }, true);
                        $scope.selectedName = possibleOldValues[0];
                        $scope.isPaneShown = false;
                        return;
                    }
                }
            }
            var data = {
                apexClassName: $scope.selectedName.name
            };
            var config = {
                params: data
            };
            $http.get("/getApexBody", config).then(getApexBodyCallback, getApexBodyErrorCallback);

            function getApexBodyCallback(response) {
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
                        editor.markClean();
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
                    document.getElementById('saveBtn').style.visibility = 'visible';
                    $scope.isPaneShown = false;
                }
            }

            function getApexBodyErrorCallback(error) {
                //error code
            }
            /*$http.get("/getApexBody", config).then(function(response) {

            });*/
        }
    }
    $scope.postdata = function(apexClassWrapper) {
        $scope.isPaneShown = true;
        apexClassWrapper.body = globalEditor1.getValue();
        var dataObj = {
            name: apexClassWrapper.name,
            body: apexClassWrapper.body,
            id: apexClassWrapper.id,
            originalBodyFromOrg: apexClassWrapper.originalBodyFromOrg
        };
        $http.post('/modifyApexBody', dataObj).then(modifyCallback, modifyErrorCallback);

        function modifyCallback(response) {
            $scope.apexClassWrapper = response.data;
            var errors = response.data.pmdStructures;
            if (Object.keys(errors).length > 0) {
                $scope.isPaneShown = false;
                if (response.data.isCompilationError) {
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
                    $('.code-helper').select2({
                        disabled: true
                    });
                    $('#myModal').modal('show');
                    document.getElementById('saveBtn').style.visibility = 'hidden';
                }
            } else {
                $scope.isPaneShown = false;
                for (var i = 0; i < widgets.length; ++i) {
                    globalEditor1.removeLineWidget(widgets[i]);
                }
                $scope.errorDetails = 'No errors';
                $('.code-helper').select2({
                    disabled: true
                });
                $('#myModalWithoutError').modal('show');
                document.getElementById('saveBtn').style.visibility = 'hidden';
            }
        }

        function modifyErrorCallback(error) {
            $scope.isPaneShown = false;
            var x = document.getElementById("snackbar");
            x.innerHTML = data;
            x.className = "show";
            // After 3 seconds, remove the show class from DIV
            setTimeout(function() {
                x.className = x.className.replace("show", "");
            }, 10000);
        }
        //$http.post("/modifyApexBody", dataObj).success(function(data) {}).error(function(data) {});
    };
    $scope.deployWithErrors = function(apexClassWrapper) {
        $scope.isPaneShown = true;
        $('#myModal').modal('hide');
        $('#myModalWithoutError').modal('hide');
        $('.code-helper').select2({
            disabled: true
        });
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
        $http.post('/saveModifiedApexBody', dataObj).then(saveCallback, saveErrorCallback);

        function saveCallback(response) {
            if (response.data.dataNotMatching) {
                $scope.isPaneShown = false;
                $scope.apexClassWrapper = response.data;
                $('.code-helper').select2({
                    disabled: true
                });
                $('#diffView').modal('show');
                var value, orig1, orig2, dv, hilight = true;
                orig1 = response.data.body;
                orig2 = response.data.modifiedApexClassWrapper.body;
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
                }, 100);
            } else {
                $('.code-helper').select2({
                    disabled: false
                });
                $scope.isPaneShown = false;
                console.log('Success : ' + response.data);
                var x = document.getElementById("snackbar");
                x.innerHTML = "Saved Successfully !";
                globalEditor1.markClean();
                x.className = "show";
                // After 3 seconds, remove the show class from DIV
                setTimeout(function() {
                    x.className = x.className.replace("show", "");
                }, 3000);
                document.getElementById('saveBtn').style.visibility = 'visible';
            }
        }

        function saveErrorCallback(error) {
            $scope.isPaneShown = false;
            var x = document.getElementById("snackbar");
            x.innerHTML = data;
            x.className = "show";
            // After 3 seconds, remove the show class from DIV
            setTimeout(function() {
                x.className = x.className.replace("show", "");
            }, 10000);
        }
    }
    $scope.replaceMerged = function() {
        $('.code-helper').select2({
            disabled: false
        });
        globalEditor1.getDoc().setValue(globalMergeEditor.editor().getValue());
    };
    $('#myModal').on('hidden.bs.modal', function() {
        $('.code-helper').select2({
            disabled: false
        });
        document.getElementById('saveBtn').style.visibility = 'visible';
    })
    $('#myModalWithoutError').on('hidden.bs.modal', function() {
        $('.code-helper').select2({
            disabled: false
        });
        document.getElementById('saveBtn').style.visibility = 'visible';
    })
    $('#diffView').on('hidden.bs.modal', function() {
        $('.code-helper').select2({
            disabled: false
        });
        document.getElementById('saveBtn').style.visibility = 'visible';
    })
});

function testAnim(x) {
    $('.modal .modal-dialog').attr('class', 'modal-dialog  ' + x + '  animated');
};
$(document).ready(function() {
    $('.code-helper').select2({
        placeholder: 'Select a command to begin'
    });
});
app.directive('loadingPane', function($timeout, $window) {
    return {
        restrict: 'A',
        link: function(scope, element, attr) {
            var directiveId = 'loadingPane';
            var targetElement;
            var paneElement;
            var throttledPosition;

            function init(element) {
                targetElement = element;
                paneElement = angular.element('<div>');
                paneElement.addClass('loading-pane');
                if (attr['id']) {
                    paneElement.attr('data-target-id', attr['id']);
                }
                var spinnerImage = angular.element('<div>');
                spinnerImage.addClass('spinner-image');
                spinnerImage.appendTo(paneElement);
                angular.element('body').append(paneElement);
                setZIndex();
                //reposition window after a while, just in case if:
                // - watched scope property will be set to true from the beginning
                // - and initial position of the target element will be shifted during page rendering
                $timeout(position, 100);
                $timeout(position, 200);
                $timeout(position, 300);
                throttledPosition = _.throttle(position, 50);
                angular.element($window).scroll(throttledPosition);
                angular.element($window).resize(throttledPosition);
            }

            function updateVisibility(isVisible) {
                if (isVisible) {
                    show();
                } else {
                    hide();
                }
            }

            function setZIndex() {
                var paneZIndex = 500;
                paneElement.css('zIndex', paneZIndex).find('.spinner-image').css('zIndex', paneZIndex + 1);
            }

            function position() {
                paneElement.css({
                    'left': targetElement.offset().left,
                    'top': targetElement.offset().top - $(window).scrollTop(),
                    'width': targetElement.outerWidth(),
                    'height': targetElement.outerHeight()
                });
            }

            function show() {
                paneElement.show();
                position();
            }

            function hide() {
                paneElement.hide();
            }
            init(element);
            scope.$watch(attr[directiveId], function(newVal) {
                updateVisibility(newVal);
            });
            scope.$on('$destroy', function cleanup() {
                paneElement.remove();
                $(window).off('scroll', throttledPosition);
                $(window).off('resize', throttledPosition);
            });
        }
    };
});