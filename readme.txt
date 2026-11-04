Soy Xulio, del grupo 1 de APP del munics y os escribo porque analizando la aplicación hemos visto una vulnerabilidad al exponer los registros de la aplicación.

Cualquier usuario puede acceder al archivo de logs desde cualquier máquina mediante una petición como esta:

curl 'http://localhost:8888/resources/server.log'

En estos registros se muestra mucha información interna del sistema. Entre esta información, al filtrar la salida buscando el símbolo @, es posible extraer fácilmente una lista completa con los correos electrónicos de los usuarios activos.

Estos correos filtrados podrían utilizarse posteriormente para dirigir otros ataques contra la plataforma, como por ejemplo en la funcionalidad de restablecimiento de contraseña que descubrimos anteriormente (Grupo 1 -  Validación de datos de entrada (III) - Vie 09/10/2026 20:58) o directamente SQL-i para autenticarse como cualquier usuario sin saber su contraseña.

Un saludo,






Soy Xulio, del grupo 1 de APP del munics y os escribo porque analizando la aplicación hemos encontrado una vulnerabilidad en la funcionalidad de cancelación de pedidos.

Cuando se procesa la cancelación, el servidor recibe la sesión del usuario pero busca el pedido únicamente por su identificador. El problema es que el código no comprueba si la orden pertenece realmente al usuario que realiza la petición antes de cambiar su estado a cancelado.

Como los IDs de los pedidos son autonuméricos y secuenciales (1, 2, 3...), cualquier usuario autenticado en la aplicación puede modificar el identificador del pedido para cancelar las compras de otros usuarios sin su permiso, siempre que el pedido esté en estado pendiente:

curl 'http://localhost:8888/orders/3/cancel' -X POST -H 'Cookie: JSESSIONID=C4E74174536D9A10366495ACBC226729'

Un saludo,





Soy Xulio, del grupo 1 de APP del munics y os escribo porque analizando la aplicación hemos encontrado una vulnerabilidad al publicar comentarios en los productos.

Cuando un usuario envía un comentario, la aplicación no limpia los datos de entrada antes de guardarlos. Si se introduce un comentario con etiquetas de script como este:
<!DOCTYPE html>
<html>
<body>
  <script>
    alert('XSS');
  </script>
</body>
</html>

este se almacena directamente en la base de datos.

Cuando cualquier otro usuario entra a ver el producto, la página carga el comentario y ejecuta ese código automáticamente en su navegador. Esto permite que se ejecute código no autorizado en el cliente de los usuarios que estén viendo las valoraciones. Aunque en esta caso solo mostramos una alerta en pantalla, podría utilizarse para robar las cookies de sesión del usuario y enviarlas a un servidor remoto, redirigir a los usuarios a páginas externas falsas o maliciosas, capturar las pulsaciones de teclado o datos introducidos en la pantalla...




Hemos encontrado una vulnerabilidad en el restablecimiento de contraseña por correo. Al manipular la cabecera Host en la petición:

curl 'http://localhost:8888/sendEmail' \
  -X POST \
  -H 'Host: dominiofalso.com' \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  --data-raw 'email=prueba%40gmail.com'

El servidor genera el enlace del correo apuntando al dominio del atacante en lugar de al legítimo:

http://dominiofalso.com/changePassword?token=...&email=prueba@gmail.com

Si la víctima pulsa en el enlace, el atacante recibe en sus logs el token de restablecimiento y puede usarlo para cambiar la contraseña de la cuenta.









Hola,

Te escribo porque analizando la aplicación hemos encontrado una vulnerabilidad en el mecanismo de autenticación dentro del formulario de inicio de sesión. Supongo que ya la ha descubierto otro grupo, pero aviso de todas formas.

Al procesar la solicitud, el servidor concatena directamente la entrada del correo electrónico dentro de una consulta HQL/JPQL en la capa de datos. Aunque la interfaz web restringe la inserción de ciertos caracteres especiales en el navegador, esta validación se produce únicamente en el frontend. Interceptando o extrayendo la solicitud HTTP directamente desde la pestaña de red de las herramientas de desarrollo del navegador, un usuario puede modificar los parámetros enviados al backend.

Si en el campo del correo se introduce una cadena como prueba@gmail.com' or '1'='1, al concatenarse con la comprobación posterior de la contraseña la consulta interna queda construida como:

WHERE email = 'prueba@gmail.com' or '1'='1' and password = 'passwordInvalida'

Debido a las reglas de precedencia de operadores lógicos, el motor evalúa primero el operador AND frente al OR. Aunque la verificación de la contraseña falle ('1'='1' and password = 'passwordInvalida' resulte en falso), la expresión completa se reduce a:

WHERE email = 'prueba@gmail.com' or falso

Como el correo de la izquierda existe en la base de datos, la condición global se evalúa como verdadera. Esto provoca que el backend dé por confirmada la existencia de la cuenta y complete el inicio de sesión sin comprobar si la contraseña coincide. 

La petición reproducida a partir de los datos obtenidos en las herramientas de depuración tiene la siguiente forma:

 curl 'http://localhost:8888/login' -i \

  -X POST \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  --data-raw 'email=prueba%40gmail.com%27+or+%271%27%3d%271&password=passwordInvalida&_rememberMe=on'

Al enviar esta solicitud, el servidor responde con un código 302 Found, asignando una cookie de sesión válida. Al cargar la página principal utilizando dicha sesión, el usuario accede autenticado con el perfil asociado a dicha cuenta, teniendo acceso completo a sus datos y funciones privadas.

Quedamos a tu disposición por si necesitas cualquier comprobación adicional.

Saludos,

Xulio









Te escribo porque analizando la aplicación hemos encontrado una vulnerabilidad en la parte de actualizar la foto de perfil. En principio la imagen debería quedarse guardada en la carpeta del usuario (store-app/work/{id-usuario}/), pero el servidor acepta el nombre del archivo tal cual viene en la petición HTTP. Si modificamos esa petición e incluimos ../ en el nombre, el servidor permite salir de esa carpeta y subir niveles por los directorios del proyecto para almacenar la foto en cualquier rincón.

Además, el servidor tampoco comprueba si lo que se sube es realmente una imagen. Un atacante podría interceptar su propia petición desde las herramientas de desarrollador del navegador para copiar su cookie de sesión y, a partir de ahí, usar una herramienta como curl para enviar archivos .html o cualquier texto y sobrescribir archivos clave del servidor.

En el comando de curl, el primer elemento (@Index.html) es el archivo malicioso que tenemos preparado en nuestra máquina local, y el parámetro filename (../../src/main/resources/templates/Index.html) es la ruta del archivo objetivo que queremos reemplazar en el servidor. Con esto se podría machacar directamente la plantilla de inicio por una propia y cambiar lo que ve cualquier visitante de la web:
curl 'http://localhost:8888/profile' \
  -X POST \
  -H 'Cookie: JSESSIONID=COOKIE' \
  -F 'name=UsuarioPrueba' \
  -F 'email=usuario@prueba.com' \
  -F 'address=Calle Prueba' \
  -F 'image=@Index.html;filename="../../src/main/resources/templates/Index.html";type=image/png'

Por último, aprovecho para comentarte que Gabriel Ngodji Entcheu está en nuestro grupo (el Grupo 1), ya que nos dimos cuenta de que no aparece anotado así en el foro de novas.

Gracias de antemano,
