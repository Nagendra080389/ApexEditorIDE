var globalEditor1 = null;
var globalMergeEditor = null;
var widgets = [];

function OrderFormController($scope, $http) {

    document.getElementById("saveBtn").disabled = true;
    document.getElementById("cleanBtn").disabled = true;
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
                document.getElementById("cleanBtn").disabled = false;
                $scope.apexClassWrapper = response.data;
                $('#loaderImage').hide();
                if (globalEditor1) {
                    globalEditor1.toTextArea();
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
                        gutters: ["CodeMirror-lint-markers"],
                        lint: true,
                        mode: "text/x-apex"

                    });

                    globalEditor1 = $('.CodeMirror')[0].CodeMirror;
                }), 2000
            });


        }
    });

    $scope.postdata = function(apexClassWrapper) {
        console.log(apexClassWrapper);

        apexClassWrapper.body = globalEditor1.getValue();
        var dataObj = {
            name: apexClassWrapper.name,
            body: apexClassWrapper.body,
            id: apexClassWrapper.id,
            originalBodyFromOrg: apexClassWrapper.originalBodyFromOrg

        };


        $('#loaderImage').show();
        $http.post("/modifyApexBody", dataObj)
            .success(function(data) {
                $scope.apexClassWrapper = data;
                $('#loaderImage').hide();
                var errors = data.pmdStructures;
                if (Object.keys(errors).length > 0) {
                    if(data.isCompilationError){
                       for (var i = 0; i < widgets.length; ++i){
                             globalEditor1.removeLineWidget(widgets[i]);
                       }
                       widgets.length = 0;
                       for (var i = 0; i < 1; ++i) {
                             var err = errors[i];
                             if (!err) continue;
                             var msg = document.createElement("div");
                             var icon = msg.appendChild(document.createElement("span"));
                             icon.innerHTML = "!!";
                             icon.className = "lint-error-icon";
                             msg.appendChild(document.createTextNode(err.reviewFeedback));
                             msg.className = "lint-error";
                             widgets.push(globalEditor1.addLineWidget(err.lineNumber - 1, msg, {coverGutter: false, noHScroll: true}));
                        }
                    }else{
                        $scope.errorDetails = errors;
                        $('#myModal').modal('show');
                    }
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
        apexClassWrapper.body = globalEditor1.getValue();
        var dataObj = {
            name: apexClassWrapper.name,
            body: apexClassWrapper.body,
            id: apexClassWrapper.id,
            //originalBodyFromOrg: apexClassWrapper.originalBodyFromOrg
            originalBodyFromOrg: globalMergeEditor != undefined ? globalMergeEditor.rightOriginal().getValue() : apexClassWrapper.originalBodyFromOrg
        };

        $http.post("/saveModifiedApexBody", dataObj)
            .success(function(data) {
                console.log('Success : ' + data);
                if (data.dataNotMatching) {
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
      globalEditor1.getDoc().setValue(globalMergeEditor.editor().getValue());

    };

    $scope.replaceSpaceWithTabs = function(){
        var cleaneddata = globalEditor1.getValue().replace(new RegExp(' +', 'g'), ' ')
        globalEditor1.getDoc().setValue(cleaneddata);

    };

    $scope.newClassCreation = function(){
          bootbox.prompt({
                  title: 'Enter Class Name',
                  placeholder: 'Enter Class Name',
                  buttons: {
                      confirm: {
                          label: 'Submit'
                      }
                  },
                  callback: function(value){
                      if(value == null){
                          return;
                      }

                  }
              });

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

function makeMarker() {
  var marker = document.createElement("div");
  marker.style.color = "#FF0000";
  marker.innerHTML = "●";
  return marker;
}