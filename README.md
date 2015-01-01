justiciacotidiana-api
=====================

API REST para las aplicaciones del portal de Justicia Cotidiana

###Fast setup
````bash
ant dist
export MONGO_URL='mongodb://user:password@localhost:27017/cide'
curl http://central.maven.org/maven2/com/github/jsimone/webapp-runner/7.0.22.1/webapp-runner-7.0.22.1.jar -o webapp-runner.jar
java -jar webapp-runner.jar dist/justiciacotidiana.war
````

###Construir la aplicación
La aplicación contiene una definición de proyecto de NetBeans IDE, por lo que podrá manipularla y costruirla desde este IDE. Si lo desea, puede construir la aplicación usando directamente Apache ant con el siguiente comando.
````bash
ant dist
````

###Desplegar la aplicación
Una vez construida la aplicación se generará un archivo WAR que podrá ser desplegado en cualquier servidor de aplicaciones Java, mediante los mecanismos asociados a dicho servidor de aplicaciones.

####Despliegue con webapp-runner
Para simplificar el despliegue de la aplicación, puede utilizar [webapp-runner](https://github.com/jsimone/webapp-runner). Para esto, primero deberá descargar el archivo JAR desde el repositorio de webapp-runner y posteriormente ejecutar el siguiente comando:
`````bash
java -jar webap-runner.jar dist/justiciacotidiana.war
````

###Acceso a los datos
La aplicación administra objetos en varias colecciones de MongoDB, por lo que será necesario un servidor de MongoDB en ejecución. Por defecto, la aplicación intenta conectarse a la base de datos llamada **cide**, sin usuario ni password. Sin embargo, si cuenta con credenciales para acceder a las bases de datos, puede especificar la cadena de conexión mediante la definición de la variable de entorno **MONGO_URL** con el siguiente comando:

````bash
export MONGO_URL='mongodb://[user:password@]host:port/database';
````

La cadena de conexión debe ser conforme al [Formato URI de la cadena de conexión](http://docs.mongodb.org/manual/reference/connection-string/) descrito en la documentación de MongoDB.

Para más información, visite el [wiki](https://github.com/haxdai/justiciacotidiana-api/wiki) del proyecto.