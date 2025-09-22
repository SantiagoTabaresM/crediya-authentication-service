![CrediYa](docs/CrediYa_logo.png)


# CrediYa - crediya-authentication-service

Este microservicio está diseñado para gestionar usuarios dentro del sistema CreiYa, siguiendo principios de arquitectura hexagonal, desarrollado con WebFlux para un enfoque reactivo y eficiente en la gestión de solicitudes concurrentes.

Cada microservicio en el ecosistema de CreiYa se mantiene en un repositorio y base de datos independiente, asegurando modularidad y escalabilidad.


# Tecnologías utilizadas

- Java 17 / Spring Boot WebFlux – Desarrollo reactivo y no bloqueante.
- Arquitectura Hexagonal (scaffold) – Separación clara entre dominio, aplicación e infraestructura.
- Gradle – Gestión de dependencias y construcción del proyecto.
- PostgreSQL – Base de datos relacional robusta y escalable.
- Spring Data R2DBC – Acceso reactivo a bases de datos SQL.
- Swagger / OpenAPI – Documentación de API interactiva.
- SonarLint – Validación de calidad de código en tiempo de desarrollo.
- JUnit + Mockito / Test unitarios – Validación de lógica de negocio.
- Logs de traza y manejo de excepciones – Para monitoreo y control de errores.


# Arquitectura
Para este proyecto se ha utilizado una clean architecture  (utilizando el pluggin de bancolombia scaffold), que se compone de las siguientes capas: .-


![Clean Architecture](https://miro.medium.com/max/1400/1*ZdlHz8B0-qu9Y-QO3AXR_w.png)
- Domain 
- Infrastructure 
- Application

Este módulo es el más externo de la arquitectura, es el encargado de ensamblar los distintos módulos, resolver las dependencias y crear los beans de los casos de use (UseCases) de forma automática, inyectando en éstos instancias concretas de las dependencias declaradas. Además inicia la aplicación (es el único módulo del proyecto donde encontraremos la función “public static void main(String[] args)”.

# Base de datos 

Para la base de datos se utiliza PostgreSQL originalmente para pruebas en Supabase, para despues  ser cambiada utilizando docker, y se gestiona a través de R2DBC para mantener el enfoque reactivo en todas las capas del microservicio. La configuración de la conexión a la base de datos se encuentra en el archivo `application.yml`, donde se especifican los detalles necesarios para establecer la conexión.
![CrediYa](docs/BD_authentication-service.png)

# Despliegue local con Docker

Para desplegar el microservicio localmente utilizando Docker, se proporciona un archivo `docker-compose.yml` que define los servicios necesarios, incluyendo la base de datos PostgreSQL y el propio microservicio. A continuación, se detallan los pasos para ejecutar el despliegue:
1. Asegúrate de tener Docker y Docker Compose instalados en tu máquina.
2. Ya que este repo se despliega junto a otros microservicios se establece el `docker-compose.yml` a un nivel superior, en este caso en la carpeta `crediya-authentication-service`
3. Se crea una carpeta llamada `scripts` y dentro de esta otra llamada `db_auth` donde se coaca el script `init.sql` para inicializar la base de datos con las tablas necesarias .
4. Se debe ejecutar el siguiente comando en la terminal, ubicado en la carpeta donde se encuentra el archivo `docker-compose.yml`:
   ```bash
   docker-compose up --build
   ```
5. Docker Compose se encargará de construir las imágenes necesarias y levantar los contenedores definidos

**Nota:** En la carpeta `deployment` se encuentran los archivos `Dockerfile`, `docker-compose.yml`, el script de inicialización de la BD utilizados para el despliegue.'

**Nota:** Hay un segundo `docker-compose-proxy.yml` en el cual se utiliza gninx como proxy inverso para gestionar las solicitudes a los microservicios. Para ell funcionamiento de este es necesario crear una carpeta llamada `nginx` y dentro de esta colocar el archivo `default.conf` que se encuentra en la carpeta `deployment`.

![CrediYa](docs/docker_execute.png)