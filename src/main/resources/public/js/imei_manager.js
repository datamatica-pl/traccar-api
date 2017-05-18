var imeiManager = {
    bootstrapAlertsFactory : {
        getAlertEl: function(alertType, alertMessage) {
            var $bootstrapAlert = $('<div class="alert alert-dismissable" />');
            var allowedTypes = ["success", "info", "warning", "danger"];
            var alertClass = "alert-info";
            
            if ($.inArray(alertType, allowedTypes) !== -1) {
                alertClass = "alert-" + alertType;
            }
            
            $bootstrapAlert.append('<a href="#" class="close" data-dismiss="alert" aria-label="close">&times;</a>');
            $bootstrapAlert.addClass(alertClass);
            $bootstrapAlert.append( $("<span />").text(alertMessage) );
            
            return $bootstrapAlert;
        }
    }
}

$(function() {
    
    $('[data-toggle=confirmation]').confirmation({
        rootSelector: '[data-toggle=confirmation]',
        onConfirm: function(e, del_button) {
            e.preventDefault(); // Don't scroll page up after click
            
            $.ajax({
                url: '/api/imei_manager/imei/' + this.imeiId,
                type: 'DELETE',
                success: function(result) {
                    var $alertEl = imeiManager.bootstrapAlertsFactory.getAlertEl('success', result);
                    
                    $(del_button).closest('tr').remove();
                    $("#imei-numbers").before($alertEl);
                }
            });
        }
    });
    
    $('#add-new-imei').on('click', function() {
        var $addImeiForm = $('form#new-imei-number');
        var $imeiNumber = $addImeiForm.find('#imei');
        
        $.ajax({
            url: '/api/imei_manager/imei/',
            method: 'POST',
            data: {
                imeiNumber: $imeiNumber.val()
            },
            success: function(result) {
                alert(result);
            }
        });
    });
    
});
