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
        addImei: function(imei, value) {
            if (typeof(Storage) !== "undefined") {
                localStorage.setItem(imei, value);
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
                var $row = $(this);
                var imeiObj = $row.data('imei-obj');
                imeiManager.imeiBackupStorage.addImei(imeiObj.imei, JSON.stringify(imeiObj));
            });
        }
    },
    getImeiJsonFromForm: function($form) {
        var $form_inputs = $form.find('.form-control');
        
        var imeiJson = {
            imei: $form_inputs.filter('.imei').val(),
            email: $form_inputs.filter('.email').val(),
            contactPhone: $form_inputs.filter('.contact-phone').val(),
            firstName: $form_inputs.filter('.first-name').val(),
            lastName: $form_inputs.filter('.last-name').val(),
            invoiceNumber: $form_inputs.filter('.invoice-number').val(),
            comment: $form_inputs.filter('.comment').val()
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
        
        var $modal = $('#imei-details-modal').modal('show');
        var $modal_inputs = $modal.find('.form-control');
        
        $modal.data('imei-id', $imei_link.data('imei-id'));
        
        $modal_inputs
                .filter('.imei').val(current_imei).end()
                .filter('.email').val(current_email).end()
                .filter('.contact-phone').val(current_contact_phone).end()
                .filter('.first-name').val(current_first_name).end()
                .filter('.last-name').val(current_last_name).end()
                .filter('.invoice-number').val(current_invoice_number).end()
                .filter('.comment').val(current_comment);
        
        $modal_inputs.attr('readonly', 'readonly');
        $('#update-imei').hide();
        $('#edit-imei').show();
        
        return false; // Prevent go to top of the page on click
    });
    
    $('#edit-imei').on('click', function() {
        $('#imei-details-modal').find('.form-control').not('.imei').removeAttr('readonly');
        $('#edit-imei').hide();
        $('#update-imei').show();
    });
    
    $('#update-imei').on('click', function() {
        var $modal = $('#imei-details-modal').modal();
        
        $.ajax({
            url: '/api/imei_manager/imei/' + $modal.data('imei-id'),
            type: 'PUT',
            data: JSON.stringify( imeiManager.getImeiJsonFromForm( $('#imei-number-details') ) ),
            success: function(result) {
                alert(result);
                window.location.reload();
            }
        });
    });
    
});
