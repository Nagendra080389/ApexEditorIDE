function OrderFormController($scope, $http) {

    $http.get("http://USBLRNAGESINGH1:8989/getApexBody").then(function (response) {
        $scope.apexClassWrapper = response.data;
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
                $('#error').html("Deployed Successfully");
            })
            .error(function (data) {
                $scope.apexClassWrapperError = data;
                $('#error').show();
                $('#error').html(data.message);
            });

    };

};