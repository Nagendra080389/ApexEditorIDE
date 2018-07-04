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
    document.getElementById('saveBtn').style.visibility = 'hidden';
    var namesFromOption = [];
    $scope.isPaneShown = true;
    var foundTheme = ['3024-day', '3024-night', 'abcdef', 'ambiance', 'ambiance-mobile', 'base16-dark', 'base16-light', 'bespin', 'blackboard', 'cobalt', 'colorforth', 'darcula', 'dracula', 'duotone-dark', 'duotone-light', 'eclipse', 'elegant', 'erlang-dark', 'gruvbox-dark', 'hopscotch', 'icecoder', 'idea', 'isotope', 'lesser-dark', 'liquibyte', 'lucario', 'material', 'mbo', 'mdn-like', 'midnight', 'monokai', 'neat', 'neo', 'night', 'oceanic-next', 'panda-syntax', 'paraiso-dark', 'paraiso-light', 'pastel-on-dark', 'railscasts', 'rubyblue', 'seti', 'shadowfox', 'solarized', 'ssms', 'the-matrix', 'tomorrow-night-bright', 'tomorrow-night-eighties', 'ttcn', 'twilight', 'vibrant-ink', 'xq-dark', 'xq-light', 'yeti', 'zenburn'];
    $scope.themeNames = foundTheme;
    if (localStorage.getItem('organization_id') && localStorage.getItem('display_name') && localStorage.getItem('displayEmail') && localStorage.getItem('domainName')) {
        var localUser = {
            email: localStorage.getItem('displayEmail'),
            display_name: localStorage.getItem('displayEmail'),
            domainName: localStorage.getItem('domainName'),
            orgId: localStorage.getItem('organization_id'),
        };
        $scope.currentUser = localUser;
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
            iziToast.info({
                icon: 'fa fa-user',
                title: 'Welcome ' + response.data.display_name,
                message: 'Welcome!',
                position: 'topCenter',
                layout: 2,
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
        $scope.isPaneShown = false;
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
                    var fx = "wobble", $modal = $(this).closest('.iziModal');
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
        $scope.isPaneShown = false;
    }
    $scope.retrieveSelectedClass = function(newValue, oldValue) {
        var windowsEvent = $window;
        $scope.isPaneShown = true;
        if ($scope.selectedName === undefined) {
            $scope.isPaneShown = false;
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
                        $scope.isPaneShown = false;
                        return;
                    }
                }
                if (windowsEvent.event.ctrlKey) {
                    $window.open('/html/apexEditor.html?name=' + newValue.name, '_blank');
                    $scope.selectedName = possibleOldValues[0];
                    $scope.isPaneShown = false;
                    return;
                }
            }
            $scope.isPaneShown = false;
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
                    Pace.track(function(){
                        $http.post("/createFile", nameAndDesc).then(createFileCallback, createFileErrorCallback);
                    });
                    $('#enterClass').iziModal('close');
                } else {
                    var fx = "wobble", $modal = $(this).closest('.iziModal');
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
                        $scope.isPaneShown = false;
                        return;
                    }
                }
            }
            if (windowsEvent.event.ctrlKey) {
                $window.open('/html/apexEditor.html?name=' + newValue.name, '_blank');
                $scope.selectedName = possibleOldValues[0];
                $scope.isPaneShown = false;
                return;
            }
            var data = {
                apexClassName: $scope.selectedName.name
            };
            var config = {
                params: data
            };
            $http.get("/getApexBody", config).then(getApexBodyCallback, getApexBodyErrorCallback);
        }
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
            $scope.isPaneShown = false;
        }
    }

    function createFileErrorCallback(error) {
        $scope.isPaneShown = false;
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
            $scope.isPaneShown = false;
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
            iziToast.error({
                title: 'Error',
                message: error,
                position: 'topRight',
            });
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
                }, 1000);
            } else {
                $('.code-helper').select2({
                    disabled: false
                });
                $(document).prop('title', response.data.name);
                $scope.isPaneShown = false;
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
            $scope.isPaneShown = false;
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
$(document).on('click', '#queryEditor', function(event) {
    event.preventDefault();
    $('#queryEditorModal').iziModal('open');
});
$(document).ready(function() {
    $('.code-helper').select2({
        placeholder: 'Select a command to begin'
    });
    $("#queryEditorModal").iziModal({
        history: true,
        icon: 'icon-star',
        timeoutProgressbar: true,
        timeoutProgressbarColor: 'white',
        arrowKeys: true,
        width: 600,
        padding: 20,
        restoreDefaultContent: true,
        loop: true,
        fullscreen: false,
    });
    $("#queryEditorModal").on('click', '.btn-fetch', function(event) {
        event.preventDefault();
        $("#queryEditorModal").iziModal('startLoading');
        fetch('https://api.github.com/repos/dolce/izimodal', {
            method: 'get' // opcional
        }).then(function(response) {
            response.json().then(function(result) {
                console.log("FullName: " + result.full_name);
                console.log("URL: " + result.html_url);
                console.log("Forks: " + result.forks);
                console.log("Stars: " + result.stargazers_count);
                $("#queryEditorModal .iziModal-content").html("<li><b>Repository</b>: " + result.full_name + "</li><b><li>URL</b>: <a href='" + result.html_url + "' target='blank'> " + result.html_url + "</a></li><b><li>Forks</b>: " + result.forks + "</li><b><li>Stars</b>: " + result.stargazers_count + "</li><b><li>Watchers</b>: " + result.watchers_count + "</li>");
                $("#queryEditorModal").iziModal('stopLoading');
            });
        }).
        catch (function(err) {
            $("#modal-default").iziModal('stopLoading');
            console.error(err);
            $("#modal-default .iziModal-content").html(err);
        });
    });
    /* Instantiating iziModal */
    $("#enterClass").iziModal({
        overlayClose: false,
        overlayColor: 'rgba(0, 0, 0, 0.6)'
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