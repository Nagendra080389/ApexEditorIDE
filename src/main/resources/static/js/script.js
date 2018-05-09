var globalEditor = null;
var globalMergeEditor = null;

function OrderFormController($scope, $http) {

    document.getElementById("saveBtn").disabled = true;
    $('#autocomplete').autocomplete({
        type: 'POST',
        serviceUrl: '/getSuggestion',
        onSelect: function(suggestion) {
            console.log('suggestion.value -> ' + suggestion.value);

            var data = {
                apexClassName: suggestion.value
            };

            var config = {
                params: data
            };

            $('#loaderImage').show();
            $http.get("/getApexBody", config).then(function(response) {
                document.getElementById("saveBtn").disabled = false;
                $scope.apexClassWrapper = response.data;
                console.log('date while Loading  - >' +response.data.salesForceSystemModStamp);
                $('#loaderImage').hide();
                if (globalEditor) {
                    globalEditor.toTextArea();
                }
                setTimeout(function(test) {
                    CodeMirror.commands.autocomplete = function(cm) {
                        cm.showHint({
                            hint: CodeMirror.hint.auto
                        });
                    };
                    var editor = CodeMirror.fromTextArea(document.getElementById('apexBody'), {
                        lineNumbers: true,
                        matchBrackets: true,
                        extraKeys: {
                            "Ctrl-Space": "autocomplete"
                        },
                        mode: "text/x-apex"

                    });

                    globalEditor = $('.CodeMirror')[0].CodeMirror;
                }), 2000
            });


        }
    });

    var ModalInstanceCtrl = function($scope, $modalInstance, data) {
        $scope.data = data;
        $scope.close = function( /*result*/ ) {
            $modalInstance.close($scope.data);
        };
    };

    $scope.data = {
        boldTextTitle: "Done",
        textAlert: "Some content",
        mode: 'error'
    }


    $scope.postdata = function(apexClassWrapper) {
        console.log(apexClassWrapper);

        apexClassWrapper.body = globalEditor.getValue();
        var dataObj = {
            name: apexClassWrapper.name,
            body: apexClassWrapper.body,
            id: apexClassWrapper.id,
            salesForceSystemModStamp: new Date(apexClassWrapper.salesForceSystemModStamp)
        };

        console.log('salesForceSystemModStamp - >' +dataObj.salesForceSystemModStamp);

        $('#loaderImage').show();
        $http.post("/modifyApexBody", dataObj)
            .success(function(data) {
                $scope.apexClassWrapper = data;
                $('#loaderImage').hide();
                var errors = data.lineNumberError;
                if (Object.keys(errors).length > 0) {
                    $scope.errorDetails = errors;
                    $('#myModal').modal('show');
                } else {
                    $scope.errorDetails = 'No errors';
                    $('#myModalWithoutError').modal('show');
                }

            })
            .error(function(data) {
                $scope.apexClassWrapperError = data;
                $('#loaderImage').hide();
                $('#error').show();
                $('#error').css("color", "red")
                $('#error').html(data.message);
            });

    };

    $scope.deployWithErrors = function(salesforcetimeStamp, apexClassWrapper) {
        $('#myModal').modal('hide');
        $('#loaderImage').show();
        apexClassWrapper.body = globalEditor.getValue();
        var dataObj = {
            name: apexClassWrapper.name,
            body: apexClassWrapper.body,
            id: apexClassWrapper.id,
            salesForceSystemModStamp: new Date(apexClassWrapper.salesForceSystemModStamp)
        };

        $http.post("/saveModifiedApexBody", dataObj)
            .success(function(data) {
                console.log('Success : ' + data);
                if (data.timeStampNotMatching) {
                    $scope.apexClassWrapper = data;
                    $('#diffView').modal('show');
                    var value, orig1, orig2, dv, hilight = true;
                    orig1 = data.body;
                    orig2 = data.modifiedApexClassWrapper.body;
                    var target = document.getElementById("mergemodal");
                    target.innerHTML = "";
                    dv = CodeMirror.MergeView(target, {
                        value: orig1,
                        origLeft: null,
                        orig: orig2,
                        lineNumbers: true,
                        mode: "text/x-apex",
                        highlightDifferences: hilight
                    });
                    globalMergeEditor = dv;
                }
                $('#loaderImage').hide();
            })
            .error(function(data) {
                $('#loaderImage').hide();
                console.log('Failure : ' + data);
            });
    }

    $scope.replaceMerged = function(){
      globalEditor.getDoc().setValue(globalMergeEditor.editor().getValue());

    };

    $scope.replaceSpaceWithTabs = function(){
        var cleaneddata = globalEditor.getValue().replace(new RegExp(' +', 'g'), ' ')
        globalEditor.getDoc().setValue(cleaneddata);

    };

    $(document).ready(function() {

        $('#loaderImage').show();
        $http.post("/getAllApexClasses")
            .success(function(data) {


            })
            .error(function(data) {

            });
        $('#loaderImage').hide();
    });




};

function testAnim(x) {
    $('.modal .modal-dialog').attr('class', 'modal-dialog  ' + x + '  animated');
};



/* Set the width of the side navigation to 250px and the left margin of the page content to 250px and add a black background color to body */
function openNav() {
    document.getElementById("mySidenav").style.width = "250px";
    document.getElementById("main").style.marginLeft = "250px";
    document.body.style.backgroundColor = "rgba(0,0,0,0.4)";
}

/* Set the width of the side navigation to 0 and the left margin of the page content to 0, and the background color of body to white */
function closeNav() {
    document.getElementById("mySidenav").style.width = "0";
    document.getElementById("main").style.marginLeft = "0";
    document.body.style.backgroundColor = "white";
}