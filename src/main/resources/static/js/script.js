function OrderFormController($scope, $http) {

    $http.get("http://localhost:8989/getApexBody").then(function (response) {
        $scope.apexClassWrapper = response.data;
    });


    $scope.postdata = function (apexClassWrapper) {
        console.log(apexClassWrapper);

        var dataObj = {
            name: apexClassWrapper.name,
            body: apexClassWrapper.body,
            id: apexClassWrapper.id
        };

        $http.post("http://localhost:8989/modifyApexBody", dataObj)
            .success(function (data) {
                $scope.message = data;
            })
            .error(function (data) {
                alert("failure message: " + JSON.stringify({data: data}));
            });

    };

};