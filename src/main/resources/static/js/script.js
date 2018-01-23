function OrderFormController($scope, $http) {

    $('#autocomplete').autocomplete({
        type: 'POST',
        serviceUrl: 'http://USBLRNAGESINGH1:8989/getSuggestion',
        onSelect: function (suggestion) {
            console.log('suggestion.value -> '+suggestion.value);

            var data = {
                apexClassName:suggestion.value
            };

            var config = {
                params: data
            };

            $http.get("http://USBLRNAGESINGH1:8989/getApexBody",config).then(function (response) {
                $scope.apexClassWrapper = response.data;
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

        $http.post("http://USBLRNAGESINGH1:8989/modifyApexBody", dataObj)
            .success(function (data) {
                $scope.apexClassWrapper = data;
                $('#error').show();
                $('#error').html("Compiled Successfully");
            })
            .error(function (data) {
                $scope.apexClassWrapperError = data;
                $('#error').show();
                $('#error').css("color","red")
                $('#error').html(data.message);
            });

    };

};