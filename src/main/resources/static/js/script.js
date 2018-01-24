var globalEditor = null;
function OrderFormController($scope, $http) {

    $('#autocomplete').autocomplete({
        type: 'POST',
        serviceUrl: 'http://USBLRNAGESINGH1:8989/getSuggestion',
        onSelect: function (suggestion) {
            console.log('suggestion.value -> ' + suggestion.value);

            var data = {
                apexClassName: suggestion.value
            };

            var config = {
                params: data
            };

            $('#loaderImage').show();
            $http.get("http://USBLRNAGESINGH1:8989/getApexBody", config).then(function (response) {
                $scope.apexClassWrapper = response.data;
                $('#loaderImage').hide();
                if(globalEditor) {
                    globalEditor.toTextArea();
                }
                setTimeout(function (test) {
                    CodeMirror.commands.autocomplete = function(cm) {
                            CodeMirror.showHint(cm, CodeMirror.hint.anyword);
                          }
                    var editor = CodeMirror.fromTextArea(document.getElementById('apexBody'), {
                        lineNumbers: true,
                        matchBrackets: true,
                        extraKeys: {"Ctrl-Space": "autocomplete"},
                        mode: "text/x-apex"
                    });

                    globalEditor =  $('.CodeMirror')[0].CodeMirror;
                }), 2000
            });


        }
    });


    $scope.postdata = function (apexClassWrapper) {
        console.log(apexClassWrapper);

        var dataObj = {
            name: apexClassWrapper.name,
            body: apexClassWrapper.body,
            id: apexClassWrapper.id
        };

        $('#loaderImage').show();
        $http.post("http://USBLRNAGESINGH1:8989/modifyApexBody", dataObj)
            .success(function (data) {
                $scope.apexClassWrapper = data;
                $('#loaderImage').hide();
                $('#error').show();
                $('#error').html("Compiled Successfully");
                var value;
                Object.keys(data.lineNumberError).forEach(function (key) {
                    value = data.lineNumberError[key];
                    $('#error').append('<br>' + ' ' + key + ': -> ' + value);
                });

            })
            .error(function (data) {
                $scope.apexClassWrapperError = data;
                $('#loaderImage').hide();
                $('#error').show();
                $('#error').css("color", "red")
                $('#error').html(data.message);
            });

    };

};