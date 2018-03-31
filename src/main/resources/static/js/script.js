var globalEditor = null;
function OrderFormController($scope, $http) {

    $('#autocomplete').autocomplete({
        type: 'POST',
        serviceUrl: '/getSuggestion',
        onSelect: function (suggestion) {
            console.log('suggestion.value -> ' + suggestion.value);

            var data = {
                apexClassName: suggestion.value
            };

            var config = {
                params: data
            };

            $('#loaderImage').show();
            $http.get("/getApexBody", config).then(function (response) {
                $scope.apexClassWrapper = response.data;
                $('#loaderImage').hide();
                if (globalEditor) {
                    globalEditor.toTextArea();
                }
                setTimeout(function (test) {
                    CodeMirror.commands.autocomplete = function (cm) {
                        cm.showHint({hint: CodeMirror.hint.anyword});
                        //cm.showHint({hint: CodeMirror.hint.fromList});
                    };
                    var editor = CodeMirror.fromTextArea(document.getElementById('apexBody'), {
                        lineNumbers: true,
                        matchBrackets: true,
                        extraKeys: {"Ctrl-Space": "autocomplete"},
                        mode: "text/x-apex"

                    });

                    globalEditor = $('.CodeMirror')[0].CodeMirror;
                }), 2000
            });


        }
    });

    var ModalInstanceCtrl = function ($scope, $modalInstance, data) {
      $scope.data = data;
      $scope.close = function(/*result*/){
        $modalInstance.close($scope.data);
      };
    };

    $scope.data = {
        boldTextTitle: "Done",
        textAlert : "Some content",
        mode : 'error'
      }


    $scope.postdata = function (apexClassWrapper) {
        console.log(apexClassWrapper);

        apexClassWrapper.body = globalEditor.getValue();
        var dataObj = {
            name: apexClassWrapper.name,
            body: apexClassWrapper.body,
            id: apexClassWrapper.id
        };

        $('#loaderImage').show();
        $http.post("/modifyApexBody", dataObj)
            .success(function (data) {
                $scope.apexClassWrapper = data;
                $('#loaderImage').hide();
                /*$('#error').show();
                $('#error').html("Compiled Successfully");
                var value;
                Object.keys(data.lineNumberError).forEach(function (key) {
                    value = data.lineNumberError[key];
                    $('#error').css("color", "red")
                    $('#error').append('<br>' + ' ' + key + ': -> ' + value);
                });*/
                $scope.errorDetails = data.lineNumberError;
                $('#myModal').modal('show');

            })
            .error(function (data) {
                $scope.apexClassWrapperError = data;
                $('#loaderImage').hide();
                $('#error').show();
                $('#error').css("color", "red")
                $('#error').html(data.message);
            });

    };

    $(document).ready(function () {

            $('#loaderImage').show();
            $http.post("/getAllApexClasses")
                .success(function (data) {


                })
                .error(function (data) {

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