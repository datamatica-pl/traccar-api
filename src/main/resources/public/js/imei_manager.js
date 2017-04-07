$(function() {
    
    $('[data-toggle=confirmation]').confirmation({
        rootSelector: '[data-toggle=confirmation]',
        onConfirm: function(e) {
            e.preventDefault(); // Don't scroll page up after click
            
            $.ajax({
                url: '/api/imei/' + this.imeiId,
                type: 'DELETE',
                success: function(result) {
                    alert(result)
                }
            });
        }
    });
    
});
