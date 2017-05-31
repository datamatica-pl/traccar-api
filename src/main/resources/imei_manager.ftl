<#import "imei_manager_macros.ftl" as imm>

<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <meta http-equiv="X-UA-Compatible" content="IE=edge">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <!-- The above 3 meta tags *must* come first in the head; any other head content must come *after* these tags -->
  <title>DM IMEI Manager</title>

  <!-- Bootstrap -->
  <!-- <link href="css/bootstrap.min.css" rel="stylesheet"> -->

  <!-- HTML5 shim and Respond.js for IE8 support of HTML5 elements and media queries -->
  <!-- WARNING: Respond.js doesn't work if you view the page via file:// -->
  <!--[if lt IE 9]>
    <script src="https://oss.maxcdn.com/html5shiv/3.7.3/html5shiv.min.js"></script>
    <script src="https://oss.maxcdn.com/respond/1.4.2/respond.min.js"></script>
  <![endif]-->
</head>
<body>
    <div class="container theme-showcase" role="main">
        <div class="jumbotron">
            <button id="im-logout" type="button" class="btn btn-primary pull-right" data-toggle="modal">Wyloguj</button>
            <img src="/api/images/imei_manager_logo.png" class="img-responsive" alt="IMEI manager logo" width="128" height="128"> 
            <h1>DM IMEI Manager</h1>
            <p>Aplikacja do zarządzania numerami IMEI</p>
        </div>

        <div class="page-header">
            <h1>Lista IMEI</h1>
        </div>

        <div class="container">
          <p>Lista aktualnych numerów IMEI w bazie</p>
          <table id="imei-numbers" class="table">
            <thead>
                <tr>
                    <th>IMEI</th>
                    <th>E-mail</th>
                    <th>Telefon kontaktowy</th>
                    <th>Imię</th>
                    <th>Nazwisko</th>
                    <th>Akcja</th>
                </tr>
            </thead>
            <tbody>
                <#list imeis as imei>
                    <tr>
                        <td class="imei">${imei.imei}</td>
                        <td class="email">${imei.email!''}</td>
                        <td class="contact-phone">${imei.contactPhone!''}</td>
                        <td class="first-name">${imei.firstName!''}</td>
                        <td class="last-name">${imei.lastName!''}</td>
                        <td>
                            <button type="button"
                                    data-imei-id="${imei.id}"
                                    href="/api/delete/${imei.id}"
                                    class="btn btn-sm btn-danger"
                                    data-toggle="confirmation"
                                    data-title="Potwierdź usunięcie IMEI"
                                    data-placement="left"
                                    data-btn-ok-label="Usuń IMEI ${imei.imei}"
                                    data-btn-ok-icon="glyphicon glyphicon-trash"
                                    data-btn-cancel-label="Anuluj"
                                    data-btn-cancel-icon="glyphicon glyphicon-ban-circle"
                                    data-singleton="true"
                                    data-on-confirm="imeiNumberManager.deleteImei">
                                Usuń
                            </button>
                        </td>
                    </tr>
                </#list>
            </tbody>
          </table>
        </div>

        <!-- Button trigger modal -->
        <div class="container">
            <button type="button" class="btn btn-primary" data-toggle="modal" data-target="#new-imei-modal">
              Nowy IMEI
            </button>
        </div>

        <!-- New IMEI modal -->
        <div class="modal fade" id="new-imei-modal" tabindex="-1" role="dialog" aria-labelledby="new-imei-modal-title" aria-hidden="true">
          <div class="modal-dialog" role="document">
            <div class="modal-content">
              <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                  <span aria-hidden="true">&times;</span>
                </button>
                <h5 class="modal-title" id="new-imei-modal-title">Nowy IMEI</h5>
              </div>
              <div class="modal-body">
                <@imm.imeiDataForm formIdAttr="new-imei-number" />
              </div>
              <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-dismiss="modal">Zamknij</button>
                <button id="add-new-imei" type="button" class="btn btn-primary">Dodaj IMEI</button>
              </div>
            </div>
          </div>
        </div>

        <!-- jQuery (necessary for Bootstrap's JavaScript plugins) -->
        <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.12.4/jquery.min.js"></script>
        <!-- Include all compiled plugins (below), or include individual files as needed -->
        <!-- <script src="js/bootstrap.min.js"></script> -->

        <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css" integrity="sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u" crossorigin="anonymous">
        <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap-theme.min.css" integrity="sha384-rHyoN1iRsVXV4nD0JutlnGaslCJuC7uwjduW9SVrLvRYooPp2bWYgmgJQIXwl/Sp" crossorigin="anonymous">
        <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js" integrity="sha384-Tc5IQib027qvyjSMfHjOMaLkfuWVxZxUPnCJA7l2mCWNIpG9mGCD8wGNIcPD7Txa" crossorigin="anonymous"></script>
        <script src="https://cdnjs.cloudflare.com/ajax/libs/bootstrap-confirmation/1.0.5/bootstrap-confirmation.min.js"></script>

        <script src="/api/js/imei_manager.js"></script>
    </div>
</body>
</html>
