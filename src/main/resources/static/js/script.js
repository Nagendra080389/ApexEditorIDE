var globalEditor1 = null;
var globalMergeEditor = null;
var widgets = [];
var timeout;
var app = angular.module('myApp', []);
var previousValue;
var event = null;
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
app.directive('stringToNumber', function() {
    return {
        require: 'ngModel',
        link: function(scope, element, attrs, ngModel) {
            ngModel.$parsers.push(function(value) {
                return '' + value;
            });
            ngModel.$formatters.push(function(value) {
                return parseFloat(value);
            });
        }
    };
});
app.config(['$locationProvider', function($locationProvider) {
    $locationProvider.html5Mode({
        enabled: true,
        requireBase: false
    })
}])
app.controller('OrderFormController', function($scope, $http, $filter, $window, $location) {
    document.getElementById('saveBtn').style.visibility = 'hidden';
    var namesFromOption = [];
    var foundTheme = ['3024-day', '3024-night', 'abcdef', 'ambiance', 'ambiance-mobile', 'base16-dark', 'base16-light', 'bespin', 'blackboard', 'cobalt', 'colorforth', 'darcula', 'dracula', 'duotone-dark', 'duotone-light', 'eclipse', 'elegant', 'erlang-dark', 'gruvbox-dark', 'hopscotch', 'icecoder', 'idea', 'isotope', 'lesser-dark', 'liquibyte', 'lucario', 'material', 'mbo', 'mdn-like', 'midnight', 'monokai', 'neat', 'neo', 'night', 'oceanic-next', 'panda-syntax', 'paraiso-dark', 'paraiso-light', 'pastel-on-dark', 'railscasts', 'rubyblue', 'seti', 'shadowfox', 'solarized', 'ssms', 'the-matrix', 'tomorrow-night-bright', 'tomorrow-night-eighties', 'ttcn', 'twilight', 'vibrant-ink', 'xq-dark', 'xq-light', 'yeti', 'zenburn'];
    $scope.themeNames = foundTheme;
    if (localStorage.getItem('organization_id') && localStorage.getItem('display_name') && localStorage.getItem('displayEmail') && localStorage.getItem('domainName')) {
        var localUser = {
            email: localStorage.getItem('displayEmail'),
            display_name: localStorage.getItem('display_name'),
            domainName: localStorage.getItem('domainName'),
            orgId: localStorage.getItem('organization_id'),
            userId: localStorage.getItem('userId')
        };
        $scope.currentUser = localUser;
        ga('send', {
            hitType: 'event',
            eventCategory: 'EditorPage',
            eventAction: 'login',
            eventLabel: 'User Logged In : ' + localUser.email + ' from orgId ' + localUser.orgId
        });
        ga('send', {
            hitType: 'event',
            eventCategory: localUser.orgId,
            eventAction: 'Track user by Organisation Id',
            eventLabel: 'Org Id'
        });
        ga('send', {
            hitType: 'event',
            eventCategory: localUser.email,
            eventAction: 'Track user by User Email',
            eventLabel: 'User Email'
        });
    } else {
        $http.get("/getCurrentUser").then(userCallback, userErrorCallback);
    }

    function userCallback(response) {
        if (response.data.error && (response.data.error.indexOf('Bad_OAuth_Token') || response.data.error.indexOf('No cookies found'))) {
            alert(response.data.error + ', Please relogin!');
            $window.location.href = '/index.html';
        } else {
            $scope.currentUser = response.data;
            localStorage.setItem('organization_id', response.data.orgId);
            localStorage.setItem('display_name', response.data.display_name);
            localStorage.setItem('displayEmail', response.data.email);
            localStorage.setItem('domainName', response.data.domainName);
            localStorage.setItem('userId', response.data.userId);
            iziToast.info({
                icon: 'fa fa-user',
                title: 'Welcome ' + response.data.display_name,
                message: 'Welcome!',
                position: 'topCenter',
                layout: 2,
            });
            ga('send', {
                hitType: 'event',
                eventCategory: 'EditorPage',
                eventAction: 'login',
                eventLabel: 'User Logged In : ' + response.data.email + ' from orgId ' + response.data.orgId
            });
            ga('send', {
                hitType: 'event',
                eventCategory: response.data.orgId,
                eventAction: 'Track user by Organisation Id',
                eventLabel: 'Org Id'
            });
            ga('send', {
                hitType: 'event',
                eventCategory: response.data.email,
                eventAction: 'Track user by User Email',
                eventLabel: 'User Email'
            });
        }
    }

    function userErrorCallback(error) {
        iziToast.error({
            title: 'Error',
            message: error,
            position: 'topRight',
        });
    }
    $http.post("/getAllApexClasses").then(classesCallback, classesErrorCallback);

    function classesCallback(response) {
        var foundClass = [];
        if (response.data) {
            for (var index = 0; index < response.data.length; ++index) {
                foundClass.push(response.data[index]);
                namesFromOption.push(response.data[index].name)
            }
            $scope.names = foundClass;
        }
        iziToast.info({
            title: 'Apex Classes Loaded',
            position: 'topRight',
        });
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
    var urlNameValue = $location.search()
    if (urlNameValue && urlNameValue.name) {
        if (urlNameValue.name !== 'New Apex Class....') {
            var data = {
                apexClassName: urlNameValue.name
            };
            var config = {
                params: data
            };
            $http.get("/getApexBody", config).then(getApexBodyCallback, getApexBodyErrorCallback);
        } else {
            $('#enterClass').iziModal('open');
            $("#enterClass").on('click', '.submit', function(event) {
                event.preventDefault();
                var className = $('#classNameId').val();
                var classDesc = $('#classDescId').val();
                if (classDesc && className && className != "" && classDesc != "") {
                    if ($.inArray(className, namesFromOption) > -1) {
                        iziToast.error({
                            timeout: 5000,
                            title: 'Error',
                            message: 'Class with same name already exists',
                            position: 'topRight',
                        });
                        return;
                    }
                    var nameAndDesc = $('#classNameId').val() + '+' + $('#classDescId').val();
                    if (localStorage.getItem('display_name')) {
                        nameAndDesc = nameAndDesc + '+' + localStorage.getItem('display_name');
                    }
                    $http.post("/createFile", nameAndDesc).then(createFileCallback, createFileErrorCallback);
                    $('#enterClass').iziModal('close');
                } else {
                    var fx = "wobble",
                        $modal = $(this).closest('.iziModal');
                    //wobble shake
                    alert('Both Fields are mandatory');
                    if (!$modal.hasClass(fx)) {
                        $modal.addClass(fx);
                        setTimeout(function() {
                            $modal.removeClass(fx);
                        }, 1500);
                    }
                }
            });
        }
    }

    function classesErrorCallback(error) {
        iziToast.error({
            title: 'Error',
            message: error,
            position: 'topRight',
        });
    }

    $scope.focusCallback = function($event) {
      if($event === null) {
        return;
      }

      event = $event;
    };

    $scope.retrieveSelectedClass = function(newValue, oldValue, val) {
        var windowsEvent = $window;
        if ($scope.selectedName === undefined) {
            return;
        }
        var possibleOldValues = [];
        var oldValueSelected = {};
        if (angular.isUndefined(oldValueSelected.id) && oldValue.indexOf('"name"') !== -1) {
            oldValueSelected = JSON.parse(oldValue);
        }
        possibleOldValues = $filter('filter')($scope.names, {
            name: oldValueSelected.name
        }, true);
        if ($scope.selectedName === null) {
            return;
        }
        if ($scope.selectedName.groupName === 'Create New') {
            if (globalEditor1) {
                if (!globalEditor1.isClean()) {
                    var r = confirm("You have unsaved changes, are you sure you want to proceed ?");
                    if (r != true) {
                        $scope.selectedName = possibleOldValues[0];
                        return;
                    }
                }
                if (navigator.userAgent.indexOf("Firefox") != -1) {
                    //$window.click(function(event) {});

                } else {
                    if (windowsEvent.event.ctrlKey) {
                        windowsEvent.open('/html/apexEditor.html?name=' + newValue.name, '_blank');
                        $scope.selectedName = possibleOldValues[0];
                        return;
                    }
                }
            }
            $('#enterClass').iziModal('open');
            $("#enterClass").on('click', '.submit', function(event) {
                event.preventDefault();
                var className = $('#classNameId').val();
                var classDesc = $('#classDescId').val();
                if (classDesc && className && className != "" && classDesc != "") {
                    if ($.inArray(className, namesFromOption) > -1) {
                        iziToast.error({
                            timeout: 5000,
                            title: 'Error',
                            message: 'Class with same name already exists',
                            position: 'topRight',
                        });
                        return;
                    }
                    var nameAndDesc = $('#classNameId').val() + '+' + $('#classDescId').val();
                    if (localStorage.getItem('display_name')) {
                        nameAndDesc = nameAndDesc + '+' + localStorage.getItem('display_name');
                    }
                    $http.post("/createFile", nameAndDesc).then(createFileCallback, createFileErrorCallback);
                    $('#enterClass').iziModal('close');
                } else {
                    var fx = "wobble",
                        $modal = $(this).closest('.iziModal');
                    //wobble shake
                    alert('Both Fields are mandatory');
                    if (!$modal.hasClass(fx)) {
                        $modal.addClass(fx);
                        setTimeout(function() {
                            $modal.removeClass(fx);
                        }, 1500);
                    }
                }
            });
        } else {
            var possibleOldValues = [];
            var oldValueSelected = {};
            if (angular.isUndefined(oldValueSelected.id) && oldValue.indexOf('"name"') !== -1) {
                oldValueSelected = JSON.parse(oldValue);
            }
            possibleOldValues = $filter('filter')($scope.names, {
                name: oldValueSelected.name
            }, true);
            if (globalEditor1) {
                if (!globalEditor1.isClean()) {
                    var r = confirm("You have unsaved changes, are you sure you want to proceed ?");
                    if (r != true) {
                        $scope.selectedName = possibleOldValues[0];
                        return;
                    }
                }
            }
            if (navigator.userAgent.indexOf("Firefox") != -1) {
                    console.log($event);
                    if (event !== null && event.ctrlKey) {
                        windowsEvent.open('/html/apexEditor.html?name=' + newValue.name, '_blank');
                        $scope.selectedName = possibleOldValues[0];
                        //clickedClassName = '';
                        return;
                    }

            } else {
                if (windowsEvent.event.ctrlKey) {
                    windowsEvent.open('/html/apexEditor.html?name=' + newValue.name, '_blank');
                    $scope.selectedName = possibleOldValues[0];
                    return;
                }
            }
            var data = {
                apexClassName: $scope.selectedName.name
            };
            var config = {
                params: data
            };
            $http.get("/getApexBody", config).then(getApexBodyCallback, getApexBodyErrorCallback);
        }
        //clickedClassName = '';
    }

    function createFileCallback(response) {
        if (response.data) {
            $scope.apexClassWrapper = response.data;
            $(document).prop('title', response.data.name);
            if (globalEditor1) {
                globalEditor1.toTextArea();
            }
            setTimeout(function(test) {
                var editor = CodeMirror.fromTextArea(document.getElementById('apexBody'), {
                    lineNumbers: true,
                    matchBrackets: true,
                    lineWrapping: true,
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
                    $(document).prop('title', response.data.name + '  *');
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
                if (localStorage && localStorage.getItem('apexEditorTheme')) {
                    editor.setOption("theme", localStorage.getItem('apexEditorTheme'));
                }
                globalEditor1 = $('.CodeMirror')[0].CodeMirror;
            }), 2000
            document.getElementById('saveBtn').style.visibility = 'visible';
        }
    }

    function createFileErrorCallback(error) {
        iziToast.error({
            title: 'Error',
            message: error,
            position: 'topRight',
        });
    }

    function getApexBodyCallback(response) {
        if (response.data) {
            $scope.apexClassWrapper = response.data;
            $(document).prop('title', response.data.name);
            if (globalEditor1) {
                globalEditor1.toTextArea();
            }
            setTimeout(function(test) {
                var editor = CodeMirror.fromTextArea(document.getElementById('apexBody'), {
                    lineNumbers: true,
                    matchBrackets: true,
                    lineWrapping: true,
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
                    $(document).prop('title', response.data.name + '  *');
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
                if (localStorage && localStorage.getItem('apexEditorTheme')) {
                    globalEditor1.setOption("theme", localStorage.getItem('apexEditorTheme'));
                }
                //globalEditor1.(localStorage.getItem('apexEditorTheme'));
            }), 2000
            document.getElementById('saveBtn').style.visibility = 'visible';
        }
    }

    function getApexBodyErrorCallback(error) {
        //error code
    }
    $scope.selectTheme = function() {
        if (globalEditor1) {
            globalEditor1.setOption("theme", $scope.selectedTheme);
            localStorage.setItem('apexEditorTheme', $scope.selectedTheme);
        }
    }
    $scope.postdata = function(apexClassWrapper) {
        apexClassWrapper.body = globalEditor1.getValue();
        var dataObj = {
            name: apexClassWrapper.name,
            body: apexClassWrapper.body,
            id: apexClassWrapper.id,
            orgId: localStorage.getItem('organization_id'),
            originalBodyFromOrg: apexClassWrapper.originalBodyFromOrg
        };
        $http.post('/modifyApexBody', dataObj).then(modifyCallback, modifyErrorCallback);

        function modifyCallback(response) {
            $scope.apexClassWrapper = response.data;
            var errors = response.data.pmdStructures;
            if (Object.keys(errors).length > 0) {
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
            iziToast.error({
                title: 'Error',
                message: error,
                position: 'topRight',
            });
        }
        //$http.post("/modifyApexBody", dataObj).success(function(data) {}).error(function(data) {});
    };
    $scope.deployWithErrors = function(apexClassWrapper) {
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
            orgId: localStorage.getItem('organization_id'),
            currentUser: localStorage.getItem('userId'),
            pmdStructures: $scope.errorDetails === 'No errors' ? [] : $scope.errorDetails,
            originalBodyFromOrg: globalMergeEditor != undefined ? globalMergeEditor.rightOriginal().getValue() : apexClassWrapper.originalBodyFromOrg
        };
        $http.post('/saveModifiedApexBody', dataObj).then(saveCallback, saveErrorCallback);

        function saveCallback(response) {
            if (response.data.dataNotMatching) {
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
                }, 1000);
            } else {
                $('.code-helper').select2({
                    disabled: false
                });
                $(document).prop('title', response.data.name);
                console.log('Success : ' + response.data);
                iziToast.success({
                    timeout: 5000,
                    title: 'OK',
                    position: 'bottomLeft',
                    message: 'Saved Successfully !',
                });
                document.getElementById('saveBtn').style.visibility = 'visible';
                if (globalEditor1) {
                    globalEditor1.markClean();
                }
            }
        }

        function saveErrorCallback(error) {
            iziToast.error({
                title: 'Error',
                message: error,
                position: 'topRight',
            });
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
    $scope.logout = function() {
        localStorage.clear();
        $http.get("/logout").then(logoutCallBack, logoutErrorCallback);
    }
    $scope.getRuleEngine = function() {
        var userObject = {
            orgId: localStorage.getItem('organization_id'),
            userId: localStorage.getItem('userId')
        };
        $http.post("/getRuleEngine", userObject).then(getRuleEngineCallBack, getRuleEngineErrorCallback);
    }
    $scope.modifyRuleEngine = function(ruleEngineData, rulesetType, rulePriorities) {
        var modifiedRuleSets = {
            ruleSetWrapper: ruleEngineData,
            rulesetType: rulesetType,
            listOfPriorities: rulePriorities,
            orgId: localStorage.getItem('organization_id')
        }
        $http.post("/modifyRuleEngine", modifiedRuleSets).then(modifyRuleEngineCallBack, modifyRuleEngineErrorCallback);
    }

    function getRuleEngineCallBack(response) {
        if (response.data) {
            $scope.ruleEngineData = response.data.ruleSetWrapper;
            $scope.rulesetType = response.data.rulesetType;
            $scope.rulePriorities = response.data.listOfPriorities;
            $('#ruleEngine').modal('show');
        } else {
            $('#notAuthorised').modal('show');
        }
    };

    function getRuleEngineErrorCallback() {};

    function modifyRuleEngineCallBack() {
        $('#ruleEngine').modal('hide');
    };

    function modifyRuleEngineErrorCallback() {
        $('#ruleEngine').modal('hide');
    };

    function logoutCallBack() {
        $window.location.href = "/index.html";
    };

    function logoutErrorCallback() {};
    $scope.removeCustomSymbolTable = function() {
        iziToast.question({
            timeout: false,
            pauseOnHover: true,
            close: false,
            overlay: true,
            toastOnce: true,
            backgroundColor: 'rgb(136, 160, 185)',
            id: 'question',
            zindex: 999,
            title: 'Hey',
            message: 'Are you sure about that?, after deleting Custom Sysmbol table you need to refresh the page to regenerate it.',
            position: 'center',
            buttons: [
                ['<button><b>YES</b></button>', function(instance, toast) {
                    instance.hide({
                        transitionOut: 'fadeOut'
                    }, toast, 'button');
                    if (localStorage.getItem('hintTable')) {
                        localStorage.removeItem('hintTable');
                    }
                },
                true],
                ['<button>NO</button>', function(instance, toast) {
                    instance.hide({
                        transitionOut: 'fadeOut'
                    }, toast, 'button');
                }], ],
            onClosing: function(instance, toast, closedBy) {
                console.info('Closing | closedBy: ' + closedBy);
            },
            onClosed: function(instance, toast, closedBy) {
                console.info('Closed | closedBy: ' + closedBy);
            }
        });
    }
});

function testAnim(x) {
    $('.modal .modal-dialog').attr('class', 'modal-dialog  ' + x + '  animated');
};
$(document).ready(function() {
    $('.code-helper').select2({
        placeholder: 'Select a command to begin'
    });
    /* Instantiating iziModal */
    $("#enterClass").iziModal({
        overlayClose: false,
        overlayColor: 'rgba(0, 0, 0, 0.6)'
    });
});