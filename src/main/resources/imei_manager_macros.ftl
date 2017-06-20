<#macro imeiDataForm formIdAttr>
    <form id="${formIdAttr}">
        <div class="form-group">
            <label for="${formIdAttr}-imei">Numer IMEI:</label>
            <input type="text" class="form-control imei" id="${formIdAttr}-imei"></input>
        </div>
        <div class="form-group">
            <label for="${formIdAttr}-email">E-mail</label>
            <input type="text" class="form-control email" id="${formIdAttr}-email"></input>
        </div>
        <div class="form-group">
            <label for="${formIdAttr}-contact-phone">Telefon kontaktowy</label>
            <input type="text" class="form-control contact-phone" id="${formIdAttr}-contact-phone"></input>
        </div>
        <div class="form-group">
            <label for="${formIdAttr}-device-model">Model urządzenia</label>
            <input type="text" class="form-control device-model" id="${formIdAttr}-device-model"></input>
        </div>
        <div class="form-group">
            <label for="${formIdAttr}-first-name">Imię</label>
            <input type="text" class="form-control first-name" id="${formIdAttr}-first-name"></input>
        </div>
        <div class="form-group">
            <label for="${formIdAttr}-last-name">Nazwisko</label>
            <input type="text" class="form-control last-name" id="${formIdAttr}-last-name"></input>
        </div>
        <div class="form-group">
            <label for="${formIdAttr}-invoice-number">Numer faktury</label>
            <input type="text" class="form-control invoice-number" id="${formIdAttr}-invoice-number"></input>
        </div>
        <div class="form-group">
            <label for="${formIdAttr}-comment">Uwagi</label>
            <textarea class="form-control comment" id="${formIdAttr}-comment" rows="3"></textarea>
        </div>
    </form>
</#macro>
