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
            if(args.length > 3 && args.length < 6){
                origin = here();
                isOrigin = true;
                finished = false;
                readed = 0;
                destiny = new ContainerID((String) args[0], null);
                operation = (String) args[1];
                name = (String) args[2];
                length = Integer.parseInt((String)args[3]);

                System.out.println("LA OPERACION ES " + operation);
                System.out.println("EL NOMBRE DEL ARCHIVO ES " + name);
                System.out.println("LA CANTIDAD DE BYTES A LEER/ESCRIBIR ES " + length);
                System.out.println("LA CANTIDAD DE ARGS. SON " + args.length);

                /*  
                *       SI LA OPERACION ES 'READ', DEBERIA RECIBIR
                *          'CONTAINER, OPERACION, NOMBRE_ARCH, CANT_BYTES_LEER, POSICION'
                */
                if(checkMethod("read", 5, args.length)){
                    System.out.println("SE EJECUTO EL READ");
                    System.out.println("Debug: Arg 4: "+ args[4]);
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
                else throw new Exception(
                    "LOS ARGS. SON container, operacion (read o write), " + 
                    "nombre_archivo, cant_bytes (mayor a 0), posicion (solo en read)");
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
        else if (operation.equals("read")){
                if(isOrigin)
                    write();
                else{
                //Estoy en el File System y leo el archivo   
                    System.out.println("En el File System");
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
            /* LEO EL ARCHIVO, DEPENDIENDO EN DONDE ESTE */
            File file = new File((isOrigin ? "local/" : "fs/") + name);
           
            /* SI EL ARCHIVO NO EXISTE O ES UN DIRECTORIO, SE LEVANTA UNA EXCEPCION*/
            if(!file.exists() || file.isDirectory())
                throw new IOException("EL ARCHIVO NO EXISTE O ES UN DIRECTORIO");

            String path = file.getAbsolutePath();
            FileInputStream fis = new FileInputStream(file);
           
            /* SE HACE UN OFFSET DEL FIS, DE ACUERDO A LA POSICION ACTUAL */
            fis.skip(position);
            System.out.println("Debug: la posicion es: " + position);

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
                System.out.println("Debug: termine de leer todos los bytes "+finished);
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
}
