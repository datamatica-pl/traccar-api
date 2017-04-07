<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <meta http-equiv="X-UA-Compatible" content="IE=edge">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <!-- The above 3 meta tags *must* come first in the head; any other head content must come *after* these tags -->
  <title>DM IMEI Manager</title>

  <!-- Bootstrap -->
  <link href="css/bootstrap.min.css" rel="stylesheet">

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
            <img src="https://localhost/api/images/imei_manager_logo.png" class="img-responsive" alt="IMEI manager logo" width="128" height="128"> 
            <h1>DM IMEI Manager</h1>
            <p>Aplikacja do zarządzania numerami IMEI</p>
        </div>

        <div class="page-header">
            <h1>Lista IMEI</h1>
        </div>

        <div class="container">
          <p>Lista aktualnych numerów IMEI w bazie</p>
          <table class="table">
            <thead>
                <tr>
                    <th>IMEI</th>
                    <th>Imię</th>
                    <th>Nazwisko</th>
                    <th>E-mail</th>
                    <th>Telefon</th>
                </tr>
            </thead>
            <tbody>
                <#list imeis as imei>
                    <tr>
                        <td>${imei.imei}</td>
                        <td></td>
                        <td></td>
                        <td></td>
                        <td></td>
                    </tr>
                </#list>
                <tr>
                    <td>TODO: Input</td>
                    <td>TODO: Input</td>
                    <td>TODO: Input</td>
                    <td>TODO: Input</td>
                    <td>TODO: Input</td>
                </tr>
            </tbody>
          </table>
        </div>

        <!-- jQuery (necessary for Bootstrap's JavaScript plugins) -->
        <script src="https://ajax.googleapis.com/ajax/libs/jquery/1.12.4/jquery.min.js"></script>
        <!-- Include all compiled plugins (below), or include individual files as needed -->
        <!-- <script src="js/bootstrap.min.js"></script> -->

        <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css" integrity="sha384-BVYiiSIFeK1dGmJRAkycuHAHRg32OmUcww7on3RYdg4Va+PmSTsz/K68vbdEjh4u" crossorigin="anonymous">
        <script src="imei_manager_js"></script>
    </div>
</body>
</html>
