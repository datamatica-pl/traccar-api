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
    },
    getImeiJsonFromForm: function($form) {
        var imeiJson = {
            imei: $form.find('#imei').val(),
            email: $form.find('#email').val(),
            contactPhone: $form.find('#contact-phone').val(),
            firstName: $form.find('#first-name').val(),
            lastName: $form.find('#last-name').val(),
            invoiceNumber: $form.find('#invoice-number').val(),
            comment: $form.find('#comment').val()
        };
        
        return imeiJson;
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
        
        $.ajax({
            url: '/api/imei_manager/imei/',
            method: 'POST',
            data: JSON.stringify( imeiManager.getImeiJsonFromForm( $addImeiForm ) ),
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
    
    $('#im-logout').on('click', function() {
        $.ajax({
            url: '/api/imei_manager/logout',
            method: "GET",
            async: false,
            error: function(response) {
                var json = $.parseJSON(response.responseText);
                if (json.messageKey && json.messageKey === "err_access_denied") {
                    alert("Dostęp został zablokowany.");
                }
            }
        });
    });
    
});
