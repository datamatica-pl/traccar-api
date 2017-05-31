<#macro imeiDataForm formIdAttr>
    <form id="${formIdAttr}">
        <div class="form-group">
            <label for="imei">Numer IMEI:</label>
            <input type="text" class="form-control" id="imei">
        </div>
        <div class="form-group">
            <label for="email">E-mail</label>
            <input type="text" class="form-control" id="email">
        </div>
        <div class="form-group">
            <label for="contact-phone">Telefon kontaktowy</label>
            <input type="text" class="form-control" id="contact-phone">
        </div>
        <div class="form-group">
            <label for="first-name">Imię</label>
            <input type="text" class="form-control" id="first-name">
        </div>
        <div class="form-group">
            <label for="last-name">Nazwisko</label>
            <input type="text" class="form-control" id="last-name">
        </div>
        <div class="form-group">
            <label for="invoice-number">Numer faktury</label>
            <input type="text" class="form-control" id="invoice-number">
        </div>
        <div class="form-group">
            <label for="comment">Uwagi</label>
            <input type="textarea" class="form-control" id="comment">
        </div>
    </form>
</#macro>
