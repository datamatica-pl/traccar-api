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
    },
    imeiBackupStorage : {
        addImei: function(imei, email) {
            if (typeof(Storage) !== "undefined") {
                localStorage.setItem(imei, email);
            }
        },
        removeImei: function(imei) {
            if (typeof(Storage) !== "undefined") {
                localStorage.setItem(imei, "deleted");
            }
        },
        refreshLocalImeis: function() {
            $imeiNumberRows = $('#imei-numbers')
                                .find('tbody')
                                .find('tr');

            $imeiNumberRows.each(function() {
                var $cells = $(this).find('td');
                var imei = $cells.filter('.imei').text();
                
                imeiManager.imeiBackupStorage.addImei(imei, '');
            });
        }
    }
}

$(function() {
    
    imeiManager.imeiBackupStorage.refreshLocalImeis();
    
    $('[data-toggle=confirmation]').confirmation({
        rootSelector: '[data-toggle=confirmation]',
        onConfirm: function(e, del_button) {
            e.preventDefault(); // Don't scroll page up after click
            
            $.ajax({
                url: '/api/imei_manager/imei/' + this.imeiId,
                type: 'DELETE',
                success: function(result) {
                    var $alertEl = imeiManager.bootstrapAlertsFactory.getAlertEl('success', result);
                    var $rowToDelete = $(del_button).closest('tr');
                    
                    imeiManager.imeiBackupStorage.removeImei( $rowToDelete.find('td.imei').text() );
                    
                    $rowToDelete.remove();
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
                window.location.reload();
            },
            error: function(response) {
                if (response.statusText === "Conflict") {
                    alert(response.responseText);
                } else {
                    alert("Wystąpił błąd przy dodawaniu numeru IMEI." +
                        " Proszę sprawdzić, czy IMEI jest poprawny," +
                        " oraz czy nie występuje już w bazie.");
                }
            }
        });
    });
    
});
