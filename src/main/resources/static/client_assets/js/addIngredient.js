$(document).ready(function () {
    // Lấy ngày hiện tại


    let count = 1;
    $('#addIngredientButton').click(function () {
        count++;
        $.ajax({
            url: '/api/add-ingredient',
            type: 'GET',
            data: {index:count},
            dataType: 'html',
            success: function (response) {
                $('#tbody').append(response);
            },
            error: function(xhr, status, error) {
                console.log(xhr);
                console.log(error);
            }
        });
    });

});

