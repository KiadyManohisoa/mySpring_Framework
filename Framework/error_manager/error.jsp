<%
    String error = (String) request.getAttribute("error");
%>

<!DOCTYPE html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Erreur - Framework</title>
    <style>
        body {
            font-family: Arial, sans-serif;
            background-color: #f8d7da;
            color: #721c24;
            margin: 0;
            padding: 0;
            display: flex;
            justify-content: center;
            align-items: center;
            height: 100vh;
        }
        .error-container {
            text-align: center;
            background-color: #ffffff;
            padding: 20px;
            border: 1px solid #f5c6cb;
            border-radius: 5px;
            box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
        }
        .error-title {
            font-size: 24px;
            margin-bottom: 10px;
        }
        .error-message {
            font-size: 18px;
            margin-bottom: 20px;
        }
        .error-details {
            font-size: 14px;
            margin-bottom: 20px;
        }
        .home-button {
            background-color: #f5c6cb;
            color: #721c24;
            border: none;
            padding: 10px 20px;
            text-decoration: none;
            border-radius: 5px;
            font-size: 16px;
            cursor: pointer;
        }
        .home-button:hover {
            background-color: #f1b0b7;
        }
    </style>
</head>
<body>
    <div class="error-container">
        <h1 class="error-title"> *** ERROR</h1>
        <p class="error-message"><%=error%></p>
    </div>
</body>
</html>
