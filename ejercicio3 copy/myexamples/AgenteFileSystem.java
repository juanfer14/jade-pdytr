import jade.core.*;

import java.lang.IndexOutOfBoundsException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Serializable;

public class AgenteFileSystem extends Agent {

    /* DATOS QUE SE LEEN POR DEFAULT */
    private static final int SIZE = 512;

    /* 
     *      isOrigin: INDICA SI ESTA EN EL CONTAINER DE SALIDA
     *      finished: INDICA SI SE TERMINO DE LEER O ESCRIBIR
     */
    private boolean isOrigin;
    private boolean finished;

    /* 
    *   UBICACIONES A DONDE DEBE IR EL AGENTE 
    *       origin: La ubicacion desde donde empieza
    *       destiny: La ubicacion a donde debe leer o escribir     
    */
    private Location origin;
    private Location destiny;
	
    /* ENTRADA */

    /* 
     *      name: Nombre del archivo a leer o escribir
     *      operation: Operacion a realizar 'read' o 'write'
     *      postion: Posicion del archivo a leer o escribir
     *      length: Cantidad de bytes solicitadas para leer o escribir
     */
    private String name;
    private String operation;
    private int position;
    private int length;

    /*variable auxiliar */
    private boolean not_copy_yet = true;
    
    /* SALIDA */

    /* 
     *  readed: DATOS QUE SE LLEVAN LEIDOS EN TOTAL.
     *  effective: DATOS EFECTIVAMENTE LEIDOS.
     */
    private int readed;
    private int effective;

    /* DATOS QUE SE RECIBEN DEL READ O DATOS QUE SE DEBEN ESCRIBIR EN EL WRITE  */
    private byte[] data;


    public void setup(){
        try {
            System.out.println("SE EJECUTO EL SETUP");
            boolean isWrite = checkArgs();
            /* SI ES UN WRITE, DEBO LEER LO QUE TENGO, ANTES DE IR AL DESTINO */
            if (isWrite)
                read();
            doMove(destiny);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        System.out.println("TERMINO EL SETUP");
    }

    private boolean checkArgs() throws Exception{
            Object[] args = getArguments();
            if(args.length > 2 && args.length < 6){
                origin = here();
                isOrigin = true;
                finished = false;
                readed = 0;
                destiny = new ContainerID((String) args[0], null);
                operation = (String) args[1];
                name = (String) args[2];

                if (!operation.equals("copy")){
                    length = Integer.parseInt((String)args[3]);
                    System.out.println("LA CANTIDAD DE BYTES SON " + args.length);
                }else{
                    length = 10000;
                }
                

                System.out.println("LA OPERACION ES " + operation);
                System.out.println("EL NOMBRE DEL ARCHIVO ES " + name);
                System.out.println("LA CANTIDAD DE BYTES A LEER/ESCRIBIR ES " + length);
                

                /*  
                *       SI LA OPERACION ES 'READ', DEBERIA RECIBIR
                *          'CONTAINER, OPERACION, NOMBRE_ARCH, CANT_BYTES_LEER, POSICION'
                */
                if(checkMethod("read", 5, args.length)){
                    System.out.println("SE EJECUTO EL READ");
                    position = Integer.parseInt((String)args[4]);
                    System.out.println("SE TERMINO DE EJECUTAR EL READ");
                    return false;
                }
                /*  
                *       SI LA OPERACION ES 'WRITE', DEBERIA RECIBIR
                *          'CONTAINER, OPERACION, NOMBRE_ARCH, CANT_BYTES_ESCRIBIR'
                */
                else if(checkMethod("write", 4, args.length)){
                    System.out.println("SE EJECUTO EL WRITE");
                    position = 0;
                    return true;
                }

                /*
                 * Si la operacion es copy, los parametros deberan ser:
                 * Container, Operacion, nombre del  archivo
                 */

                else if(checkMethod("copy", 3, args.length)){
                    System.out.println("SE EJECUTO EL COPY");
                    position = 0;
                    length = 10000;
                    System.out.println("SE TERMINO DE EJECUTAR EL COPY");
                    return false;
                }
                else throw new Exception(
                    "LOS ARGS. SON container, operacion (read o write), " + 
                    "nombre_archivo, cant_bytes (mayor a 0), posicion (solo en read)"+
                    checkMethod("copy", 3, args.length));
            } else throw new Exception("LA CANTIDAD DE ARGS. ES INVALIDA");
    }

    private boolean checkMethod(String method, int numberArgs, int argsLength){
        return operation.equals(method) && length > 0 && position >= 0 && argsLength == numberArgs;
    }

    protected void afterMove() {
        isOrigin = (origin.getID().equals(here().getID()));

        if(operation.equals("write"))
                if(isOrigin)
                    read();
                else
                    write();
        else if (operation.equals("copy")){
                if(isOrigin)
                    write();
                else{
                    read();
                }                 
        };

        if(!finished)
            doMove(isOrigin ? destiny : origin);
        else
            finalMessage();
    }

    private void finalMessage(){
            if(operation.equals("write")){
                System.out.println("TERMINO LA ESCRITURA...");
                System.out.println("LA CANTIDAD DE BYTES ESCRITOS FUERON " + readed);
            }
            else if(operation.equals("read")){
                System.out.println("TERMINO LA LECTURA...");
            }
            
    }

    private void write(){
        try {
            /* LEO EL ARCHIVO, DEPENDIENDO EN DONDE ESTE */
            File file = new File((isOrigin ? "local/" : "fs/") + name);

            /* CREA UN ARCHIVO SI NO EXISTE */
            file.createNewFile();

            String path = file.getAbsolutePath();
            /* 
             *   SE INSTANCIA UN FileOutputStream con file y true
             *   CON TRUE, SE INDICA QUE SI EL ARCHIVO YA EXISTE, 
             *   SE ABRE EN MODO APPEND.
             *  
            */
            FileOutputStream fos = new FileOutputStream(file, true);
            
            /*  
             *  ESCRIBO LOS DATOS DE "DATA" EN EL ARCHIVO, 
             *  DESDE EL FINAL, SEGUNA LA CANTIDAD DE BYTES 
             *  LEIDAS PREVIAMENTE y SE CIERRA EL FOS
             */
            fos.write(data, 0, effective);
            System.out.println("SE ESCRIBIERON " + effective + " BYTES EN " + path);
            fos.close();
             System.out.println("CERRANDO EL ARCHIVO");


        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private void read(){
        try {
            if (not_copy_yet == true){

                /*
                 * GENERO LA COPIA DE UN ARCHIVO.
                 */

                String nombreArchivoOrigen = "backup/"+name;
                String nuevoNombreArchivo = "backup/"+generarNuevoNombre(name);


                //Actualizo el tamanio total del archivo a leer

                File archivo = new File(nombreArchivoOrigen);
                length = (int) archivo.length();

                System.out.println("La cantidad a leer sera "+length);
                try {
                    copiarArchivo(nombreArchivoOrigen, nuevoNombreArchivo);
                    System.out.println("Archivo copiado exitosamente.");
                    not_copy_yet = false;
                } catch (IOException e) {
                    System.err.println("Error al copiar el archivo: " + e.getMessage());
                }
            }
            /* LEO EL ARCHIVO, DEPENDIENDO EN DONDE ESTE */
            File file;
            if (operation.equals("copy")){
                file = new File((isOrigin ? "local/" : "backup/") + name);
            }else{
                file = new File((isOrigin ? "local/" : "fs/") + name);
            }
                
           
            /* SI EL ARCHIVO NO EXISTE O ES UN DIRECTORIO, SE LEVANTA UNA EXCEPCION*/
            if(!file.exists() || file.isDirectory())
                throw new IOException("EL ARCHIVO NO EXISTE O ES UN DIRECTORIO");

            String path = file.getAbsolutePath();
            FileInputStream fis = new FileInputStream(file);
           
            /* SE HACE UN OFFSET DEL FIS, DE ACUERDO A LA POSICION ACTUAL */
            fis.skip(position);

            /*  CALCULO CUANTO DEBERIA LEER, SEGUN:
                    -LO QUE TENGA DISPONIBLE EN EL ARCHIVO
                    -LA CANTIDAD QUE ME HAYAN PEDIDO
                    -LA CANTIDAD POR DEFAULT (512 BYTES)
            */
            int nextToRead = Math.min(fis.available(), length);
            nextToRead = Math.min(SIZE, nextToRead);

            /*
             * VERIFICO QUE:
                    -NO HAY NADA MAS PARA LEER (SE LLEGO HASTA EL FINAL)
                    -NO HAY MAS DATOS PARA LEER, DE LOS SOLICITADOS
             */
            if (nextToRead == 0 ||  length == 0){
                finished = true;
            } 
            else {
                System.out.println("INSTANCIANDO DATA");
                data = new byte[nextToRead];
                
                /* 
                    1) TOMO LA CANTIDAD DE DATOS, EFECTIVAMENTE LEIDAS
                    2) INCREMENTO EL OFFSET O POSICION EN DONDE SE DEBE LEER
                       PARA EL PROXIMO READ
                    3) RESTO LA CANTIDAD DE BYTES LEIDOS, DE LOS SOLICITADOS
                */
                effective = fis.read(data);
                position += effective;
                length -= effective;
                readed += effective;

                System.out.println("SE LEYERON " + effective + " BYTES, DESDE " + path);
                System.out.println("CERRANDO EL ARCHIVO");
                fis.close();
            }
        } catch (IOException e){
            System.out.println(e.getMessage());
        }
    }

    /*FUNCIONES AUXILIARES PARA GENERAR COPIAS DE ARCHIVOS*/


    //Esta funcion se encarga de generar una copia de un archivo, dados 2 nombres de archivos.
    public static void copiarArchivo(String nombreArchivoOrigen, String nuevoNombreArchivo) throws IOException {
        File archivoOrigen = new File(nombreArchivoOrigen);
        File archivoDestino = new File(nuevoNombreArchivo);

        try (FileInputStream fis = new FileInputStream(archivoOrigen);
             FileOutputStream fos = new FileOutputStream(archivoDestino)) {

            byte[] buffer = new byte[1024];
            int longitud;

            while ((longitud = fis.read(buffer)) > 0) {
                fos.write(buffer, 0, longitud);
            }
        }
    }

    //Esta funcion genera un nombre unico de una archivo.
    public static String generarNuevoNombre(String nombreArchivo) {
        int indicePunto = nombreArchivo.lastIndexOf(".");
        String nombreBase = nombreArchivo.substring(0, indicePunto);
        String extension = nombreArchivo.substring(indicePunto);

        // Agregar un timestamp para obtener un nombre unico
        long timestamp = System.currentTimeMillis();
        return nombreBase + "_" + timestamp + extension;
    }
}
