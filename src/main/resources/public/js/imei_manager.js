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
    
    $('#imei-numbers').find('.imei-details').on('click', function() {
        var $imei_link = $(this);
        var $imei_row = $imei_link.closest('tr');
        var $imei_row_cells = $imei_row.find('td');
        var current_imei = $imei_row_cells.filter('.imei').find('a').text();
        var current_email = $imei_row_cells.filter('.email').text();
        var current_contact_phone = $imei_row_cells.filter('.contact-phone').text();
        var current_first_name = $imei_row_cells.filter('.first-name').text();
        var current_last_name = $imei_row_cells.filter('.last-name').text();
        var current_invoice_number = $imei_row.data('invoice-number');
        var current_comment = $imei_row_cells.filter('.comment').text();
        
        var $modal = $('#imei-details-modal').modal('show'); // Otwiera modal
        var $modal_inputs = $modal.find('.form-control');
        
        $modal_inputs
                .filter('.imei').val(current_imei).end()
                .filter('.email').val(current_email).end()
                .filter('.contact-phone').val(current_contact_phone).end()
                .filter('.first-name').val(current_first_name).end()
                .filter('.last-name').val(current_last_name).end()
                .filter('.invoice-number').val(current_invoice_number).end()
                .filter('.comment').val(current_comment);
        
        return false; // Prevent go to top of the page on click
    });
    
});
